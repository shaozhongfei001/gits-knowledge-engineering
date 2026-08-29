import { test, expect, type Page } from '@playwright/test';

/**
 * Live W9-A E2E for 信号与互动 / 客户经营旅程.
 * Hits the running Vite proxy + API (no SIT mocks; mocks would 403 POST /journey/start).
 * Run: npx playwright test --config=playwright.live.config.ts
 */
const API = process.env.LIVE_API_URL || 'http://127.0.0.1:8082';

async function requireApi(): Promise<void> {
  const response = await fetch(`${API}/api/v1/interactions`);
  if (!response.ok) {
    throw new Error(`Live API ${API} GET /api/v1/interactions returned ${response.status}`);
  }
}

async function noHttp500(page: Page): Promise<string[]> {
  const failures: string[] = [];
  page.on('response', res => {
    if (res.url().includes('/api/') && res.status() >= 500) {
      failures.push(`${res.status()} ${res.url()}`);
    }
  });
  return failures;
}

test.describe('信号与互动 live domain', () => {
  test.beforeAll(async () => {
    await requireApi();
  });

  test('L1 信号与互动 opens the journey workspace, not a blank shell', async ({ page }) => {
    const failures = await noHttp500(page);
    await page.goto('/workbench', { waitUntil: 'networkidle' });
    await page.getByTestId('nav-item-signals').click();
    await expect(page).toHaveURL(/\/engagement/);
    await expect(page.getByTestId('p11-engagement-workspace')).toBeVisible();
    await expect(page.getByTestId('signals-domain-tabs')).toBeVisible();
    await expect(page.getByTestId('p11-start-journey')).toBeVisible();
    await expect(page.getByTestId('p11-object-context')).not.toContainText('未选择客户');
    expect(failures, failures.join('\n')).toEqual([]);
  });

  test('domain tabs reach 经营信号 and 互动对象 without 500', async ({ page }) => {
    const failures = await noHttp500(page);
    await page.goto('/engagement', { waitUntil: 'networkidle' });
    await page.getByTestId('signals-tab-p08').click();
    await expect(page).toHaveURL(/\/signals/);
    await expect(page.getByTestId('p08-signals')).toBeVisible();
    await expect(page.getByTestId('success-state')).toBeVisible();
    await expect(page.getByTestId('gated-action')).toBeDisabled();

    await page.getByTestId('signals-tab-p10').click();
    await expect(page).toHaveURL(/\/engagements/);
    await expect(page.getByTestId('p10-engagements')).toBeVisible();
    await expect(page.getByTestId('success-state')).toBeVisible();
    await expect(page.getByTestId('error-state')).toHaveCount(0);
    await expect(page.getByTestId('p10-engagements')).not.toContainText('500');
    await expect(page.getByTestId('gated-action')).toBeDisabled();
    expect(failures, failures.join('\n')).toEqual([]);
  });

  test('starts a customer journey from the domain entry and lists the interaction', async ({ page }) => {
    const failures = await noHttp500(page);
    const startPosts: number[] = [];
    page.on('response', res => {
      if (res.url().includes('/journey/start') && res.request().method() === 'POST') {
        startPosts.push(res.status());
      }
    });

    await page.goto('/engagement', { waitUntil: 'networkidle' });
    await expect(page.getByTestId('p11-start-journey')).toBeEnabled();
    await page.getByTestId('p11-start-journey').click();
    await expect(page.getByTestId('p11-object-context')).not.toContainText('未启动', { timeout: 30000 });
    await expect(page.getByTestId('p11-start-error')).toHaveCount(0);
    expect(startPosts.some(status => status === 201 || status === 200), `start statuses=${startPosts.join(',')}`).toBe(true);

    await page.getByTestId('signals-tab-p10').click();
    await expect(page.getByTestId('p10-engagements')).toBeVisible();
    await expect(page.locator('[data-testid="p10-interaction-row"]').first()).toBeVisible({ timeout: 15000 });
    expect(failures, failures.join('\n')).toEqual([]);
  });

  test('P11 subnav C0 slices load and C2 writes stay gated', async ({ page }) => {
    const failures = await noHttp500(page);
    await page.goto('/engagement', { waitUntil: 'networkidle' });
    await page.getByTestId('p11-link-gaps').click();
    await expect(page.getByTestId('p12-previsit-gaps')).toBeVisible();
    await expect(page.getByTestId('gated-action')).toBeDisabled();

    await page.goto('/engagement', { waitUntil: 'domcontentloaded' });
    await page.getByTestId('p11-link-evidence').click();
    await expect(page.getByTestId('p13-previsit-evidence')).toBeVisible();

    await page.goto('/engagement', { waitUntil: 'domcontentloaded' });
    await page.getByTestId('p11-link-pack').click();
    await expect(page.getByTestId('p14-previsit-pack')).toBeVisible();

    await page.goto('/engagement', { waitUntil: 'domcontentloaded' });
    await page.getByTestId('p11-link-postvisit').click();
    await expect(page.getByTestId('p18-postvisit')).toBeVisible();

    await page.goto('/engagement', { waitUntil: 'domcontentloaded' });
    await page.getByTestId('p11-link-crm').click();
    await expect(page.getByTestId('p19-crm-writeback')).toBeVisible();

    await page.goto('/engagement', { waitUntil: 'domcontentloaded' });
    await page.getByTestId('p11-link-in-meeting').click();
    await expect(page.getByTestId('p15-in-meeting')).toBeVisible();
    await expect(page.getByTestId('gated-action')).toBeDisabled();
    expect(failures, failures.join('\n')).toEqual([]);
  });

  test('signal row opens P09 when signals exist; otherwise empty is success', async ({ page }) => {
    await page.goto('/signals', { waitUntil: 'networkidle' });
    await expect(page.getByTestId('p08-signals')).toBeVisible();
    const rows = page.locator('.signal-link');
    if (await rows.count()) {
      await rows.first().click();
      await expect(page.getByTestId('p09-signal-record')).toBeVisible();
      await expect(page.getByTestId('gated-action').first()).toBeDisabled();
    } else {
      await expect(page.getByTestId('success-state')).toBeVisible();
      await expect(page.getByTestId('p08-signals')).toContainText(/暂无/);
    }
  });
});
