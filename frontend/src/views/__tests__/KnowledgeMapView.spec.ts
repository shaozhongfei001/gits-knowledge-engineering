import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import KnowledgeMapView from '../KnowledgeMapView.vue'
import type { KnowledgeElement } from '../../api/knowledge'

vi.mock('../../api/knowledge', async () => {
  const actual = await vi.importActual<typeof import('../../api/knowledge')>('../../api/knowledge')
  return {
    ...actual,
    fetchKnowledgeMap: vi.fn(),
  }
})

const element: KnowledgeElement = {
  schemaVersion: '1',
  elementId: 'KE-1',
  name: '适用边界',
  kind: 'K-Type-P',
  knowledgeItemId: 'KI-009',
  content: '只读知识要素',
  source: { sourceRef: 'CTR-KELEM-001', authority: 'AUTHORITATIVE' },
  status: 'ACTIVE',
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

async function mountP38() {
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/knowledge-map', name: 'KnowledgeMapView', component: KnowledgeMapView }],
  })
  await router.push('/knowledge-map')
  const wrapper = mount(KnowledgeMapView, { global: { plugins: [router], stubs } })
  await flushPromises()
  return wrapper
}

describe('P38 KnowledgeMapView C2 upgrade', () => {
  beforeEach(async () => {
    sessionStorage.clear()
    const { fetchKnowledgeMap } = await import('../../api/knowledge')
    ;(fetchKnowledgeMap as ReturnType<typeof vi.fn>).mockReset()
  })

  it('enters successfully and keeps KE read-only', async () => {
    const { fetchKnowledgeMap } = await import('../../api/knowledge')
    ;(fetchKnowledgeMap as ReturnType<typeof vi.fn>).mockResolvedValue({ 'KI-009': [element] })
    const wrapper = await mountP38()
    expect(wrapper.get('[data-testid="p38-knowledge-map"]').text()).toContain('知识卡与产品适用边界')
    expect(wrapper.text()).toContain('知识要素 KE（只读）')
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('KE-1')
    expect(wrapper.text()).toContain('只读知识要素')
    expect(wrapper.text()).not.toContain('NEED-826')
  })

  it('shows empty success when the map has no items', async () => {
    const { fetchKnowledgeMap } = await import('../../api/knowledge')
    ;(fetchKnowledgeMap as ReturnType<typeof vi.fn>).mockResolvedValue({})
    const wrapper = await mountP38()
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/暂无/)
  })

  it('shows error four-state when fetchKnowledgeMap fails', async () => {
    const { fetchKnowledgeMap } = await import('../../api/knowledge')
    ;(fetchKnowledgeMap as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('map down'))
    const wrapper = await mountP38()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('keeps 比较产品 and 反馈知识 disabled', async () => {
    const { fetchKnowledgeMap } = await import('../../api/knowledge')
    ;(fetchKnowledgeMap as ReturnType<typeof vi.fn>).mockResolvedValue({})
    const wrapper = await mountP38()
    expect(wrapper.text()).toContain('比较产品')
    expect(wrapper.text()).toContain('反馈知识')
    const buttons = wrapper.findAll('[data-testid="gated-action"]')
    expect(buttons.length).toBe(2)
    for (const button of buttons) {
      expect((button.element as HTMLButtonElement).disabled).toBe(true)
    }
    expect(wrapper.get('[data-testid="disabled-reason"]').text()).toMatch(/原因|解除路径/)
  })
})
