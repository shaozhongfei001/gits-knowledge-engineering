<template>
  <div class="event-card" :class="`event-${severityClass}`">
    <div class="event-header">
      <span class="source-badge">{{ sourceLabel }}</span>
      <span class="severity-badge" :class="`sev-${severityClass}`">{{ severityLabel }}</span>
    </div>
    <div class="event-title">{{ event.title }}</div>
    <div class="event-content">{{ event.content }}</div>
    <div v-if="event.possibleBusinessSignal" class="business-signal">
      信号: {{ event.possibleBusinessSignal }}
    </div>
    <div v-if="event.noGoStatement" class="no-go">
      禁止: {{ event.noGoStatement }}
    </div>
    <div class="event-meta">
      <span class="event-date">{{ formattedDate }}</span>
      <span v-if="event.bankUseAllowed" class="bank-usable">可用于银行业务</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ExternalEvent, ExternalEventType, ExternalEventSeverity } from '../api/v11'
import { EXTERNAL_EVENT_TYPE_LABELS, EXTERNAL_EVENT_SEVERITY_LABELS } from '../api/v11'

const props = defineProps<{
  event: ExternalEvent
}>()

const sourceLabel = computed(() =>
  EXTERNAL_EVENT_TYPE_LABELS[props.event.sourceType as ExternalEventType] || props.event.sourceType
)
const severityLabel = computed(() =>
  EXTERNAL_EVENT_SEVERITY_LABELS[(props.event.confidence as ExternalEventSeverity)] || props.event.confidence || '中'
)
const severityClass = computed(() => {
  const sev = props.event.confidence?.toLowerCase() || 'medium'
  return sev
})
const formattedDate = computed(() => {
  try {
    return new Date(props.event.eventDate).toLocaleDateString('zh-CN')
  } catch {
    return props.event.eventDate
  }
})
</script>

<style scoped>
.event-card {
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: box-shadow 0.2s;
}
.event-card:hover {
  box-shadow: 0 2px 8px rgba(0, 51, 102, 0.1);
}
.event-critical { border-left: 3px solid #cf1322; }
.event-high { border-left: 3px solid #fa541c; }
.event-medium { border-left: 3px solid #faad14; }
.event-low { border-left: 3px solid #52c41a; }
.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.source-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #e6f7ff;
  color: #003366;
  font-weight: 600;
}
.severity-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.sev-critical { background: #fff1f0; color: #cf1322; }
.sev-high { background: #fff2e8; color: #fa541c; }
.sev-medium { background: #fff7e6; color: #faad14; }
.sev-low { background: #f6ffed; color: #52c41a; }
.event-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}
.event-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}
.business-signal {
  font-size: 12px;
  color: #003366;
  background: #e6f7ff;
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 4px;
}
.no-go {
  font-size: 12px;
  color: #cf1322;
  background: #fff1f0;
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 4px;
}
.event-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
.bank-usable {
  color: #389e0d;
  font-weight: 500;
}
</style>
