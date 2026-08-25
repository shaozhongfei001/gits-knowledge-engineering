import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import PrevisitPackView from '../PrevisitPackView.vue'
import type { PrevisitExecutionResponse } from '../../api/engagement'

vi.mock('../../api/engagement', async () => {
  const actual = await vi.importActual<typeof import('../../api/engagement')>('../../api/engagement')
  return {
    ...actual,
    executePrevisit: vi.fn(),
  }
})

const mockPack: PrevisitExecutionResponse = {
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
    visitStrategy: '先对账',
  },
  battleCard: {
    cardId: 'bc1',
    customerName: '企业A',
    visitObjective: '扩产融资专题拜访',
    customerTier: '战略客户',
    riskLevel: '中',
    keyPoints: [],
    productHints: [],
    dontForget: [],
    bottomLine: '不承诺定价',
  },
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

async function mountP14(query: Record<string, string> = { customerId: 'c1', journeyId: 'j1', operatingCaseId: 'oc1' }) {
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/engagement/previsit/pack', name: 'PrevisitPack', component: PrevisitPackView }],
  })
  await router.push({ path: '/engagement/previsit/pack', query })
  const wrapper = mount(PrevisitPackView, { global: { plugins: [router], stubs } })
  await flushPromises()
  return wrapper
}

describe('P14 PrevisitPackView', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it('enters successfully and requires human confirm before executePrevisit', async () => {
    const { executePrevisit } = await import('../../api/engagement')
    ;(executePrevisit as ReturnType<typeof vi.fn>).mockResolvedValue(mockPack)
    const wrapper = await mountP14()
    expect(wrapper.get('[data-testid="p14-previsit-pack"]').text()).toContain('访前包')
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect((wrapper.get('[data-testid="p14-execute"]').element as HTMLButtonElement).disabled).toBe(true)
    expect(executePrevisit as ReturnType<typeof vi.fn>).not.toHaveBeenCalled()
    await wrapper.get('[data-testid="p14-confirm"]').setValue(true)
    await wrapper.get('[data-testid="p14-execute"]').trigger('click')
    await flushPromises()
    expect(executePrevisit as ReturnType<typeof vi.fn>).toHaveBeenCalled()
    expect(wrapper.text()).toContain('r1')
  })

  it('shows empty success when journey context is missing', async () => {
    const wrapper = await mountP14({})
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/暂无|缺对象/)
  })

  it('shows error four-state when executePrevisit fails', async () => {
    const { executePrevisit } = await import('../../api/engagement')
    ;(executePrevisit as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('pack failed'))
    const wrapper = await mountP14()
    await wrapper.get('[data-testid="p14-confirm"]').setValue(true)
    await wrapper.get('[data-testid="p14-execute"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('disables execute when the journey object is missing', async () => {
    const wrapper = await mountP14({})
    expect((wrapper.get('[data-testid="gated-action"]').element as HTMLButtonElement).disabled).toBe(true)
    expect(wrapper.get('[data-testid="disabled-reason"]').text()).toMatch(/原因|解除路径/)
  })
})
