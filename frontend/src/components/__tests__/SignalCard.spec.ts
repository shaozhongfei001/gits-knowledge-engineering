import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SignalCard from '../SignalCard.vue'
import type { OpportunitySignal } from '../../api/engagement'

function createSignal(overrides: Partial<OpportunitySignal> = {}): OpportunitySignal {
  return {
    signalId: 'sig-001',
    signalType: 'FINANCING_NEED',
    content: '客户有融资需求',
    sourceType: 'INTERACTION',
    status: 'DETECTED',
    detectedAt: '2025-06-15T10:30:00Z',
    ...overrides,
  }
}

describe('SignalCard', () => {
  it('renders signal type label', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ signalType: 'FINANCING_NEED' }) },
    })
    expect(wrapper.find('.signal-type-badge').text()).toBe('融资需求')
  })

  it('renders signal status label', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ status: 'CONFIRMED' }) },
    })
    expect(wrapper.find('.signal-status-badge').text()).toBe('已确认')
  })

  it('renders signal content', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ content: '客户近期有大额融资计划' }) },
    })
    expect(wrapper.find('.signal-content').text()).toBe('客户近期有大额融资计划')
  })

  it('renders confidence when present', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ confidence: 0.85 }) },
    })
    expect(wrapper.find('.confidence').text()).toContain('85%')
  })

  it('hides confidence when not provided', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal() },
    })
    // confidence is undefined by default in createSignal
    expect(wrapper.find('.confidence').exists()).toBe(false)
  })

  it('formats detectedAt time', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ detectedAt: '2025-06-15T10:30:00Z' }) },
    })
    const timeEl = wrapper.find('.detected-at')
    expect(timeEl.exists()).toBe(true)
    expect(timeEl.text().length).toBeGreaterThan(0)
  })

  it('applies status-specific CSS class on root element', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ status: 'CONFIRMED' }) },
    })
    expect(wrapper.find('.signal-card').classes()).toContain('signal-confirmed')
  })

  it('applies detected status CSS class', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ status: 'DETECTED' }) },
    })
    expect(wrapper.find('.signal-card').classes()).toContain('signal-detected')
  })

  it('applies dismissed status CSS class', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ status: 'DISMISSED' }) },
    })
    expect(wrapper.find('.signal-card').classes()).toContain('signal-dismissed')
  })

  it('applies converted status CSS class', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ status: 'CONVERTED' }) },
    })
    expect(wrapper.find('.signal-card').classes()).toContain('signal-converted')
  })

  it('renders PRODUCT_OPPORTUNITY type label', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ signalType: 'PRODUCT_OPPORTUNITY' }) },
    })
    expect(wrapper.find('.signal-type-badge').text()).toBe('产品机会')
  })

  it('renders RELATIONSHIP_CHANGE type label', () => {
    const wrapper = mount(SignalCard, {
      props: { signal: createSignal({ signalType: 'RELATIONSHIP_CHANGE' }) },
    })
    expect(wrapper.find('.signal-type-badge').text()).toBe('关系变化')
  })
})
