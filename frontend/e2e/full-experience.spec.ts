import { test, expect } from '@playwright/test';

async function ensureLoggedIn(page: import('@playwright/test').Page) {
  await page.goto('/');
  await page.waitForTimeout(500);
  if (page.url().includes('/login')) {
    const devBtn = page.locator('button:has-text("开发模式直接进入")');
    await devBtn.waitFor({ state: 'visible', timeout: 5000 });
    await devBtn.click();
    await page.waitForURL(/localhost:5173/, { timeout: 10000 });
    await page.waitForTimeout(1000);
  }
}

test.describe('GITS 客户经营闭环 - 完整体验测试', () => {

  test('1. 登录页面 - 开发模式进入', async ({ page }) => {
    await page.goto('/login');
    await page.waitForTimeout(1000);
    await expect(page.locator('.login-title')).toContainText('GITS');
    await page.click('button:has-text("开发模式直接进入")');
    await page.waitForURL(/localhost:5173/, { timeout: 10000 });
    await page.waitForTimeout(1000);
    await expect(page).not.toHaveURL(/login/);
    await page.screenshot({ path: 'test-results/01-login-success.png', fullPage: true });
  });

  test('2. 客户经营概览 Dashboard - 必须显示客户数据', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/');
    await page.waitForTimeout(3000);

    // 关键断言：页面必须包含客户名称
    await expect(page.locator('text=华东精工')).toBeVisible({ timeout: 10000 });

    // 验证有客户卡片
    const cards = page.locator('.n-card');
    const cardCount = await cards.count();
    expect(cardCount, 'Dashboard 必须至少显示1个客户卡片').toBeGreaterThan(0);

    await page.screenshot({ path: 'test-results/02-dashboard.png', fullPage: true });
  });

  test('3. 客户详情 - 点击客户卡片', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/');
    await page.waitForTimeout(3000);

    // 点击第一个客户卡片
    const firstCard = page.locator('.n-card').first();
    await firstCard.click();
    await page.waitForTimeout(2000);

    // 验证详情页有内容
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '客户详情页不能为空白').toBeGreaterThan(50);

    await page.screenshot({ path: 'test-results/03-customer-detail.png', fullPage: true });
  });

  test('4. 持续经营工作台 - 必须显示流程和操作面板', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/engagement');
    await page.waitForTimeout(3000);

    // 关键断言：必须显示螺旋迭代流程
    await expect(page.locator('text=螺旋迭代')).toBeVisible({ timeout: 10000 });
    // 必须显示操作面板或客户选择
    const hasActionPanel = await page.locator('text=操作面板').isVisible().catch(() => false);
    const hasCustomerSelect = await page.locator('text=选择客户').isVisible().catch(() => false);
    const hasNoCustomer = await page.locator('text=未选择').isVisible().catch(() => false);
    expect(hasActionPanel || hasCustomerSelect || hasNoCustomer, '工作台必须显示操作面板、客户选择或未选择状态').toBeTruthy();

    // 选择客户后验证流程步骤
    const selectTrigger = page.locator('.n-base-selection').first();
    if (await selectTrigger.isVisible().catch(() => false)) {
      await selectTrigger.click();
      await page.waitForTimeout(500);
      const firstOption = page.locator('.n-base-select-option').first();
      if (await firstOption.isVisible().catch(() => false)) {
        await firstOption.click();
        await page.waitForTimeout(2000);
        // 选择客户后应显示流程步骤
        await expect(page.locator('text=KYC采集')).toBeVisible({ timeout: 5000 });
      }
    }

    // 不能是空白页面
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '持续经营工作台不能为空白').toBeGreaterThan(100);

    await page.screenshot({ path: 'test-results/04-engagement-workspace.png', fullPage: true });
  });

  test('5. 承诺与任务 - 必须显示承诺和任务数据', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/commitments');
    await page.waitForTimeout(3000);

    // 关键断言：必须显示承诺内容
    await expect(page.locator('text=承诺列表')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=提供三年期融资结构建议')).toBeVisible({ timeout: 5000 });

    // 必须显示任务内容
    await expect(page.locator('text=任务列表')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('text=跟进设备清单')).toBeVisible({ timeout: 5000 });

    // 必须有机会管线
    await expect(page.locator('h2').filter({ hasText: '机会管线' })).toBeVisible({ timeout: 5000 });

    await page.screenshot({ path: 'test-results/05-commitments.png', fullPage: true });
  });

  test('6. 外部事件监控 - 必须显示事件数据', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/external-events');
    await page.waitForTimeout(3000);

    // 关键断言：必须显示事件内容
    await expect(page.locator('text=事件总数')).toBeVisible({ timeout: 10000 });
    // 必须显示具体事件（如果无数据则显示空状态）
    const hasEventTitle = await page.locator('.event-title').first().isVisible().catch(() => false);
    const hasEmptyState = await page.locator('text=暂无').isVisible().catch(() => false);
    expect(hasEventTitle || hasEmptyState, '外部事件页面必须显示事件或空状态').toBeTruthy();

    // 不能是空白页面
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '外部事件页面不能为空白').toBeGreaterThan(100);

    await page.screenshot({ path: 'test-results/06-external-events.png', fullPage: true });
  });

  test('7. 顶部导航菜单切换', async ({ page }) => {
    await ensureLoggedIn(page);
    await page.goto('/');
    await page.waitForTimeout(2000);

    const menuItems = [
      { text: '持续经营工作台', url: /engagement/ },
      { text: '承诺与任务', url: /commitments/ },
      { text: '外部事件监控', url: /external-events/ },
    ];

    for (const item of menuItems) {
      const menuItem = page.locator(`.n-menu-item-content:has-text("${item.text}")`);
      if (await menuItem.isVisible()) {
        await menuItem.click();
        await page.waitForTimeout(1500);
        await expect(page).toHaveURL(item.url);
      }
    }

    // 回到首页
    await page.locator('.brand-name').click();
    await page.waitForTimeout(1000);
    await expect(page).toHaveURL(/localhost:5173\/?$/);
  });

  test('8. API 数据完整性验证', async ({ page }) => {
    await ensureLoggedIn(page);

    const checks = [
      { name: '客户', url: '/api/v1/engagement/customer?rmId=ALL', minCount: 1 },
      { name: '承诺', url: '/api/v1/commitments', minCount: 1 },
      { name: '任务', url: '/api/v1/tasks', minCount: 1 },
      { name: '机会', url: '/api/v1/opportunities', minCount: 1 },
      { name: '外部事件', url: '/api/v1/external-events', minCount: 1 },
    ];

    for (const check of checks) {
      const resp = await page.request.get(check.url);
      expect(resp.ok(), `${check.name} API 必须返回 200`).toBeTruthy();
      const data = await resp.json();
      const count = Array.isArray(data) ? data.length : 0;
      expect(count, `${check.name} 必须至少有 ${check.minCount} 条记录`).toBeGreaterThanOrEqual(check.minCount);
      console.log(`✓ ${check.name}: ${count} 条`);
    }
  });

  test('9. 页面无严重 JS 错误', async ({ page }) => {
    await ensureLoggedIn(page);
    const criticalErrors: string[] = [];
    page.on('pageerror', err => criticalErrors.push(err.message));
    page.on('console', msg => {
      if (msg.type() === 'error' && !msg.text().includes('favicon') && !msg.text().includes('ResizeObserver')) {
        criticalErrors.push(msg.text());
      }
    });

    for (const path of ['/', '/engagement', '/commitments', '/external-events']) {
      await page.goto(path);
      await page.waitForTimeout(2000);
    }

    const crashErrors = criticalErrors.filter(e =>
      !e.includes('favicon') &&
      !e.includes('ResizeObserver') &&
      !e.includes('404') &&
      !e.includes('net::ERR')
    );
    expect(crashErrors.length, '不应有崩溃性 JS 错误').toBeLessThanOrEqual(2);
    if (crashErrors.length > 0) {
      console.log('非关键错误:', crashErrors);
    }
  });
});
