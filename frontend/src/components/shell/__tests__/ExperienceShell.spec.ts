import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ExperienceShell from '../../../layouts/ExperienceShell.vue'

const stubs = {
  NLayout: { template: '<div class="n-layout"><slot /></div>' },
  NLayoutSider: { template: '<aside class="n-layout-sider"><slot /></aside>' },
  NLayoutHeader: { template: '<header class="n-layout-header"><slot /></header>' },
  NLayoutContent: { template: '<main class="n-layout-content"><slot /></main>' },
  NSwitch: { template: '<button class="n-switch" />' },
  NMenu: { template: '<div class="n-menu" data-mode="vertical" />' },
  NTag: { template: '<span />' },
  NButton: { template: '<button><slot /></button>' },
}

describe('ExperienceShell', () => {
  it('brands GITS Bank and keeps a left grouped shell', async () => {
    setActivePinia(createPinia())
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/workbench', name: 'Workbench', meta: { title: '我的客户经营', objectType: '客户经营应用' }, component: { template: '<div/>' } }],
    })
    await router.push('/workbench')
    const wrapper = mount(ExperienceShell, {
      props: { isDark: false },
      slots: { default: '<div>child</div>' },
      global: { plugins: [router], stubs },
    })
    expect(wrapper.get('[data-testid="shell-brand"]').text()).toContain('GITS Bank')
    expect(wrapper.find('[data-testid="shell-sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="workspace-tabs"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('日常作业')
  })
})
