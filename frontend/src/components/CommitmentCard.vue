<template>
  <div class="commitment-card" :class="`commitment-${commitment.status.toLowerCase()}`">
    <div class="commitment-header">
      <span class="type-badge">{{ typeLabel }}</span>
      <span class="status-badge" :class="`status-${commitment.status.toLowerCase()}`">
        {{ statusLabel }}
      </span>
    </div>
    <div class="commitment-content">{{ commitment.content }}</div>
    <div class="commitment-meta">
      <span v-if="commitment.owner" class="owner">{{ commitment.owner }}</span>
      <span v-if="commitment.dueDate" class="due-date" :class="{ overdue: isOverdue }">
        截止: {{ formattedDueDate }}
      </span>
    </div>
    <div v-if="commitment.evidenceRef" class="evidence-ref">
      证据: {{ commitment.evidenceRef }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Commitment, CommitmentType, CommitmentStatus } from '../api/v11'
import { COMMITMENT_TYPE_LABELS, COMMITMENT_STATUS_LABELS } from '../api/v11'

const props = defineProps<{
  commitment: Commitment
}>()

const typeLabel = computed(() =>
  COMMITMENT_TYPE_LABELS[props.commitment.commitmentType as CommitmentType] || props.commitment.commitmentType
)
const statusLabel = computed(() =>
  COMMITMENT_STATUS_LABELS[props.commitment.status as CommitmentStatus] || props.commitment.status
)
const formattedDueDate = computed(() => {
  try {
    return new Date(props.commitment.dueDate!).toLocaleDateString('zh-CN')
  } catch {
    return props.commitment.dueDate
  }
})
const isOverdue = computed(() => {
  if (props.commitment.status !== 'OPEN' || !props.commitment.dueDate) return false
  return new Date(props.commitment.dueDate) < new Date()
})
</script>

<style scoped>
.commitment-card {
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: box-shadow 0.2s;
}
.commitment-card:hover {
  box-shadow: 0 2px 8px rgba(0, 51, 102, 0.1);
}
.commitment-open { border-left: 3px solid #003366; }
.commitment-fulfilled { border-left: 3px solid #389e0d; }
.commitment-breached { border-left: 3px solid #cf1322; }
.commitment-waived { border-left: 3px solid #d9d9d9; }
.commitment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.type-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #e6f7ff;
  color: #003366;
  font-weight: 600;
}
.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.status-open { background: #e6f7ff; color: #003366; }
.status-fulfilled { background: #f6ffed; color: #389e0d; }
.status-breached { background: #fff1f0; color: #cf1322; }
.status-waived { background: #f5f5f5; color: #999; }
.commitment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}
.commitment-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
.owner { color: #003366; font-weight: 500; }
.due-date { color: #8c8c8c; }
.overdue { color: #cf1322; font-weight: 600; }
.evidence-ref {
  margin-top: 6px;
  font-size: 11px;
  color: #8c8c8c;
  font-style: italic;
}
</style>
