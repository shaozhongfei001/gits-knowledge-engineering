<template>
  <div class="task-card" :class="`task-${task.status.toLowerCase()}`">
    <div class="task-header">
      <span class="type-badge">{{ typeLabel }}</span>
      <span class="status-badge" :class="`status-${task.status.toLowerCase()}`">
        {{ statusLabel }}
      </span>
    </div>
    <div class="task-title">{{ task.title }}</div>
    <div v-if="task.description" class="task-description">{{ task.description }}</div>
    <div class="task-meta">
      <span v-if="task.assignedTo" class="assignee">{{ task.assignedTo }}</span>
      <span v-if="task.dueDate" class="due-date" :class="{ overdue: isOverdue }">
        截止: {{ formattedDueDate }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Task, TaskType, TaskStatus } from '../api/v11'
import { TASK_TYPE_LABELS, TASK_STATUS_LABELS } from '../api/v11'

const props = defineProps<{
  task: Task
}>()

const typeLabel = computed(() =>
  TASK_TYPE_LABELS[props.task.taskType as TaskType] || props.task.taskType
)
const statusLabel = computed(() =>
  TASK_STATUS_LABELS[props.task.status as TaskStatus] || props.task.status
)
const formattedDueDate = computed(() => {
  try {
    return new Date(props.task.dueDate!).toLocaleDateString('zh-CN')
  } catch {
    return props.task.dueDate
  }
})
const isOverdue = computed(() => {
  if (props.task.status === 'COMPLETED' || props.task.status === 'CANCELLED' || !props.task.dueDate) return false
  return new Date(props.task.dueDate) < new Date()
})
</script>

<style scoped>
.task-card {
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: box-shadow 0.2s;
}
.task-card:hover {
  box-shadow: 0 2px 8px rgba(0, 51, 102, 0.1);
}
.task-pending { border-left: 3px solid #faad14; }
.task-in_progress { border-left: 3px solid #003366; }
.task-completed { border-left: 3px solid #389e0d; }
.task-cancelled { border-left: 3px solid #d9d9d9; }
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.type-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #fff7e6;
  color: #b8860b;
  font-weight: 600;
}
.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.status-pending { background: #fff7e6; color: #b8860b; }
.status-in_progress { background: #e6f7ff; color: #003366; }
.status-completed { background: #f6ffed; color: #389e0d; }
.status-cancelled { background: #f5f5f5; color: #999; }
.task-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
}
.task-description {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}
.task-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
.assignee { color: #003366; font-weight: 500; }
.due-date { color: #8c8c8c; }
.overdue { color: #cf1322; font-weight: 600; }
</style>
