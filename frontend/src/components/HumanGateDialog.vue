<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  NModal, NCard, NDescriptions, NDescriptionsItem, NTag, NButton,
  NSpace, NInput, NAlert, NSpin, NDivider, NIcon, NTooltip
} from 'naive-ui'
import {
  type HumanGate,
  type GateDecision,
  GATE_TYPE_LABELS,
  HUMAN_GATE_STATUS_LABELS,
  GATE_DECISION_LABELS
} from '../api/v11'

const props = defineProps<{
  show: boolean
  gate: HumanGate | null
  loading?: boolean
  error?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'decide', gateId: string, decision: GateDecision, modification: Record<string, unknown> | undefined, reason: string): void
}>()

const selectedDecision = ref<GateDecision | null>(null)
const modificationJson = ref('')
const reason = ref('')
const submitting = ref(false)

const gateTypeLabel = computed(() => {
  if (!props.gate) return ''
  return GATE_TYPE_LABELS[props.gate.gateType] || props.gate.gateType
})

const statusLabel = computed(() => {
  if (!props.gate) return ''
  return HUMAN_GATE_STATUS_LABELS[props.gate.status] || props.gate.status
})

const statusType = computed(() => {
  if (!props.gate) return 'default' as const
  switch (props.gate.status) {
    case 'PENDING': return 'warning' as const
    case 'APPROVED': return 'success' as const
    case 'REJECTED': return 'error' as const
    case 'MODIFIED': return 'info' as const
    default: return 'default' as const
  }
})

const isPending = computed(() => props.gate?.status === 'PENDING')

const decisionOptions = computed(() => {
  const options: Array<{ value: GateDecision; label: string; type: 'success' | 'error' | 'warning' | 'info' }> = [
    { value: 'APPROVE', label: GATE_DECISION_LABELS.APPROVE, type: 'success' },
    { value: 'REJECT', label: GATE_DECISION_LABELS.REJECT, type: 'error' },
    { value: 'MODIFY', label: GATE_DECISION_LABELS.MODIFY, type: 'info' },
    { value: 'HOLD', label: GATE_DECISION_LABELS.HOLD, type: 'warning' },
    { value: 'DECLINE', label: GATE_DECISION_LABELS.DECLINE, type: 'error' }
  ]
  return options
})

function handleDecide(decision: GateDecision) {
  if (!props.gate) return
  selectedDecision.value = decision
  if (decision !== 'MODIFY') {
    submitDecision(decision)
  }
}

function submitDecision(decision: GateDecision) {
  if (!props.gate) return
  submitting.value = true
  let mod: Record<string, unknown> | undefined
  if (decision === 'MODIFY' && modificationJson.value) {
    try {
      mod = JSON.parse(modificationJson.value)
    } catch {
      // 解析失败，忽略
    }
  }
  emit('decide', props.gate.gateId, decision, mod, reason.value)
  setTimeout(() => {
    submitting.value = false
    selectedDecision.value = null
    reason.value = ''
    modificationJson.value = ''
  }, 500)
}

function handleClose() {
  emit('update:show', false)
  selectedDecision.value = null
  reason.value = ''
  modificationJson.value = ''
}
</script>

<template>
  <NModal :show="show" @update:show="handleClose" preset="card" style="width: 680px" :title="`门禁审批 - ${gateTypeLabel}`">
    <NSpin :show="loading">
      <NAlert v-if="error" type="error" :title="error" style="margin-bottom: 16px" />

      <template v-if="gate">
        <NDescriptions bordered :column="2" label-placement="left" size="small">
          <NDescriptionsItem label="门禁类型">
            <NTag size="small">{{ gateTypeLabel }}</NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="状态">
            <NTag :type="statusType" size="small">{{ statusLabel }}</NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="主题" :span="2">
            {{ gate.subject }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="gate.journeyId" label="旅程ID" :span="2">
            {{ gate.journeyId }}
          </NDescriptionsItem>
          <NDescriptionsItem v-if="gate.customerId" label="客户ID" :span="2">
            {{ gate.customerId }}
          </NDescriptionsItem>
        </NDescriptions>

        <NDivider>AI 提案</NDivider>

        <div v-if="gate.proposal" class="proposal-section">
          <pre class="proposal-content">{{ JSON.stringify(gate.proposal, null, 2) }}</pre>
        </div>
        <NAlert v-else type="info" title="无AI提案" />

        <div v-if="gate.evidenceRefs?.length" style="margin-top: 12px">
          <NDivider>证据引用</NDivider>
          <NSpace>
            <NTag v-for="ref in gate.evidenceRefs" :key="ref" size="small" type="info">
              {{ ref }}
            </NTag>
          </NSpace>
        </div>

        <!-- 决策区域 -->
        <template v-if="isPending">
          <NDivider>审批决策</NDivider>

          <NInput
            v-model:value="reason"
            type="textarea"
            placeholder="请输入决策原因（可选）"
            :rows="2"
            style="margin-bottom: 12px"
          />

          <template v-if="selectedDecision === 'MODIFY'">
            <NInput
              v-model:value="modificationJson"
              type="textarea"
              placeholder="请输入修改内容（JSON格式）"
              :rows="4"
              style="margin-bottom: 12px"
            />
            <NButton type="info" :loading="submitting" @click="submitDecision('MODIFY')">
              确认修改
            </NButton>
          </template>

          <NSpace v-else justify="center" style="margin-top: 8px">
            <NButton
              v-for="opt in decisionOptions"
              :key="opt.value"
              :type="opt.type"
              :loading="submitting && selectedDecision === opt.value"
              @click="handleDecide(opt.value)"
            >
              {{ opt.label }}
            </NButton>
          </NSpace>
        </template>

        <!-- 已决策信息 -->
        <template v-else-if="gate.decision">
          <NDivider>决策结果</NDivider>
          <NDescriptions bordered :column="2" label-placement="left" size="small">
            <NDescriptionsItem label="决策">
              <NTag :type="gate.decision === 'APPROVE' ? 'success' : 'error'" size="small">
                {{ GATE_DECISION_LABELS[gate.decision] }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="决策人">
              {{ gate.actorId || '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem v-if="gate.decisionReason" label="原因" :span="2">
              {{ gate.decisionReason }}
            </NDescriptionsItem>
            <NDescriptionsItem v-if="gate.modification" label="修改内容" :span="2">
              <pre>{{ JSON.stringify(gate.modification, null, 2) }}</pre>
            </NDescriptionsItem>
          </NDescriptions>
        </template>
      </template>

      <NAlert v-else type="info" title="未选择门禁" />
    </NSpin>

    <template #footer>
      <NSpace justify="end">
        <NButton @click="handleClose">关闭</NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped>
.proposal-section {
  max-height: 300px;
  overflow-y: auto;
  background: #f5f5f5;
  border-radius: 4px;
  padding: 12px;
}

.proposal-content {
  margin: 0;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
