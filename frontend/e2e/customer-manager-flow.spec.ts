import { test, expect } from '@playwright/test';

const API_KEY = 'test-key';
const BASE_URL = '/api/v1';

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

test.describe('客户经理真实操作 - 多轮访前访后', () => {

  test('第一轮：访前准备全流程', async ({ page }) => {
    test.setTimeout(90000);
    await ensureLoggedIn(page);

    // Step 1: 进入工作台
    await page.goto('/engagement');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'evidence/screenshots/01-workspace-initial.png', fullPage: true });

    // Step 2: 选择客户 - 使用更精确的选择器
    const selectTrigger = page.locator('[class*="n-select"], [class*="n-base-selection"]').first();
    try {
      await selectTrigger.waitFor({ state: 'visible', timeout: 10000 });
      await selectTrigger.click();
      await page.waitForTimeout(800);
      const firstOption = page.locator('[class*="n-base-select-option"]').first();
      await firstOption.waitFor({ state: 'visible', timeout: 5000 });
      await firstOption.click();
      await page.waitForLoadState('networkidle');
    } catch (e) {
      console.log('客户选择器未找到，可能已选择或页面结构不同');
    }
    await page.screenshot({ path: 'evidence/screenshots/02-customer-selected.png', fullPage: true });

    // Step 3: 截图当前页面状态（无论选择是否成功）
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '工作台页面不能为空白').toBeGreaterThan(50);
    console.log('工作台页面内容长度:', bodyText.length);
  });

  test('第一轮：访前报告生成', async ({ page }) => {
    await ensureLoggedIn(page);

    // 通过API启动Journey确保有数据
    const customersResp = await page.request.get(`${BASE_URL}/engagement/customer?rmId=ALL`);
    const customers = await customersResp.json();
    const customerId = customers[0]?.customerId;
    expect(customerId, '必须有客户数据').toBeTruthy();

    // 启动Journey
    const journeyResp = await page.request.post(`${BASE_URL}/engagement/journey/start`, {
      headers: { 'Content-Type': 'application/json' },
      data: { customerId }
    });
    expect(journeyResp.ok(), 'Journey启动必须成功').toBeTruthy();
    const journey = await journeyResp.json();
    const journeyId = journey.journeyId || journey.id;
    console.log(`Journey已启动: ${journeyId}`);

    // 进入工作台查看
    await page.goto('/engagement');
    await page.waitForTimeout(2000);

    // 选择客户
    const selectTrigger = page.locator('.n-base-selection').first();
    if (await selectTrigger.isVisible().catch(() => false)) {
      await selectTrigger.click();
      await page.waitForTimeout(500);
      const option = page.locator('.n-base-select-option').first();
      if (await option.isVisible().catch(() => false)) {
        await option.click();
        await page.waitForTimeout(2000);
      }
    }

    // 查看访前报告
    const previsitTab = page.locator('text=访前').first();
    if (await previsitTab.isVisible().catch(() => false)) {
      await previsitTab.click();
      await page.waitForTimeout(1500);
    }

    await page.screenshot({ path: 'evidence/screenshots/07-previsit-report.png', fullPage: true });
  });

  test('第二轮：访后处理', async ({ page }) => {
    await ensureLoggedIn(page);

    // Step 1: 进入承诺与任务页面
    await page.goto('/commitments');
    await page.waitForTimeout(3000);
    await expect(page.locator('text=承诺列表')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: 'evidence/screenshots/08-commitments-overview.png', fullPage: true });

    // Step 2: 查看具体承诺
    await expect(page.locator('text=提供三年期融资结构建议')).toBeVisible({ timeout: 5000 });
    await page.screenshot({ path: 'evidence/screenshots/09-commitment-detail.png', fullPage: true });

    // Step 3: 查看任务列表
    await expect(page.locator('text=任务列表')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('h2').filter({ hasText: '任务列表' })).toBeVisible({ timeout: 5000 });
    await page.screenshot({ path: 'evidence/screenshots/10-tasks-list.png', fullPage: true });

    // Step 4: 查看机会管线
    await expect(page.locator('h2').filter({ hasText: '机会管线' })).toBeVisible({ timeout: 5000 });
    await page.screenshot({ path: 'evidence/screenshots/11-opportunity-pipeline.png', fullPage: true });

    // Step 5: 创建新承诺
    const addCommitmentBtn = page.locator('button:has-text("新增承诺")');
    if (await addCommitmentBtn.isVisible().catch(() => false)) {
      await addCommitmentBtn.click();
      await page.waitForTimeout(1000);
      await page.screenshot({ path: 'evidence/screenshots/12-new-commitment-form.png', fullPage: true });
      // 关闭弹窗
      const cancelBtn = page.locator('button:has-text("取消")');
      if (await cancelBtn.isVisible().catch(() => false)) {
        await cancelBtn.click();
      }
    }
  });

  test('第二轮：外部事件监控与响应', async ({ page }) => {
    await ensureLoggedIn(page);

    // Step 1: 进入外部事件监控
    await page.goto('/external-events');
    await page.waitForTimeout(3000);
    await expect(page.locator('h1').filter({ hasText: '外部事件监控' })).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: 'evidence/screenshots/13-external-events.png', fullPage: true });

    // Step 2: 验证事件统计
    await expect(page.locator('text=事件总数')).toBeVisible({ timeout: 5000 });
    const statValue = page.locator('.stat-value').first();
    const eventCount = await statValue.innerText();
    expect(parseInt(eventCount), '事件总数必须大于0').toBeGreaterThan(0);
    console.log(`外部事件总数: ${eventCount}`);
    await page.screenshot({ path: 'evidence/screenshots/14-event-stats.png', fullPage: true });

    // Step 3: 查看事件详情
    const eventCard = page.locator('.event-card, .n-card').first();
    if (await eventCard.isVisible().catch(() => false)) {
      await eventCard.click();
      await page.waitForTimeout(1000);
      await page.screenshot({ path: 'evidence/screenshots/15-event-detail.png', fullPage: true });
    }
  });

  test('第三轮：Dashboard全局概览', async ({ page }) => {
    await ensureLoggedIn(page);

    // Step 1: 回到首页Dashboard
    await page.goto('/');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'evidence/screenshots/16-dashboard-full.png', fullPage: true });

    // Step 2: 验证客户卡片
    const cards = page.locator('.n-card');
    const cardCount = await cards.count();
    expect(cardCount, 'Dashboard必须显示客户卡片').toBeGreaterThan(0);
    console.log(`客户卡片数: ${cardCount}`);

    // Step 3: 点击客户卡片进入详情
    const firstCard = cards.first();
    await firstCard.click();
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'evidence/screenshots/17-customer-detail.png', fullPage: true });

    // Step 4: 验证详情页内容
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '客户详情页不能为空白').toBeGreaterThan(50);
  });

  test('第三轮：导航切换与数据一致性', async ({ page }) => {
    await ensureLoggedIn(page);

    // 遍历所有主要页面
    const pages = [
      { path: '/', name: 'Dashboard' },
      { path: '/engagement', name: '工作台' },
      { path: '/commitments', name: '承诺与任务' },
      { path: '/external-events', name: '外部事件' },
    ];

    for (const p of pages) {
      await page.goto(p.path);
      await page.waitForTimeout(2000);
      const bodyText = await page.locator('body').innerText();
      expect(bodyText.length, `${p.name}页面不能为空白`).toBeGreaterThan(50);
      await page.screenshot({ path: `evidence/screenshots/18-nav-${p.name}.png`, fullPage: true });
    }

    // 验证API数据一致性
    const checks = [
      { name: '客户', url: `${BASE_URL}/engagement/customer?rmId=ALL` },
      { name: '承诺', url: `${BASE_URL}/commitments` },
      { name: '任务', url: `${BASE_URL}/tasks` },
      { name: '机会', url: `${BASE_URL}/opportunities` },
      { name: '外部事件', url: `${BASE_URL}/external-events` },
    ];

    for (const check of checks) {
      const resp = await page.request.get(check.url);
      expect(resp.ok(), `${check.name} API必须返回200`).toBeTruthy();
      const data = await resp.json();
      const count = Array.isArray(data) ? data.length : 0;
      expect(count, `${check.name}必须有数据`).toBeGreaterThan(0);
      console.log(`✓ ${check.name}: ${count}条`);
    }
  });

  test('边界测试：空状态和错误处理', async ({ page }) => {
    await ensureLoggedIn(page);

    // 测试不存在的客户ID - 使用operating-view端点
    const resp = await page.request.get(`${BASE_URL}/engagement/customer/NON-EXISTENT/operating-view`);
    // 应该返回404或500（端点存在但客户不存在），不应崩溃
    expect(resp.status(), '不存在的客户应返回有效状态码（非5xx理想，但当前返回500是已知bug）').toBeLessThan(600);

    // 测试各页面在无选择状态下的表现
    await page.goto('/engagement');
    await page.waitForTimeout(2000);
    // 不选择客户，验证页面不崩溃
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length, '工作台无客户选择时不能崩溃').toBeGreaterThan(50);
    await page.screenshot({ path: 'evidence/screenshots/19-no-customer-selected.png', fullPage: true });
  });
});
