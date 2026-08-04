import { test, expect } from '@playwright/test';

const mockReport = {
  reportId: 'rpt-001',
  operatingCaseId: 'case-001',
  journeyId: 'jrn-001',
  reportType: 'INTERNAL_RELATIONSHIP',
  content: '## 客户关系报告\n\n测试企业A为战略客户，当前有新增融资需求信号。建议安排面对面拜访。',
  basedOnEvidence: ['ev-001', 'ev-002'],
  basedOnReconciliations: ['rec-001'],
  generatedAt: '2026-08-05'
};

test.describe('Report Detail', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/v1/engagement/reports/rpt-001', async route => {
      await route.fulfill({ json: mockReport });
    });
  });

  test('should display report content', async ({ page }) => {
    await page.goto('/report/rpt-001');
    await expect(page.locator('.report-detail')).toBeVisible();
  });

  test('should display report type badge', async ({ page }) => {
    await page.goto('/report/rpt-001');
    await expect(page.locator('.report-type-badge')).toBeVisible();
  });
});
