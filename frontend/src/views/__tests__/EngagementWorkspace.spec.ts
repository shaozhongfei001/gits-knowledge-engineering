import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import EngagementWorkspace from '../EngagementWorkspace.vue'
import type { Customer } from '../../api/engagement'

vi.mock('../../api/engagement', () => ({
  fetchCustomers: vi.fn(),
  generateOutreachScript: vi.fn(),
  generateMeetingScript: vi.fn(),
  executePrevisit: vi.fn(),
  preparePrevisit: vi.fn(),
  executePostvisit: vi.fn(),
  startJourney: vi.fn(),
  handleNewEvidence: vi.fn(),
  completeJourney: vi.fn(),
}))

vi.mock('naive-ui', async () => {
  const actual = await vi.importActual('naive-ui')
  return {
    ...actual,
    useMessage: () => ({
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    }),
  }
})

const mockCustomers: Customer[] = [
  { customerId: 'c1', customerName: '企业A', riskLevel: 'HIGH' },
  { customerId: 'c2', customerName: '企业B', riskLevel: 'LOW' },
]

const stubs = {
  NGrid: { template: '<div class="n-grid"><slot /></div>' },
  NGi: { template: '<div class="n-gi"><slot /></div>' },
  NCard: { template: '<div class="n-card"><slot /></div>' },
  NButton: {
    props: ['disabled'],
    template: '<button class="n-button" :disabled="disabled"><slot /></button>',
  },
  NModal: true,
  NInput: { template: '<input class="n-input" />' },
  NEmpty: { template: '<div class="n-empty" />' },
  NTag: { template: '<span class="n-tag"><slot /></span>' },
  NTable: { template: '<div class="n-table"><slot /></div>' },
  NSpin: { template: '<div class="n-spin" />' },
  NResult: { template: '<div class="n-result"><slot name="footer" /></div>' },
  NTooltip: { template: '<div><slot name="trigger" /><slot /></div>' },
  RiskBadge: { template: '<span class="risk-badge">{{ $attrs.level }}</span>' },
}

async function mountWorkspace() {
  const { fetchCustomers } = await import('../../api/engagement')
  ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomers)
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/engagement', name: 'EngagementWorkspace', component: EngagementWorkspace },
      { path: '/engagement/previsit/gaps', name: 'PrevisitGaps', component: { template: '<div/>' } },
      { path: '/in-meeting/:id?', name: 'InMeetingAssistant', component: { template: '<div/>' } },
    ],
  })
  await router.push('/engagement')
  const wrapper = mount(EngagementWorkspace, {
    global: { plugins: [router], stubs },
  })
  await flushPromises()
  return wrapper
}

describe('P11 EngagementWorkspace', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it('enters successfully with object context and spiral workbench', async () => {
    const wrapper = await mountWorkspace()
    expect(wrapper.get('[data-testid="p11-engagement-workspace"]').text()).toContain('互动 Interaction')
    expect(wrapper.text()).toContain('互动记录·访前路径')
    expect(wrapper.get('[data-testid="p11-object-context"]').text()).toMatch(/未选择|旅程/)
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.findAll('.sp-node').length).toBeGreaterThanOrEqual(5)
  })

  it('shows empty success when the customer list is empty', async () => {
    const { fetchCustomers } = await import('../../api/engagement')
    ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue([])
    setActivePinia(createPinia())
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/engagement', name: 'EngagementWorkspace', component: EngagementWorkspace }],
    })
    await router.push('/engagement')
    const wrapper = mount(EngagementWorkspace, { global: { plugins: [router], stubs } })
    await flushPromises()
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/暂无/)
  })

  it('shows error four-state when fetchCustomers fails', async () => {
    const { fetchCustomers } = await import('../../api/engagement')
    ;(fetchCustomers as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('upstream'))
    setActivePinia(createPinia())
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/engagement', name: 'EngagementWorkspace', component: EngagementWorkspace }],
    })
    await router.push('/engagement')
    const wrapper = mount(EngagementWorkspace, { global: { plugins: [router], stubs } })
    await flushPromises()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('disables writing a formal Claim and keeps spiral actions gated without a journey', async () => {
    const wrapper = await mountWorkspace()
    expect((wrapper.get('[data-testid="gated-action"]').element as HTMLButtonElement).disabled).toBe(true)
    const actions = wrapper.findAll('.ew-act')
    const disabledActions = actions.filter(a => a.classes().includes('disabled'))
    expect(disabledActions.length).toBe(actions.length)
  })

  it('renders spiral node labels', async () => {
    const wrapper = await mountWorkspace()
    const labels = wrapper.findAll('.sp-label').map(el => el.text())
    expect(labels).toContain('启动旅程')
    expect(labels).toContain('访前准备')
    expect(labels).toContain('互动执行')
    expect(labels).toContain('访后复盘')
    expect(labels).toContain('迭代决策')
    expect(labels).toContain('完成旅程')
  })

  it('renders action titles', async () => {
    const wrapper = await mountWorkspace()
    const titles = wrapper.findAll('.ew-act-title').map(el => el.text())
    expect(titles).toContain('执行访前准备（一键）')
    expect(titles).toContain('执行访后复盘')
    expect(titles).toContain('迭代决策')
    expect(titles).toContain('完成旅程')
  })

  it('marks start node as active when no journey', async () => {
    const wrapper = await mountWorkspace()
    const nodes = wrapper.findAll('.sp-node')
    expect(nodes[0].classes()).toContain('active')
  })

  it('shows customer select button', async () => {
    const wrapper = await mountWorkspace()
    const buttons = wrapper.findAll('.n-button')
    const selectBtn = buttons.find(b => b.text().includes('选择客户'))
    expect(selectBtn).toBeDefined()
  })

  it('renders iteration loop badge', async () => {
    const wrapper = await mountWorkspace()
    const badge = wrapper.find('.sp-loop-badge')
    expect(badge.exists()).toBe(true)
    expect(badge.text()).toContain('迭代环')
  })
})
