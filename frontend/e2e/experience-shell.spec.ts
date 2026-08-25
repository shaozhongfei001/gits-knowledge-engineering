import { test, expect } from '@playwright/test';
import { installApiMocks } from './sit-fixtures';

test.describe('P30 Experience Shell', () => {
  test('workbench opens with GITS Bank shell and left groups', async ({ page }) => {
    await installApiMocks(page);
    await page.goto('/workbench');
    await expect(page.getByTestId('shell-brand')).toContainText('GITS Bank');
    await expect(page.getByTestId('shell-sidebar')).toBeVisible();
    await expect(page.getByTestId('nav-group-daily')).toBeVisible();
    await expect(page.getByTestId('p01-workbench')).toBeVisible();
    await expect(page.getByTestId('success-state')).toBeVisible();
    await expect(page.getByTestId('object-header')).toContainText('首页·我的客户经营');
  });

  test('legacy / still opens the workbench', async ({ page }) => {
    await installApiMocks(page);
    await page.goto('/');
    await expect(page.getByTestId('p01-workbench')).toBeVisible();
    await expect(page.getByTestId('p01-action-queue')).toContainText('测试企业A');
  });

  test('API 500 is an error state, not a pass', async ({ page }) => {
    await installApiMocks(page, 'error');
    await page.goto('/workbench');
    await expect(page.getByTestId('error-state')).toBeVisible();
    await expect(page.getByTestId('success-state')).toHaveCount(0);
  });

  test('P01 confirmation write stays DisabledAction', async ({ page }) => {
    await installApiMocks(page);
    await page.goto('/workbench');
    await expect(page.getByTestId('gated-action')).toBeDisabled();
    await expect(page.getByTestId('disabled-reason')).toContainText(/原因|解除路径/);
  });
});
