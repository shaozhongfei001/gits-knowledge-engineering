import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import CustomerGroupView from '../CustomerGroupView.vue'
import type { Customer } from '../../api/engagement'

vi.mock('../../api/engagement', async () => {
  const actual = await vi.importActual<typeof import('../../api/engagement')>('../../api/engagement')
  return {
    ...actual,
    fetchCustomer: vi.fn(),
  }
})

const mockCustomer: Customer = {
  customerId: 'c1',
  customerName: '企业A',
  groupFlag: true,
  relationshipSummary: '集团核心成员',
  listedStatus: 'LISTED',
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

async function mountP05() {
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/customers/:id', name: 'CustomerOperatingView', component: { template: '<div/>' } },
      { path: '/customers/:id/group', name: 'CustomerGroupView', component: CustomerGroupView },
      { path: '/customers/:id/funds', name: 'CustomerFundsView', component: { template: '<div/>' } },
      { path: '/customers/:id/parties', name: 'CustomerPartiesView', component: { template: '<div/>' } },
    ],
  })
  await router.push('/customers/c1/group')
  const wrapper = mount(CustomerGroupView, { global: { plugins: [router], stubs } })
  await flushPromises()
  return wrapper
}

describe('P05 CustomerGroupView', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  it('enters successfully and shows existing group fields only', async () => {
    const { fetchCustomer } = await import('../../api/engagement')
    ;(fetchCustomer as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomer)
    const wrapper = await mountP05()
    expect(wrapper.get('[data-testid="p05-group"]').text()).toContain('客户 Account')
    expect(wrapper.text()).toContain('企业A')
    expect(wrapper.text()).toContain('集团核心成员')
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
  })

  it('shows empty success when group fields are absent', async () => {
    const { fetchCustomer } = await import('../../api/engagement')
    ;(fetchCustomer as ReturnType<typeof vi.fn>).mockResolvedValue({
      customerId: 'c1',
      customerName: '企业A',
    })
    const wrapper = await mountP05()
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/暂无|非集团|无集团/)
  })

  it('shows error four-state when fetchCustomer fails', async () => {
    const { fetchCustomer } = await import('../../api/engagement')
    ;(fetchCustomer as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('forbidden'))
    const wrapper = await mountP05()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('disables 发起核验 with C2 reason', async () => {
    const { fetchCustomer } = await import('../../api/engagement')
    ;(fetchCustomer as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomer)
    const wrapper = await mountP05()
    expect(wrapper.text()).toContain('发起核验')
    expect((wrapper.get('[data-testid="gated-action"]').element as HTMLButtonElement).disabled).toBe(true)
    expect(wrapper.get('[data-testid="disabled-reason"]').text()).toMatch(/C2|合同|解除路径/)
  })
})
