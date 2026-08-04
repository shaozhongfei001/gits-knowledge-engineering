<template>
  <n-card class="customer-card" hoverable @click="$emit('click')">
    <div class="card-header">
      <span class="customer-name">{{ customer.customerName }}</span>
      <RiskBadge :level="customer.riskLevel" />
    </div>
    <div class="card-body">
      <div class="info-row">
        <span class="label">行业</span>
        <span class="value">{{ industryLabel }}</span>
      </div>
      <div class="info-row">
        <span class="label">规模</span>
        <span class="value">{{ scaleLabel }}</span>
      </div>
      <div class="info-row">
        <span class="label">层级</span>
        <span class="value tier">{{ tierLabel }}</span>
      </div>
      <div v-if="customer.coreTags?.length" class="tags-row">
        <n-tag v-for="tag in customer.coreTags?.slice(0, 3)" :key="tag" size="small" type="info">
          {{ tag }}
        </n-tag>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NTag } from 'naive-ui'
import RiskBadge from './RiskBadge.vue'
import type { Customer, Industry, EnterpriseScale, CustomerTier } from '../api/engagement'
import { INDUSTRY_LABELS, ENTERPRISE_SCALE_LABELS, CUSTOMER_TIER_LABELS } from '../api/engagement'

const props = defineProps<{
  customer: Customer
}>()

defineEmits<{ click: [] }>()

const industryLabel = computed(() =>
  props.customer.industry ? INDUSTRY_LABELS[props.customer.industry as Industry] : '-'
)
const scaleLabel = computed(() =>
  props.customer.enterpriseScale ? ENTERPRISE_SCALE_LABELS[props.customer.enterpriseScale as EnterpriseScale] : '-'
)
const tierLabel = computed(() =>
  props.customer.customerTier ? CUSTOMER_TIER_LABELS[props.customer.customerTier as CustomerTier] : '-'
)
</script>

<style scoped>
.customer-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.customer-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 51, 102, 0.15);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #003366;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}
.info-row .label {
  color: #8c8c8c;
}
.info-row .value {
  color: #333;
  font-weight: 500;
}
.info-row .tier {
  color: #b8860b;
}
.tags-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 4px;
}
</style>
