import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PhaseIndicator from '../PhaseIndicator.vue'

describe('PhaseIndicator', () => {
  it('renders all 6 phases', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'KYC_COLLECT' } })
    const steps = wrapper.findAll('.phase-step')
    expect(steps).toHaveLength(6)
  })

  it('renders phase labels in correct order', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'KYC_COLLECT' } })
    const labels = wrapper.findAll('.phase-label').map(el => el.text())
    expect(labels).toEqual([
      'KYC信息采集', '洞察分析', '产品匹配', '访前准备', '访后复盘', '已完成'
    ])
  })

  it('marks phases before current as completed', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'PRODUCT_MATCHING' } })
    const steps = wrapper.findAll('.phase-step')
    // KYC_COLLECT and INSIGHT_ANALYSIS are completed
    expect(steps[0].classes()).toContain('phase-completed')
    expect(steps[1].classes()).toContain('phase-completed')
    // PRODUCT_MATCHING is active
    expect(steps[2].classes()).toContain('phase-active')
  })

  it('marks current phase as active', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'PREVISIT_PREP' } })
    const steps = wrapper.findAll('.phase-step')
    expect(steps[3].classes()).toContain('phase-active')
    expect(steps[3].classes()).not.toContain('phase-completed')
    expect(steps[3].classes()).not.toContain('phase-pending')
  })

  it('marks phases after current as pending', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'INSIGHT_ANALYSIS' } })
    const steps = wrapper.findAll('.phase-step')
    expect(steps[2].classes()).toContain('phase-pending')
    expect(steps[3].classes()).toContain('phase-pending')
    expect(steps[4].classes()).toContain('phase-pending')
    expect(steps[5].classes()).toContain('phase-pending')
  })

  it('shows checkmark for completed phases', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'POSTVISIT_REVIEW' } })
    const steps = wrapper.findAll('.phase-step')
    // Completed phases should have a check mark
    expect(steps[0].find('.check').exists()).toBe(true)
    expect(steps[1].find('.check').exists()).toBe(true)
    expect(steps[2].find('.check').exists()).toBe(true)
    expect(steps[3].find('.check').exists()).toBe(true)
    // Current phase should show step number, not check
    expect(steps[4].find('.step-number').exists()).toBe(true)
  })

  it('shows step numbers for non-completed phases', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'KYC_COLLECT' } })
    const numbers = wrapper.findAll('.step-number').map(el => el.text())
    // No completed phases, all show step numbers 1-6
    expect(numbers).toEqual(['1', '2', '3', '4', '5', '6'])
  })

  it('all phases completed when current is COMPLETED', () => {
    const wrapper = mount(PhaseIndicator, { props: { current: 'COMPLETED' } })
    const steps = wrapper.findAll('.phase-step')
    // All phases before COMPLETED should be completed
    expect(steps[0].classes()).toContain('phase-completed')
    expect(steps[1].classes()).toContain('phase-completed')
    expect(steps[2].classes()).toContain('phase-completed')
    expect(steps[3].classes()).toContain('phase-completed')
    expect(steps[4].classes()).toContain('phase-completed')
    // COMPLETED itself is active
    expect(steps[5].classes()).toContain('phase-active')
  })
})
