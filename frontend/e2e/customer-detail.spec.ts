import { test, expect } from '@playwright/test';

test.describe('Customer Detail', () => {
  test.skip('should search and navigate to customer', async ({ page }) => {
    await page.goto('/');
    // Wait for customer list to load
    await expect(page.locator('.loading-state')).not.toBeVisible({ timeout: 10000 });
    // Click on the first customer card
    const firstCard = page.locator('.customer-grid .customer-card').first();
    if (await firstCard.isVisible()) {
      await firstCard.click();
      await expect(page).toHaveURL(/\/customers\//);
    }
  });

  test.skip('should display customer detail view', async ({ page }) => {
    // Navigate directly to a customer page (requires a valid customer ID)
    await page.goto('/customers/test-customer-id');
    // Customer info section should be visible
    const customerInfo = page.locator('.customer-info-section');
    await expect(customerInfo).toBeVisible();
  });

  test.skip('should show operating view with KYC gaps and signals', async ({ page }) => {
    await page.goto('/customers/test-customer-id');
    // KYC gap section
    const kycSection = page.locator('.section:has-text("KYC缺口摘要")');
    if (await kycSection.isVisible()) {
      await expect(kycSection.locator('.gap-card')).toHaveCount(3);
    }
    // Opportunity signals section
    const signalSection = page.locator('.section:has-text("机会信号")');
    await expect(signalSection).toBeVisible();
  });
});
