<script setup lang="ts">
export interface StagePathStage {
  key: string
  label: string
}

const props = defineProps<{
  stages: StagePathStage[]
  currentKey: string
  completedKeys?: string[]
}>()

function stateOf(key: string): 'done' | 'current' | 'future' {
  if (props.completedKeys?.includes(key)) return 'done'
  if (props.currentKey === key) return 'current'
  return 'future'
}
</script>

<template>
  <nav class="stage-path" data-testid="stage-path" aria-label="阶段路径">
    <template v-for="(s, i) in stages" :key="s.key">
      <div class="sp-step" :class="`sp-${stateOf(s.key)}`">
        <span class="sp-dot" />
        <span class="sp-label">{{ s.label }}</span>
      </div>
      <span
        v-if="i < stages.length - 1"
        class="sp-connector"
        :class="{ 'sp-connector-done': stateOf(s.key) === 'done' }"
      />
    </template>
  </nav>
</template>

<style scoped>
.stage-path {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.sp-step {
  display: flex;
  align-items: center;
  gap: 6px;
}
.sp-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--border-strong);
  background: #fff;
  flex-shrink: 0;
}
.sp-label {
  font-size: 13px;
  color: var(--text-tertiary);
}
.sp-current .sp-dot {
  border-color: var(--brand-primary);
  background: var(--brand-primary);
}
.sp-current .sp-label {
  color: var(--brand-primary);
  font-weight: 600;
}
.sp-done .sp-dot {
  border-color: var(--gits-teal-500, #12a7a0);
  background: var(--gits-teal-500, #12a7a0);
}
.sp-done .sp-label {
  color: var(--gits-teal-700, #087771);
}
.sp-connector {
  width: 24px;
  height: 2px;
  background: var(--border-light);
  border-radius: 1px;
}
.sp-connector-done {
  background: var(--gits-teal-500, #12a7a0);
}
</style>
