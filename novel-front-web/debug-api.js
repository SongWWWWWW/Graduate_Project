const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 900 });

  const apiResponses = [];
  page.on('response', async (response) => {
    const url = response.url();
    if (url.includes('/api/front/') && (url.includes('search') || url.includes('rank'))) {
      try {
        const data = await response.json();
        apiResponses.push({ url, data });
      } catch (e) {}
    }
  });

  // Check BookClass
  await page.goto('http://localhost:1024/?t=' + Date.now() + '#/bookclass', { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 3000));

  console.log('\n=== BookClass API ===');
  for (const resp of apiResponses) {
    if (resp.data?.data?.list) {
      const books = resp.data.data.list;
      console.log('API:', resp.url);
      console.log('Books:', books.map(b => ({ name: b.bookName, picUrl: b.picUrl, id: b.id })));
    }
  }

  // Check BookRank - force fresh
  apiResponses.length = 0;
  await page.goto('http://localhost:1024/?t=' + Date.now() + '#/book_rank', { waitUntil: 'networkidle2', timeout: 30000 });
  await new Promise(r => setTimeout(r, 3000));

  console.log('\n=== BookRank API ===');
  for (const resp of apiResponses) {
    if (resp.data?.data) {
      const books = Array.isArray(resp.data.data) ? resp.data.data : resp.data.data.list;
      console.log('API:', resp.url);
      console.log('Books:', books.map(b => ({ name: b.bookName, picUrl: b.picUrl, id: b.id })));
    }
  }

  await browser.close();
})();
