<template>
  <div class="opportunity-card" :class="`opp-${stageClass}`">
    <div class="opp-header">
      <span class="type-badge">{{ typeLabel }}</span>
      <span class="stage-badge" :class="`stage-${stageClass}`">{{ stageLabel }}</span>
    </div>
    <div class="opp-description">{{ opportunity.description }}</div>
    <div class="opp-amount" v-if="opportunity.estimatedAmount">
      {{ formattedAmount }}
    </div>
    <div class="opp-meta">
      <span v-if="opportunity.assignedTo" class="assignee">{{ opportunity.assignedTo }}</span>
      <span v-if="opportunity.probability != null" class="probability">
        赢率: {{ (opportunity.probability * 100).toFixed(0) }}%
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Opportunity, OpportunityType, OpportunityStage } from '../api/v11'
import { OPPORTUNITY_TYPE_LABELS, OPPORTUNITY_STAGE_LABELS } from '../api/v11'

const props = defineProps<{
  opportunity: Opportunity
}>()

const typeLabel = computed(() =>
  OPPORTUNITY_TYPE_LABELS[props.opportunity.opportunityType as OpportunityType] || props.opportunity.opportunityType
)
const stageLabel = computed(() =>
  OPPORTUNITY_STAGE_LABELS[props.opportunity.stage as OpportunityStage] || props.opportunity.stage
)
const stageClass = computed(() => props.opportunity.stage.toLowerCase().replace('_', '-'))
const formattedAmount = computed(() => {
  if (!props.opportunity.estimatedAmount) return ''
  const curr = props.opportunity.currency || 'CNY'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency', currency: curr, maximumFractionDigits: 0
  }).format(props.opportunity.estimatedAmount)
})
</script>

<style scoped>
.opportunity-card {
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: box-shadow 0.2s;
}
.opportunity-card:hover {
  box-shadow: 0 2px 8px rgba(0, 51, 102, 0.1);
}
.opp-identified { border-left: 3px solid #003366; }
.opp-qualified { border-left: 3px solid #1890ff; }
.opp-proposal { border-left: 3px solid #b8860b; }
.opp-negotiation { border-left: 3px solid #fa8c16; }
.opp-closed-won { border-left: 3px solid #389e0d; }
.opp-closed-lost { border-left: 3px solid #d9d9d9; }
.opp-header {
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
.stage-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.stage-identified { background: #e6f7ff; color: #003366; }
.stage-qualified { background: #e6f7ff; color: #1890ff; }
.stage-proposal { background: #fff7e6; color: #b8860b; }
.stage-negotiation { background: #fff7e6; color: #fa8c16; }
.stage-closed-won { background: #f6ffed; color: #389e0d; }
.stage-closed-lost { background: #f5f5f5; color: #999; }
.opp-description {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}
.opp-amount {
  font-size: 18px;
  font-weight: 700;
  color: #003366;
  margin-bottom: 8px;
}
.opp-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
.assignee { color: #003366; font-weight: 500; }
.probability { color: #b8860b; }
</style>
