<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NLayout, NLayoutContent, NLayoutHeader, NLayoutSider, NSwitch } from 'naive-ui'
import AppSidebar from '../components/shell/AppSidebar.vue'
import WorkspaceTabs from '../components/shell/WorkspaceTabs.vue'
import { useWorkspaceTabsStore } from '../stores/workspaceTabs'

defineProps<{
  isDark: boolean
}>()

const emit = defineEmits<{ 'update:isDark': [value: boolean] }>()

const route = useRoute()
const router = useRouter()
const tabs = useWorkspaceTabsStore()

function toggleTheme(dark: boolean) {
  emit('update:isDark', dark)
}

const objectType = computed(() => String(route.meta.objectType || '工作区'))
const pageTitle = computed(() => String(route.meta.title || 'GITS Bank'))

watch(
  () => route.fullPath,
  () => {
    if (route.meta.public) {
      return
    }
    tabs.openTab({
      id: String(route.name || route.path),
      title: pageTitle.value,
      path: route.path,
      routeName: String(route.name || ''),
      objectType: objectType.value,
      recordId: typeof route.params.id === 'string' ? route.params.id : undefined,
    })
  },
  { immediate: true },
)

function goHome() {
  router.push('/workbench')
}
</script>

<template>
  <n-layout class="experience-shell" data-testid="experience-shell" has-sider>
      <n-layout-sider
        bordered
        :width="232"
        :collapsed-width="64"
        content-style="padding: 0;"
        class="shell-sider"
      >
        <AppSidebar />
      </n-layout-sider>
      <n-layout>
        <n-layout-header class="shell-header" bordered>
          <button type="button" class="brand" data-testid="shell-brand" @click="goHome">
            <span class="brand-mark">G</span>
            <span class="brand-copy">
              <span class="brand-name">GITS Bank</span>
              <span class="brand-sub">对公客户经营</span>
            </span>
          </button>
          <WorkspaceTabs />
          <n-switch :value="isDark" size="small" @update:value="toggleTheme">
            <template #checked>暗色</template>
            <template #unchecked>亮色</template>
          </n-switch>
        </n-layout-header>
        <n-layout-content class="shell-content">
          <slot />
        </n-layout-content>
      </n-layout>
    </n-layout>
</template>

<style scoped>
.experience-shell {
  min-height: 100vh;
}
.shell-sider {
  background: #08233b;
}
.shell-header {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 56px;
  padding: 0 16px;
  background: var(--bg-header);
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--brand-primary);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}
.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
.brand-sub {
  font-size: 10px;
  color: var(--text-tertiary);
}
.shell-content {
  padding: 20px 24px 32px;
  background: var(--bg-page);
  min-height: calc(100vh - 56px);
}
</style>
