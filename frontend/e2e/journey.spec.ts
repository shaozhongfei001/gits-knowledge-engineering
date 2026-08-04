import { test, expect } from '@playwright/test';

const mockJourneys = [
  {
    journeyId: 'jrn-001',
    operatingCaseId: 'case-001',
    customerId: 'cust-001',
    customerName: '测试企业A',
    phase: 'INSIGHT_ANALYSIS',
    startedAt: '2026-08-01'
  }
];

const mockInteractions = [
  {
    interactionId: 'int-001',
    caseId: 'case-001',
    journeyId: 'jrn-001',
    type: 'PHONE_CALL',
    direction: 'OUTBOUND',
    channel: '电话',
    initiator: { participantId: 'rm-001', role: 'RM', displayName: '张经理' },
    contentSummary: '了解企业融资需求',
    outcome: 'INFORMATION_GATHERED',
    occurredAt: '2026-08-02'
  }
];

const mockClaims = [
  {
    claimId: 'clm-001',
    operatingCaseId: 'case-001',
    journeyId: 'jrn-001',
    claimType: 'FINANCING_NEED',
    content: '企业有5000万新增融资需求',
    status: 'CANDIDATE_CLAIM',
    createdAt: '2026-08-02'
  }
];

test.describe('Journey Timeline', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/v1/engagement/customers/cust-001/journeys', async route => {
      await route.fulfill({ json: mockJourneys });
    });
    await page.route('**/api/v1/engagement/journeys/jrn-001/interactions', async route => {
      await route.fulfill({ json: mockInteractions });
    });
    await page.route('**/api/v1/engagement/journeys/jrn-001/claims', async route => {
      await route.fulfill({ json: mockClaims });
    });
  });

  test('should display journey list for customer', async ({ page }) => {
    await page.goto('/customer/cust-001/journeys');
    await expect(page.locator('.journey-list')).toBeVisible();
  });

  test('should display journey phase', async ({ page }) => {
    await page.goto('/customer/cust-001/journeys');
    await expect(page.locator('.journey-card').first()).toBeVisible();
  });
});
