import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ProposalWizardView from '../ProposalWizardView.vue'
import { loadProposalWizardShell } from '../../composables/proposalDegrade'

vi.mock('../../composables/proposalDegrade', async () => {
  const actual = await vi.importActual<typeof import('../../composables/proposalDegrade')>(
    '../../composables/proposalDegrade',
  )
  return {
    ...actual,
    loadProposalWizardShell: vi.fn(actual.loadProposalWizardShell),
  }
})

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

async function mountP24() {
  setActivePinia(createPinia())
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/proposals/new', name: 'ProposalWizard', component: ProposalWizardView },
      { path: '/proposals/:id', name: 'ProposalRecord', component: { template: '<div />' } },
    ],
  })
  await router.push('/proposals/new')
  const wrapper = mount(ProposalWizardView, { global: { plugins: [router], stubs } })
  await flushPromises()
  return wrapper
}

describe('P24 ProposalWizardView C2 degrade', () => {
  beforeEach(() => {
    sessionStorage.clear()
    vi.mocked(loadProposalWizardShell).mockReset()
    vi.mocked(loadProposalWizardShell).mockResolvedValue({
      informal: true,
      emptyDraft: true,
      degradeLabel: '非正式 / C2 降级',
    })
  })

  it('enters successfully as an empty informal wizard, not /proposals/:id', async () => {
    const wrapper = await mountP24()
    expect(wrapper.get('[data-testid="p24-proposal-wizard"]').text()).toContain('非正式建议书（C2 降级）')
    expect(wrapper.find('[data-testid="success-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('空草稿')
    expect(wrapper.text()).toContain('非正式 / C2 降级')
    expect(wrapper.text()).not.toContain('NEED-826')
  })

  it('shows error four-state when the wizard shell loader fails', async () => {
    vi.mocked(loadProposalWizardShell).mockRejectedValueOnce(new Error('upstream'))
    const wrapper = await mountP24()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
  })

  it('keeps 保存并继续 disabled', async () => {
    const wrapper = await mountP24()
    expect(wrapper.text()).toContain('保存并继续')
    expect((wrapper.get('[data-testid="gated-action"]').element as HTMLButtonElement).disabled).toBe(true)
    expect(wrapper.get('[data-testid="disabled-reason"]').text()).toMatch(/原因|解除路径/)
  })
})
