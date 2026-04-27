const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 900 });

  // Intercept console logs
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));

  await page.goto('http://localhost:1024/#/bookclass', { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 3000));

  const imageInfo = await page.evaluate(() => {
    const images = Array.from(document.querySelectorAll('.book-cover-thumb'));
    return images.map((img, i) => ({
      index: i,
      src: img.src,
      alt: img.alt,
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
      width: img.width,
      height: img.height,
      complete: img.complete,
      // Check if parent td exists
      parentRow: img.closest('tr')?.querySelector('.name')?.textContent?.trim()?.substring(0, 20)
    }));
  });

  console.log('\n=== BookClass Images ===');
  console.log(JSON.stringify(imageInfo, null, 2));

  // Also check picUrl values from Vue data
  const picUrls = await page.evaluate(() => {
    // Try to access Vue instance data
    const rows = document.querySelectorAll('#bookList tr');
    return Array.from(rows).map((row, i) => {
      const img = row.querySelector('.book-cover-thumb');
      return {
        index: i,
        picUrl: img?.getAttribute('data-pic-url') || 'N/A',
        actualSrc: img?.src || 'N/A'
      };
    });
  });

  console.log('\n=== PicUrl Analysis ===');
  console.log(JSON.stringify(picUrls, null, 2));

  await browser.close();
})();
