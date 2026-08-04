<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NMenu, NLayout, NLayoutHeader, NLayoutContent, NButton, NIcon } from 'naive-ui'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()

const menuOptions: MenuOption[] = [
  { label: '客户经营概览', key: 'Dashboard' },
  { label: '持续经营工作台', key: 'EngagementWorkspace' }
]

const activeKey = ref('Dashboard')

function handleMenuUpdate(key: string) {
  activeKey.value = key
  router.push({ name: key })
}

// Sync active key with route
import { watch } from 'vue'
watch(() => route.name, (name) => {
  if (name && typeof name === 'string') {
    activeKey.value = name
  }
}, { immediate: true })
</script>

<template>
  <n-layout class="app-layout">
    <n-layout-header class="app-header" bordered>
      <div class="header-left">
        <span class="brand" @click="router.push('/')">GITS</span>
        <span class="brand-sub">客户经营闭环</span>
      </div>
      <n-menu
        mode="horizontal"
        :value="activeKey"
        :options="menuOptions"
        @update:value="handleMenuUpdate"
        class="header-menu"
      />
    </n-layout-header>
    <n-layout-content class="app-content">
      <router-view />
    </n-layout-content>
  </n-layout>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.app-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  background: linear-gradient(135deg, #003366, #004d99);
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-right: 32px;
}
.brand {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  cursor: pointer;
  letter-spacing: 2px;
}
.brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}
.header-menu {
  background: transparent;
}
:deep(.header-menu .n-menu-item) {
  color: rgba(255, 255, 255, 0.8);
}
:deep(.header-menu .n-menu-item--selected) {
  color: #ffd700;
}
.app-content {
  padding: 0;
  min-height: calc(100vh - 56px);
}
</style>
