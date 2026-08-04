import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RiskBadge from '../RiskBadge.vue'

describe('RiskBadge', () => {
  it('renders HIGH risk level with correct class and label', () => {
    const wrapper = mount(RiskBadge, { props: { level: 'HIGH' } })
    expect(wrapper.text()).toContain('高风险')
    expect(wrapper.find('.risk-badge').classes()).toContain('risk-high')
  })

  it('renders MEDIUM risk level with correct class and label', () => {
    const wrapper = mount(RiskBadge, { props: { level: 'MEDIUM' } })
    expect(wrapper.text()).toContain('中风险')
    expect(wrapper.find('.risk-badge').classes()).toContain('risk-medium')
  })

  it('renders LOW risk level with correct class and label', () => {
    const wrapper = mount(RiskBadge, { props: { level: 'LOW' } })
    expect(wrapper.text()).toContain('低风险')
    expect(wrapper.find('.risk-badge').classes()).toContain('risk-low')
  })

  it('renders UNEVALUATED (no level) with risk-unknown class and default label', () => {
    const wrapper = mount(RiskBadge, { props: {} })
    expect(wrapper.text()).toContain('未评估')
    expect(wrapper.find('.risk-badge').classes()).toContain('risk-unknown')
  })

  it('renders unknown level string with risk-unknown class and raw value', () => {
    const wrapper = mount(RiskBadge, { props: { level: 'UNKNOWN_VALUE' } })
    expect(wrapper.text()).toContain('UNKNOWN_VALUE')
    expect(wrapper.find('.risk-badge').classes()).toContain('risk-unknown')
  })

  it('applies risk-badge base class to all variants', () => {
    const levels = ['HIGH', 'MEDIUM', 'LOW'] as const
    for (const level of levels) {
      const wrapper = mount(RiskBadge, { props: { level } })
      expect(wrapper.find('.risk-badge').exists()).toBe(true)
    }
  })
})
