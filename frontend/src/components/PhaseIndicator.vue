<template>
  <div class="phase-indicator">
    <div
      v-for="(phase, index) in phases"
      :key="phase.key"
      class="phase-step"
      :class="{
        'phase-active': phase.key === current,
        'phase-completed': isCompleted(phase.key),
        'phase-pending': !isCompleted(phase.key) && phase.key !== current
      }"
    >
      <div class="phase-dot">
        <span v-if="isCompleted(phase.key)" class="check">&#10003;</span>
        <span v-else class="step-number">{{ index + 1 }}</span>
      </div>
      <span class="phase-label">{{ phase.label }}</span>
      <div v-if="index < phases.length - 1" class="phase-connector" :class="{ 'connector-active': isCompleted(phase.key) }" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { JourneyPhase } from '../api/engagement'
import { JOURNEY_PHASE_LABELS } from '../api/engagement'

const props = defineProps<{
  current: JourneyPhase
}>()

const phaseOrder: JourneyPhase[] = ['KYC_COLLECT', 'INSIGHT_ANALYSIS', 'PRODUCT_MATCHING', 'PREVISIT_PREP', 'POSTVISIT_REVIEW', 'COMPLETED']

const phases = phaseOrder.map(key => ({ key, label: JOURNEY_PHASE_LABELS[key] }))

function isCompleted(phase: JourneyPhase): boolean {
  return phaseOrder.indexOf(phase) < phaseOrder.indexOf(props.current)
}
</script>

<style scoped>
.phase-indicator {
  display: flex;
  align-items: flex-start;
  gap: 0;
  padding: 16px 0;
  overflow-x: auto;
}
.phase-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
  position: relative;
}
.phase-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  transition: all 0.3s;
}
.phase-active .phase-dot {
  background: #003366;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(0, 51, 102, 0.2);
}
.phase-completed .phase-dot {
  background: #b8860b;
  color: #fff;
}
.phase-pending .phase-dot {
  background: #f0f0f0;
  color: #999;
  border: 2px solid #d9d9d9;
}
.check {
  font-size: 16px;
}
.step-number {
  font-size: 13px;
}
.phase-label {
  margin-top: 6px;
  font-size: 12px;
  text-align: center;
  color: #666;
  max-width: 90px;
}
.phase-active .phase-label {
  color: #003366;
  font-weight: 600;
}
.phase-completed .phase-label {
  color: #b8860b;
}
.phase-connector {
  position: absolute;
  top: 16px;
  left: calc(50% + 16px);
  width: calc(100% - 32px);
  height: 2px;
  background: #e8e8e8;
}
.connector-active {
  background: #b8860b;
}
</style>
