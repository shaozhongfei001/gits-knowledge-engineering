import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../api/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { title: '登录', public: true }
    },
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { title: '客户经营概览' }
    },
    {
      path: '/customers/:id',
      name: 'CustomerOperatingView',
      component: () => import('../views/CustomerOperatingView.vue'),
      meta: { title: '客户经营视图' }
    },
    {
      path: '/journeys/:id',
      name: 'JourneyTimeline',
      component: () => import('../views/JourneyTimeline.vue'),
      meta: { title: '旅程时间线' }
    },
    {
      path: '/reports/:id',
      name: 'ReportDetail',
      component: () => import('../views/ReportDetail.vue'),
      meta: { title: '报告详情' }
    },
    {
      path: '/engagement',
      name: 'EngagementWorkspace',
      component: () => import('../views/EngagementWorkspace.vue'),
      meta: { title: '持续经营工作台' }
    },
    {
      path: '/commitments',
      name: 'CommitmentDashboard',
      component: () => import('../views/CommitmentDashboard.vue'),
      meta: { title: '承诺与任务管理' }
    },
    {
      path: '/external-events',
      name: 'ExternalEventMonitor',
      component: () => import('../views/ExternalEventMonitor.vue'),
      meta: { title: '外部事件监控' }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'GITS'} - 客户经营闭环`

  // 公开页面不需要认证
  if (to.meta.public) {
    next()
    return
  }

  // 未认证时跳转登录页
  if (!isAuthenticated()) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

// 监听401事件，跳转登录页
window.addEventListener('auth:unauthorized', () => {
  router.push({ name: 'Login' })
})

export default router
