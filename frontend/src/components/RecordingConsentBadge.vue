<template>
  <span class="consent-badge" :class="`consent-${statusClass}`">
    {{ statusLabel }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RecordingConsentStatus } from '../api/v11'
import { RECORDING_CONSENT_STATUS_LABELS } from '../api/v11'

const props = defineProps<{
  status: RecordingConsentStatus
}>()

const statusLabel = computed(() =>
  RECORDING_CONSENT_STATUS_LABELS[props.status] || props.status
)
const statusClass = computed(() => props.status.toLowerCase())
</script>

<style scoped>
.consent-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.consent-granted { background: #f6ffed; color: #389e0d; }
.consent-denied { background: #fff1f0; color: #cf1322; }
.consent-pending { background: #fff7e6; color: #faad14; }
.consent-revoked { background: #f5f5f5; color: #999; }
</style>
