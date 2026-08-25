<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { NConfigProvider, NMessageProvider, darkTheme } from 'naive-ui'
import ExperienceShell from './layouts/ExperienceShell.vue'

const route = useRoute()
const isDark = ref(false)
const isPublic = computed(() => route.meta.public === true)

function initTheme() {
  const saved = localStorage.getItem('gits-theme')
  if (saved === 'dark') {
    isDark.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }
}

function toggleTheme(dark: boolean) {
  isDark.value = dark
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : '')
  localStorage.setItem('gits-theme', dark ? 'dark' : 'light')
}

onMounted(initTheme)
</script>

<template>
  <n-config-provider :theme="isDark ? darkTheme : undefined">
    <n-message-provider>
      <ExperienceShell v-if="!isPublic" :is-dark="isDark" @update:is-dark="toggleTheme">
        <router-view />
      </ExperienceShell>
      <router-view v-else />
    </n-message-provider>
  </n-config-provider>
</template>
