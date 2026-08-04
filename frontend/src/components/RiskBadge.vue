<template>
  <span class="risk-badge" :class="levelClass">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RiskLevel } from '../api/engagement'
import { RISK_LEVEL_LABELS } from '../api/engagement'

const props = defineProps<{
  level?: RiskLevel | string
}>()

const label = computed(() =>
  props.level ? RISK_LEVEL_LABELS[props.level as RiskLevel] || props.level : '未评估'
)

const levelClass = computed(() => {
  switch (props.level) {
    case 'HIGH': return 'risk-high'
    case 'MEDIUM': return 'risk-medium'
    case 'LOW': return 'risk-low'
    default: return 'risk-unknown'
  }
})
</script>

<style scoped>
.risk-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.risk-high {
  background: #fff1f0;
  color: #cf1322;
  border: 1px solid #ffa39e;
}
.risk-medium {
  background: #fff7e6;
  color: #d46b08;
  border: 1px solid #ffd591;
}
.risk-low {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}
.risk-unknown {
  background: #f5f5f5;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
}
</style>
