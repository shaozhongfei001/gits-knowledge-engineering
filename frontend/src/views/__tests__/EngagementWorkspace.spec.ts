import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import EngagementWorkspace from '../EngagementWorkspace.vue'
import type { Customer } from '../../api/engagement'

vi.mock('../../api/engagement', () => ({
  fetchCustomers: vi.fn(),
  fetchKycGapProfile: vi.fn(),
  generateOutreachScript: vi.fn(),
  generateMeetingScript: vi.fn(),
  executePrevisit: vi.fn(),
  preparePrevisit: vi.fn(),
  executePostvisit: vi.fn(),
  startJourney: vi.fn(),
  handleNewEvidence: vi.fn(),
  completeJourney: vi.fn(),
  executeSupplyChainGraph: vi.fn(),
  formatApiError: (e: unknown, fallback: string) => (e instanceof Error ? e.message : fallback),
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

const emptyGap = {
  profileId: 'p1',
  customerId: 'c1',
  asOf: '2026-08-25T00:00:00Z',
  knownItems: [],
  partialKnownItems: [],
  staleItems: [],
  conflictingOrAmbiguousItems: [],
  unknownItems: [],
  priorityQuestions: [],
}

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
  const { fetchCustomers, fetchKycGapProfile } = await import('../../api/engagement')
  ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomers)
  ;(fetchKycGapProfile as ReturnType<typeof vi.fn>).mockResolvedValue(emptyGap)
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
    vi.clearAllMocks()
  })

  it('renders object home with stage path and metrics', async () => {
    const wrapper = await mountWorkspace()
    expect(wrapper.get('[data-testid="p11-engagement-workspace"]').text()).toContain('互动 Interaction')
    expect(wrapper.text()).toContain('互动记录·访前路径')
    expect(wrapper.find('[data-testid="stage-path"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="highlights-metrics"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('访前准备')
    expect(wrapper.text()).toContain('会中协作')
    expect(wrapper.text()).toContain('访后核验与受控回写')
  })

  it('shows empty success when the customer list is empty', async () => {
    const { fetchCustomers, fetchKycGapProfile } = await import('../../api/engagement')
    ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue([])
    ;(fetchKycGapProfile as ReturnType<typeof vi.fn>).mockResolvedValue(emptyGap)
    setActivePinia(createPinia())
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/engagement', name: 'EngagementWorkspace', component: EngagementWorkspace }],
    })
    await router.push('/engagement')
    const wrapper = mount(EngagementWorkspace, { global: { plugins: [router], stubs } })
    await flushPromises()
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/请先选择客户/)
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

  it('shows guidance panel and keeps previsit/meeting actions gated without a journey', async () => {
    const wrapper = await mountWorkspace()
    expect(wrapper.find('[data-testid="guidance-panel"]').exists()).toBe(true)
    expect((wrapper.get('[data-testid="p11-open-previsit"]').element as HTMLButtonElement).disabled).toBe(true)
    expect((wrapper.get('[data-testid="p11-mark-ready"]').element as HTMLButtonElement).disabled).toBe(true)
  })

  it('shows customer select button', async () => {
    const wrapper = await mountWorkspace()
    expect(wrapper.get('[data-testid="p11-select-customer"]').text()).toMatch(/选择客户|切换客户/)
  })

  it('auto-selects a customer and starts the journey from the dedicated button', async () => {
    const { startJourney } = await import('../../api/engagement')
    ;(startJourney as ReturnType<typeof vi.fn>).mockResolvedValue({
      journeyId: 'journey-1',
      customerId: 'c1',
      operatingCaseId: 'case-1',
      phase: 'INSIGHT_ANALYSIS',
      startedAt: '2026-08-26T00:00:00Z',
    })
    const wrapper = await mountWorkspace()
    await wrapper.get('[data-testid="p11-start-journey"]').trigger('click')
    await flushPromises()
    expect(startJourney).toHaveBeenCalledWith('c1')
    expect((wrapper.get('[data-testid="p11-open-previsit"]').element as HTMLButtonElement).disabled).toBe(false)
  })
})
