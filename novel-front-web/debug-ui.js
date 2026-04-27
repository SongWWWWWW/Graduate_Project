const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: true, defaultViewport: { width: 1280, height: 800 } });
  const page = await browser.newPage();

  // BookClass
  try {
    await page.goto('http://localhost:1024/#/bookclass', { waitUntil: 'networkidle2', timeout: 30000 });
    await new Promise(r => setTimeout(r, 3000));
    await page.screenshot({ path: 'C:\\Users\\wcc\\Project\\novel-front-web\\debug-bookclass.png', fullPage: true });
    console.log('BookClass screenshot saved');
  } catch (e) { console.error('BookClass failed:', e.message); }

  // BookRank
  try {
    await page.goto('http://localhost:1024/#/book_rank', { waitUntil: 'networkidle2', timeout: 30000 });
    await new Promise(r => setTimeout(r, 3000));
    await page.screenshot({ path: 'C:\\Users\\wcc\\Project\\novel-front-web\\debug-bookrank.png', fullPage: true });
    console.log('BookRank screenshot saved');
  } catch (e) { console.error('BookRank failed:', e.message); }

  await browser.close();
})();
