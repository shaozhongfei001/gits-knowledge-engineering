import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import EngagementWorkspace from '../EngagementWorkspace.vue'
import type { Customer } from '../../api/engagement'

vi.mock('../../api/engagement', () => ({
  fetchCustomers: vi.fn(),
  generateOutreachScript: vi.fn(),
  generateMeetingScript: vi.fn(),
  executePrevisit: vi.fn(),
  executePostvisit: vi.fn(),
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
  NButton: { template: '<button class="n-button"><slot /></button>' },
  NModal: true,
  NInput: { template: '<input class="n-input" />' },
  NEmpty: { template: '<div class="n-empty" />' },
  RiskBadge: { template: '<span class="risk-badge">{{ $attrs.level }}</span>' },
}

async function mountWorkspace() {
  const { fetchCustomers } = await import('../../api/engagement')
  ;(fetchCustomers as ReturnType<typeof vi.fn>).mockResolvedValue(mockCustomers)

  const wrapper = mount(EngagementWorkspace, {
    global: { stubs },
  })
  await flushPromises()
  return wrapper
}

describe('EngagementWorkspace', () => {
  it('renders page title', async () => {
    const wrapper = await mountWorkspace()
    expect(wrapper.find('h1').text()).toBe('持续经营工作台')
  })

  it('renders 5 process steps in the flow', async () => {
    const wrapper = await mountWorkspace()
    const steps = wrapper.findAll('.process-step')
    expect(steps).toHaveLength(5)
  })

  it('renders process step names', async () => {
    const wrapper = await mountWorkspace()
    const names = wrapper.findAll('.step-name').map(el => el.text())
    expect(names).toEqual(['KYC采集', '洞察分析', '产品匹配', '访前准备', '访后复盘'])
  })

  it('renders 4 action cards', async () => {
    const wrapper = await mountWorkspace()
    const actions = wrapper.findAll('.action-card')
    expect(actions).toHaveLength(4)
  })

  it('renders action card titles', async () => {
    const wrapper = await mountWorkspace()
    const titles = wrapper.findAll('.action-title').map(el => el.text())
    expect(titles).toContain('生成外联脚本')
    expect(titles).toContain('生成会面脚本')
    expect(titles).toContain('执行访前报告')
    expect(titles).toContain('执行访后分析')
  })

  it('sets showCustomerSelect to true when action card is clicked', async () => {
    const wrapper = await mountWorkspace()
    const cards = wrapper.findAll('.action-card')
    await cards[0].trigger('click')
    await flushPromises()
    expect((wrapper.vm as any).showCustomerSelect).toBe(true)
  })

  it('sets pendingAction when action card is clicked', async () => {
    const wrapper = await mountWorkspace()
    const cards = wrapper.findAll('.action-card')
    await cards[0].trigger('click')
    await flushPromises()
    expect((wrapper.vm as any).pendingAction).toBe('outreach')
  })

  it('marks current step as active', async () => {
    const wrapper = await mountWorkspace()
    const steps = wrapper.findAll('.process-step')
    expect(steps[0].classes()).toContain('step-active')
  })

  it('marks steps after current as pending', async () => {
    const wrapper = await mountWorkspace()
    const steps = wrapper.findAll('.process-step')
    expect(steps[1].classes()).toContain('step-pending')
    expect(steps[2].classes()).toContain('step-pending')
  })

  it('calls generateOutreachScript when selectCustomer is invoked for outreach', async () => {
    const { generateOutreachScript } = await import('../../api/engagement')
    ;(generateOutreachScript as ReturnType<typeof vi.fn>).mockResolvedValue({
      scriptId: 's1', scriptType: 'OUTREACH', customerId: 'c1', content: '外联脚本内容', generatedAt: '2025-01-01',
    })

    const wrapper = await mountWorkspace()
    // Set up component state for outreach action
    ;(wrapper.vm as any).pendingAction = 'outreach'
    await (wrapper.vm as any).selectCustomer(mockCustomers[0])
    await flushPromises()

    expect(generateOutreachScript).toHaveBeenCalledWith('c1')
  })

  it('calls generateMeetingScript when selectCustomer is invoked for meeting', async () => {
    const { generateMeetingScript } = await import('../../api/engagement')
    ;(generateMeetingScript as ReturnType<typeof vi.fn>).mockResolvedValue({
      scriptId: 's2', scriptType: 'MEETING', customerId: 'c1', content: '会面脚本内容', generatedAt: '2025-01-01',
    })

    const wrapper = await mountWorkspace()
    ;(wrapper.vm as any).pendingAction = 'meeting'
    await (wrapper.vm as any).selectCustomer(mockCustomers[0])
    await flushPromises()

    expect(generateMeetingScript).toHaveBeenCalledWith('c1')
  })
})
