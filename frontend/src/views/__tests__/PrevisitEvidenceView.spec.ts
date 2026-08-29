import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import PrevisitEvidenceView from '../PrevisitEvidenceView.vue'
import type { PreparedPrevisitResponse } from '../../api/engagement'

vi.mock('../../api/engagement', async () => {
  const actual = await vi.importActual<typeof import('../../api/engagement')>('../../api/engagement')
  return {
    ...actual,
    preparePrevisit: vi.fn(),
    executeSupplyChainGraph: vi.fn(),
  }
})

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

const mockPrepared: PreparedPrevisitResponse = {
  outreachScript: {
    scriptId: 'out-1',
    customerId: 'c1',
    rmId: 'rm1',
    operatingCaseId: 'oc1',
    journeyId: 'j1',
    channel: 'EMAIL',
    objective: '确认扩产节奏',
    openingLine: '开场',
    talkingPoints: [],
    riskReminders: [],
    closingLine: '结束',
    followUpAction: '预约会面',
    createdAt: '2026-08-25T00:00:00Z',
  },
  meetingScript: {
    scriptId: 'meet-1',
    customerId: 'c1',
    rmId: 'rm1',
    operatingCaseId: 'oc1',
    journeyId: 'j1',
    meetingObjective: '扩产融资专题',
    previsitSummary: '摘要',
    agendaItems: [],
    kycQuestions: [],
    productDiscussions: [],
    riskPoints: [],
    closingSummary: '收口',
    createdAt: '2026-08-25T00:00:00Z',
  },
  previsitReport: {
    reportId: 'r1',
    customerId: 'c1',
    customerName: '企业A',
    rmName: '张经理',
    visitObjective: '扩产融资专题拜访',
    customerOverview: {
      industry: '制造业',
      enterpriseScale: '大型',
      customerTier: '战略客户',
      registeredCapitalCny: 1,
      riskLevel: '中',
      relationshipSummary: '长期合作',
    },
    kycGapSummary: { knownItems: [], partialKnownItems: [], unknownItems: [], priorityQuestions: [] },
    productSchemes: [],
    keyQuestions: [],
    riskReminders: [],
    visitStrategy: '先对账再谈方案',
  },
  battleCard: {
    cardId: 'bc1',
    customerName: '企业A',
    visitObjective: '扩产融资专题拜访',
    customerTier: '战略客户',
    riskLevel: '中',
    keyPoints: ['资金缺口'],
    productHints: [],
    dontForget: [],
    bottomLine: '不承诺定价',
  },
  assemblyTrace: [{ phase: 'dkws', status: 'ok', message: 'hit', kiId: 'KI-009' }],
  skillSections: [],
}

const stubs = {
  NSpin: { template: '<div class="n-spin" />' },
  NResult: { template: '<div class="n-result"><slot name="footer" /></div>' },
  NButton: {
    props: ['disabled'],
    template: '<button class="n-button" :disabled="disabled"><slot /></button>',
  },
  NEmpty: { template: '<div class="n-empty" />' },
  NTooltip: { template: '<div><slot name="trigger" /><slot /></div>' },
}

async function mountP13(query: Record<string, string> = {
  customerId: 'c1',
  journeyId: 'j1',
  operatingCaseId: 'oc1',
}) {
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/engagement/previsit/evidence', name: 'PrevisitEvidence', component: PrevisitEvidenceView }],
  })
  await router.push({ path: '/engagement/previsit/evidence', query })
  const wrapper = mount(PrevisitEvidenceView, { global: { plugins: [router], stubs } })
  await flushPromises()
  return wrapper
}

describe('P13 PrevisitEvidenceView', () => {
  beforeEach(() => {
    sessionStorage.clear()
    vi.clearAllMocks()
  })

  it('does not auto-trigger KERT on mount; shows empty guide', async () => {
    const { preparePrevisit } = await import('../../api/engagement')
    const wrapper = await mountP13()
    expect(preparePrevisit as ReturnType<typeof vi.fn>).not.toHaveBeenCalled()
    expect(wrapper.find('[data-testid="p13-empty"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/生成访前包/)
  })

  it('generates pack via store (KERT entry) and shows sources + trace', async () => {
    const { preparePrevisit, executeSupplyChainGraph } = await import('../../api/engagement')
    ;(preparePrevisit as ReturnType<typeof vi.fn>).mockResolvedValue(mockPrepared)
    ;(executeSupplyChainGraph as ReturnType<typeof vi.fn>).mockResolvedValue({
      requestId: 'SCG-1',
      customerId: 'c1',
      result: { nodes: [], edges: [] },
    })
    const wrapper = await mountP13()
    await wrapper.get('[data-testid="p13-generate-pack"]').trigger('click')
    await flushPromises()
    expect(preparePrevisit as ReturnType<typeof vi.fn>).toHaveBeenCalled()
    expect(wrapper.text()).toContain('out-1')
    expect(wrapper.text()).toContain('装配轨迹')
    expect(wrapper.find('[data-testid="p13-source-list"]').exists()).toBe(true)
  })

  it('shows empty success when context is missing', async () => {
    const wrapper = await mountP13({})
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/尚未执行一键访前/)
  })

  it('shows error four-state when preparePrevisit fails', async () => {
    const { preparePrevisit, executeSupplyChainGraph } = await import('../../api/engagement')
    ;(preparePrevisit as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('timeout'))
    ;(executeSupplyChainGraph as ReturnType<typeof vi.fn>).mockResolvedValue({
      requestId: 'SCG-1',
      customerId: 'c1',
      result: { nodes: [], edges: [] },
    })
    const wrapper = await mountP13()
    await wrapper.get('[data-testid="p13-generate-pack"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('disables generating unsourced conclusions', async () => {
    const wrapper = await mountP13()
    expect((wrapper.get('[data-testid="gated-action"]').element as HTMLButtonElement).disabled).toBe(true)
    expect(wrapper.get('[data-testid="disabled-reason"]').text()).toMatch(/原因|解除路径/)
  })
})
