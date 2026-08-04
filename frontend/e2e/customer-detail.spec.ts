import { test, expect } from '@playwright/test';

const mockCustomer = {
  customerId: 'cust-001',
  customerName: '测试企业A',
  riskLevel: 'HIGH',
  customerTier: 'STRATEGIC',
  industry: 'MANUFACTURING',
  enterpriseScale: 'LARGE',
  region: '华东',
  registeredCapitalCny: 50000000,
  listedStatus: 'LISTED',
  relationshipSince: '2020-01-01',
  rmName: '张经理',
  managingBranch: '总行营业部'
};

const mockContext = {
  customer: mockCustomer,
  kycGapProfile: {
    profileId: 'kgp-001',
    customerId: 'cust-001',
    asOf: '2026-08-05',
    knownItems: ['企业基本信息', '股权结构'],
    partialKnownItems: ['财务数据'],
    staleItems: ['行业分析'],
    conflictingOrAmbiguousItems: [],
    unknownItems: ['实控人信息'],
    priorityQuestions: ['请补充实控人变更信息']
  },
  opportunitySignals: [
    {
      signalId: 'sig-001',
      signalType: 'FINANCING_NEED',
      content: '企业有新增融资需求',
      sourceType: 'INTERACTION',
      status: 'DETECTED',
      detectedAt: '2026-08-01'
    }
  ],
  recentInteractions: [],
  activeJourneys: [],
  recentTransactions: []
};

test.describe('Customer Detail', () => {
  test.beforeEach(async ({ page }) => {
    // Mock customer context API
    await page.route('**/api/v1/engagement/customers/cust-001/context', async route => {
      await route.fulfill({ json: mockContext });
    });
    await page.route('**/api/v1/engagement/customers/cust-001', async route => {
      await route.fulfill({ json: mockCustomer });
    });
  });

  test('should display customer basic info', async ({ page }) => {
    await page.goto('/customer/cust-001');
    await expect(page.locator('.customer-header')).toBeVisible();
    await expect(page.locator('.customer-header')).toContainText('测试企业A');
  });

  test('should display KYC gap profile section', async ({ page }) => {
    await page.goto('/customer/cust-001');
    await expect(page.locator('.kyc-section')).toBeVisible();
    await expect(page.locator('.kyc-section')).toContainText('KYC');
  });

  test('should display opportunity signals', async ({ page }) => {
    await page.goto('/customer/cust-001');
    await expect(page.locator('.signals-section')).toBeVisible();
  });
});
