<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { SHELL_NAV_GROUPS } from '../../layouts/navConfig'

const router = useRouter()
const route = useRoute()

const menuOptions = computed<MenuOption[]>(() =>
  SHELL_NAV_GROUPS.map(group => ({
    type: 'group',
    label: group.label,
    key: `group-${group.key}`,
    children: group.children.map(item => ({
      label: item.label,
      key: item.to ?? item.key,
      disabled: item.disabled === true,
    })),
  })),
)

const activeKey = computed(() => {
  if (route.path === '/' || route.path === '/workbench') {
    return '/workbench'
  }
  if (route.path.startsWith('/accounts/portfolio')) {
    return '/accounts/portfolio'
  }
  if (route.path.startsWith('/accounts')) {
    return '/accounts'
  }
  if (route.path.startsWith('/signals')) {
    return '/signals'
  }
  if (route.path.startsWith('/engagements')) {
    return '/engagements'
  }
  if (route.path.startsWith('/in-meeting')) {
    return '/in-meeting'
  }
  if (route.path.startsWith('/needs')) {
    return '/needs'
  }
  if (route.path.startsWith('/proposals')) {
    return '/proposals'
  }
  return route.path
})

function handleUpdate(key: string) {
  const item = SHELL_NAV_GROUPS.flatMap(group => group.children).find(
    child => (child.to ?? child.key) === key,
  )
  if (!item || item.disabled || !item.to) {
    return
  }
  router.push(item.to)
}
</script>

<template>
  <nav class="shell-sidebar" data-testid="shell-sidebar" aria-label="GITS Bank 主导航">
    <div
      v-for="group in SHELL_NAV_GROUPS"
      :key="group.key"
      class="nav-group-label"
      :data-testid="`nav-group-${group.key}`"
    >
      {{ group.label }}
    </div>
    <n-menu
      class="shell-menu"
      data-testid="shell-side-menu"
      mode="vertical"
      :value="activeKey"
      :options="menuOptions"
      :root-indent="16"
      :indent="16"
      @update:value="handleUpdate"
    />
  </nav>
</template>

<style scoped>
.shell-sidebar {
  height: 100%;
  background: #08233b;
  color: #dbeafe;
  padding: 12px 0 24px;
  overflow-y: auto;
}
.nav-group-label {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
.shell-menu {
  background: transparent;
}
:deep(.n-menu-item-content) {
  color: #cbd5e1 !important;
}
:deep(.n-menu-item-content--selected) {
  color: #93c5fd !important;
  background: rgba(37, 99, 235, 0.22) !important;
}
:deep(.n-menu-item-group-title) {
  color: #93c5fd !important;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: none;
}
</style>
