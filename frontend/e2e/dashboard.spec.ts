import { test, expect } from '@playwright/test';

// Mock data matching the frontend API types
const mockCustomers = [
  {
    customerId: 'cust-001',
    customerName: '测试企业A',
    riskLevel: 'HIGH',
    customerTier: 'STRATEGIC',
    industry: 'MANUFACTURING',
    enterpriseScale: 'LARGE',
    region: '华东'
  },
  {
    customerId: 'cust-002',
    customerName: '测试企业B',
    riskLevel: 'LOW',
    customerTier: 'GROWTH',
    industry: 'TECHNOLOGY',
    enterpriseScale: 'MEDIUM',
    region: '华北'
  }
];

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Mock the customers API
    await page.route('**/api/v1/engagement/customers', async route => {
      await route.fulfill({ json: mockCustomers });
    });
  });

  test('should display page title', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('h1')).toHaveText('客户经营概览');
  });

  test('should render statistics cards', async ({ page }) => {
    await page.goto('/');
    const statsBar = page.locator('.stats-bar');
    await expect(statsBar).toBeVisible();
    await expect(statsBar.locator('.stat-item').first()).toContainText('客户总数');
    await expect(statsBar.locator('.stat-item').nth(1)).toContainText('高风险客户');
    await expect(statsBar.locator('.stat-item').nth(2)).toContainText('战略客户');
  });

  test('should load and display customer list', async ({ page }) => {
    await page.goto('/');
    // Wait for loading to finish
    await expect(page.locator('.loading-state')).not.toBeVisible({ timeout: 10000 });
    // Customer grid should be visible with mock data
    const customerGrid = page.locator('.customer-grid');
    await expect(customerGrid).toBeVisible();
  });
});
