<script setup lang="ts">
import { NCard } from 'naive-ui'
import AuditTraceList from '../components/AuditTraceList.vue'
</script>

<template>
  <div class="audit-trace-view">
    <NCard title="审计追踪">
      <AuditTraceList />
    </NCard>
  </div>
</template>

<style scoped>
.audit-trace-view {
  padding: 24px;
}
</style>
