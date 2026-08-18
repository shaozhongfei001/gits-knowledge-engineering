import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import Dashboard from '../Dashboard.vue'
import type { Customer } from '../../api/engagement'

vi.mock('../../api/engagement', () => ({
  fetchCustomers: vi.fn(),
}))

vi.mock('../../api/v11', () => ({
  fetchHumanGates: vi.fn(),
}))

const mockCustomers: Customer[] = [
  {
    customerId: 'c1', customerName: '企业A', industry: 'MANUFACTURING',
    enterpriseScale: 'LARGE', customerTier: 'STRATEGIC', riskLevel: 'HIGH', coreTags: ['龙头'],
  },
  {
    customerId: 'c2', customerName: '企业B', industry: 'FINANCE',
    enterpriseScale: 'MEDIUM', customerTier: 'KEY', riskLevel: 'LOW',
  },
  {
    customerId: 'c3', customerName: '企业C', industry: 'TECHNOLOGY',
    enterpriseScale: 'SMALL', customerTier: 'STRATEGIC', riskLevel: 'MEDIUM',
  },
]

const stubs = {
  NSpin: { template: '<div class="n-spin" />' },
  NResult: { template: '<div class="n-result" />' },
  NButton: { template: '<button class="n-button"><slot /></button>' },
  NEmpty: { template: '<div class="n-empty" />' },
  NCard: { template: '<div class="n-card"><slot /></div>' },
  NTag: { template: '<span class="n-tag"><slot /></span>' },
  CustomerCard: { template: '<div class="customer-card-stub" @click="$emit(\'click\')">{{ $attrs.customer?.customerName }}</div>' },
  RiskBadge: true,
}

async function mountDashboard() {
  const { fetchCustomers } = await import('../../api/engagement')
  ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomers)

  const { fetchHumanGates } = await import('../../api/v11')
  ;(fetchHumanGates as ReturnType<typeof vi.fn>).mockResolvedValue([])

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'Dashboard', component: Dashboard },
      { path: '/customers/:id', name: 'CustomerOperatingView', component: { template: '<div/>' } },
    ],
  })

  const wrapper = mount(Dashboard, { global: { plugins: [router], stubs } })
  await router.isReady()
  await flushPromises()
  return wrapper
}

describe('Dashboard', () => {
  it('renders page title', async () => {
    const wrapper = await mountDashboard()
    expect(wrapper.find('h1').text()).toBe('客户经营概览')
  })

  it('renders subtitle', async () => {
    const wrapper = await mountDashboard()
    expect(wrapper.find('.subtitle').text()).toBe('管理客户关系，洞察业务机会')
  })

  it('renders customer cards after loading', async () => {
    const wrapper = await mountDashboard()
    const cards = wrapper.findAll('.customer-card-stub')
    expect(cards).toHaveLength(3)
  })

  it('displays total customer count in stats bar', async () => {
    const wrapper = await mountDashboard()
    const statValues = wrapper.findAll('.stat-value')
    expect(statValues[0].text()).toBe('3')
  })

  it('displays high risk customer count', async () => {
    const wrapper = await mountDashboard()
    const statValues = wrapper.findAll('.stat-value')
    expect(statValues[1].text()).toBe('1')
  })

  it('displays strategic customer count', async () => {
    const wrapper = await mountDashboard()
    const statValues = wrapper.findAll('.stat-value')
    expect(statValues[2].text()).toBe('2')
  })

  it('shows loading state initially', async () => {
    const { fetchCustomers } = await import('../../api/engagement')
    ;(fetchCustomers as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}))

    const { fetchHumanGates } = await import('../../api/v11')
    ;(fetchHumanGates as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}))

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', name: 'Dashboard', component: Dashboard },
        { path: '/customers/:id', name: 'CustomerOperatingView', component: { template: '<div/>' } },
      ],
    })

    const wrapper = mount(Dashboard, { global: { plugins: [router], stubs } })
    await router.isReady()
    expect(wrapper.find('.loading-state').exists()).toBe(true)
  })

  it('shows error state on API failure', async () => {
    const { fetchCustomers } = await import('../../api/engagement')
    ;(fetchCustomers as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Network error'))

    const { fetchHumanGates } = await import('../../api/v11')
    ;(fetchHumanGates as ReturnType<typeof vi.fn>).mockResolvedValue([])

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', name: 'Dashboard', component: Dashboard },
        { path: '/customers/:id', name: 'CustomerOperatingView', component: { template: '<div/>' } },
      ],
    })

    const wrapper = mount(Dashboard, { global: { plugins: [router], stubs } })
    await router.isReady()
    await flushPromises()
    expect(wrapper.find('.error-state').exists()).toBe(true)
  })
})
