<script setup lang="ts">
import { RouterLink } from 'vue-router'

defineProps<{
  customerId: string
}>()
</script>

<template>
  <nav class="slice-tabs" data-testid="customer-slice-tabs" aria-label="客户记录页签">
    <RouterLink
      :to="{ name: 'CustomerOperatingView', params: { id: customerId } }"
      data-testid="tab-p04"
    >
      经营总览
    </RouterLink>
    <RouterLink
      :to="{ name: 'CustomerGroupView', params: { id: customerId } }"
      data-testid="tab-p05"
    >
      集团关系
    </RouterLink>
    <RouterLink
      :to="{ name: 'CustomerFundsView', params: { id: customerId } }"
      data-testid="tab-p06"
    >
      业务资金
    </RouterLink>
    <RouterLink
      :to="{ name: 'CustomerPartiesView', params: { id: customerId } }"
      data-testid="tab-p07"
    >
      关系人情报
    </RouterLink>
  </nav>
</template>

<style scoped>
.slice-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
.slice-tabs a {
  color: var(--brand-primary);
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
}
.slice-tabs a.router-link-active {
  text-decoration: underline;
}
</style>
