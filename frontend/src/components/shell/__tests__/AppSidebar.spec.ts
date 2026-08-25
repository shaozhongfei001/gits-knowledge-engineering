import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import AppSidebar from '../AppSidebar.vue'
import { SHELL_NAV_GROUPS } from '../../../layouts/navConfig'

    const naiveStubs = {
  NMenu: {
    props: ['mode', 'options', 'value'],
    template: '<div class="n-menu-stub" data-testid="shell-side-menu" :data-mode="mode"><slot /></div>',
  },
  'n-menu': {
    props: ['mode', 'options', 'value'],
    template: '<div class="n-menu-stub" data-testid="shell-side-menu" :data-mode="mode"><slot /></div>',
  },
}

async function mountSidebar() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/workbench', name: 'Workbench', component: { template: '<div/>' } },
      { path: '/accounts', name: 'AccountsHome', component: { template: '<div/>' } },
      { path: '/accounts/portfolio', name: 'PortfolioBoard', component: { template: '<div/>' } },
      { path: '/commitments', name: 'CommitmentDashboard', component: { template: '<div/>' } },
    ],
  })
  await router.push('/workbench')
  await router.isReady()
  return mount(AppSidebar, { global: { plugins: [router], stubs: naiveStubs } })
}

describe('AppSidebar', () => {
  it('renders V3.2 nav groups including mobile degrade', async () => {
    const wrapper = await mountSidebar()
    expect(wrapper.text()).toContain('日常作业')
    expect(wrapper.text()).toContain('客户经营')
    expect(wrapper.text()).toContain('方案与交付')
    expect(wrapper.text()).toContain('知识与治理')
    expect(wrapper.text()).toContain('移动端（降级）')
    expect(SHELL_NAV_GROUPS.map(group => group.label)).toEqual([
      '日常作业',
      '客户经营',
      '方案与交付',
      '知识与治理',
      '移动端（降级）',
    ])
  })

  it('uses vertical side menu rather than a horizontal header menu', async () => {
    const wrapper = await mountSidebar()
    expect(wrapper.find('[data-testid="shell-sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="shell-side-menu"]').exists()).toBe(true)
    expect(wrapper.find('.header-menu').exists()).toBe(false)
  })

  it('keeps P30 enabled items and adds P08/P10 while P04 enters via customer list', () => {
    const accounts = SHELL_NAV_GROUPS.find(group => group.key === 'accounts')
    expect(accounts).toBeTruthy()
    const tos = accounts!.children.map(item => item.to)
    expect(tos).toContain('/accounts')
    expect(tos).toContain('/accounts/portfolio')
    expect(tos).toContain('/engagement')
    expect(tos).toContain('/external-events')
    expect(tos).toContain('/signals')
    expect(tos).toContain('/engagements')
    expect(tos).toContain('/engagement/previsit/gaps')
    expect(tos).toContain('/engagement/previsit/evidence')
    expect(tos).toContain('/engagement/previsit/pack')
    expect(tos).toContain('/engagement/postvisit')
    expect(tos).toContain('/engagement/crm-writeback')
    expect(accounts!.children.some(item => item.to === '/accounts')).toBe(true)
  })

  it('adds degraded needs and enables 服务建议书 at /proposals while keeping /commitments in 日常作业', () => {
    const delivery = SHELL_NAV_GROUPS.find(group => group.key === 'delivery')
    expect(delivery).toBeTruthy()
    expect(delivery!.children.some(item => item.to === '/needs' && item.label.includes('需求/机会'))).toBe(true)
    const proposal = delivery!.children.find(item => item.label === '服务建议书')
    expect(proposal?.disabled).toBeFalsy()
    expect(proposal?.to).toBe('/proposals')
    expect(proposal?.routeName).toBe('ProposalsHome')
    const daily = SHELL_NAV_GROUPS.find(group => group.key === 'daily')
    expect(daily!.children.some(item => item.to === '/commitments' && item.label === '任务与承诺')).toBe(true)
  })

  it('enables 审批工作中心 and Claim / Evidence 中心 without changing /commitments', () => {
    const delivery = SHELL_NAV_GROUPS.find(group => group.key === 'delivery')
    const approval = delivery!.children.find(item => item.label === '审批工作中心')
    expect(approval?.disabled).toBeFalsy()
    expect(approval?.to).toBe('/approvals')
    const governance = SHELL_NAV_GROUPS.find(group => group.key === 'governance')
    const claims = governance!.children.find(item => item.label === 'Claim / Evidence 中心')
    expect(claims?.disabled).toBeFalsy()
    expect(claims?.to).toBe('/claims')
    const daily = SHELL_NAV_GROUPS.find(group => group.key === 'daily')
    expect(daily!.children.some(item => item.to === '/commitments')).toBe(true)
    expect(governance!.children.some(item => item.to === '/knowledge-map')).toBe(true)
    expect(governance!.children.some(item => item.to === '/audit-trace')).toBe(true)
  })

  it('adds 移动端（降级） P41-P44 without changing /commitments', () => {
    const mobile = SHELL_NAV_GROUPS.find(group => group.key === 'mobile')
    expect(mobile?.label).toBe('移动端（降级）')
    const tos = mobile!.children.map(item => item.to)
    expect(tos).toEqual(['/m/today', '/m/previsit', '/m/notes', '/m/checkout'])
    const daily = SHELL_NAV_GROUPS.find(group => group.key === 'daily')
    expect(daily!.children.some(item => item.to === '/workbench')).toBe(true)
    expect(daily!.children.some(item => item.to === '/commitments')).toBe(true)
    const accounts = SHELL_NAV_GROUPS.find(group => group.key === 'accounts')
    expect(accounts!.children.some(item => item.to === '/engagement')).toBe(true)
    const delivery = SHELL_NAV_GROUPS.find(group => group.key === 'delivery')
    expect(delivery!.children.some(item => item.to === '/in-meeting')).toBe(true)
    expect(delivery!.children.some(item => item.to === '/approvals')).toBe(true)
    const governance = SHELL_NAV_GROUPS.find(group => group.key === 'governance')
    expect(governance!.children.some(item => item.to === '/claims')).toBe(true)
  })
})

