import { chromium } from 'playwright';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const files = [
  { html: 'architecture.html', png: 'architecture.png', vw: 1280, vh: 900 },
  { html: 'flow.html', png: 'flow.png', vw: 1280, vh: 1750 },
];

const browser = await chromium.launch();
for (const f of files) {
  const page = await browser.newPage({ viewport: { width: f.vw, height: f.vh } });
  const url = 'file://' + path.resolve(__dirname, f.html);
  await page.goto(url, { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  await page.evaluate(() => document.fonts && document.fonts.ready);
  await page.waitForTimeout(800);
  const svg = page.locator('svg').first();
  const box = await svg.boundingBox();
  if (box) {
    await page.screenshot({ path: path.resolve(__dirname, f.png), fullPage: true });
    console.log(`rendered ${f.png} svg ${Math.round(box.width)}x${Math.round(box.height)}`);
  } else {
    console.log(`no svg found in ${f.html}`);
  }
  await page.close();
}
await browser.close();
