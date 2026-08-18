<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  NModal, NCard, NDescriptions, NDescriptionsItem, NTag, NButton,
  NSpace, NInput, NAlert, NSpin, NDivider, NDataTable
} from 'naive-ui'
import {
  type CrmWritebackCommand,
  type GateDecision,
  CRM_WRITEBACK_STATUS_LABELS
} from '../api/v11'

const props = defineProps<{
  show: boolean
  command: CrmWritebackCommand | null
  loading?: boolean
  error?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'decide', commandId: string, decision: GateDecision, modifications: Record<string, unknown>[] | undefined, reason: string): void
}>()

const reason = ref('')
const submitting = ref(false)
const editMode = ref(false)
const modificationsJson = ref('')

const statusLabel = computed(() => {
  if (!props.command) return ''
  return CRM_WRITEBACK_STATUS_LABELS[props.command.status] || props.command.status
})

const statusType = computed(() => {
  if (!props.command) return 'default' as const
  switch (props.command.status) {
    case 'PENDING': return 'warning' as const
    case 'APPROVED': return 'success' as const
    case 'REJECTED': return 'error' as const
    case 'SENT': return 'info' as const
    case 'FAILED': return 'error' as const
    default: return 'default' as const
  }
})

const isPending = computed(() => props.command?.status === 'PENDING')

const payloadEntries = computed(() => {
  if (!props.command?.payload) return []
  return Object.entries(props.command.payload).map(([key, value]) => ({
    field: key,
    value: typeof value === 'object' ? JSON.stringify(value) : String(value)
  }))
})

function handleApprove() {
  if (!props.command) return
  submitting.value = true
  emit('decide', props.command.commandId, 'APPROVE', undefined, reason.value)
  setTimeout(() => { submitting.value = false }, 500)
}

function handleReject() {
  if (!props.command) return
  submitting.value = true
  emit('decide', props.command.commandId, 'REJECT', undefined, reason.value)
  setTimeout(() => { submitting.value = false }, 500)
}

function handleModify() {
  editMode.value = true
}

function submitModification() {
  if (!props.command) return
  submitting.value = true
  let mods: Record<string, unknown>[] | undefined
  if (modificationsJson.value) {
    try {
      mods = JSON.parse(modificationsJson.value)
    } catch {
      // 解析失败，忽略
    }
  }
  emit('decide', props.command.commandId, 'MODIFY', mods, reason.value)
  setTimeout(() => {
    submitting.value = false
    editMode.value = false
    modificationsJson.value = ''
  }, 500)
}

function handleClose() {
  emit('update:show', false)
  reason.value = ''
  editMode.value = false
  modificationsJson.value = ''
}
</script>

<template>
  <NModal :show="show" @update:show="handleClose" preset="card" style="width: 720px" title="CRM写回审批">
    <NSpin :show="loading">
      <NAlert v-if="error" type="error" :title="error" style="margin-bottom: 16px" />

      <template v-if="command">
        <NDescriptions bordered :column="2" label-placement="left" size="small">
          <NDescriptionsItem label="命令ID">{{ command.commandId }}</NDescriptionsItem>
          <NDescriptionsItem label="状态">
            <NTag :type="statusType" size="small">{{ statusLabel }}</NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="操作">{{ command.operation }}</NDescriptionsItem>
          <NDescriptionsItem label="目标实体">{{ command.targetEntity }}</NDescriptionsItem>
          <NDescriptionsItem v-if="command.journeyId" label="旅程ID">{{ command.journeyId }}</NDescriptionsItem>
          <NDescriptionsItem v-if="command.customerId" label="客户ID">{{ command.customerId }}</NDescriptionsItem>
        </NDescriptions>

        <NDivider>写回数据</NDivider>

        <NDataTable
          :columns="[
            { title: '字段', key: 'field', width: 200 },
            { title: '值', key: 'value' }
          ]"
          :data="payloadEntries"
          :bordered="true"
          size="small"
          style="max-height: 300px; overflow-y: auto"
        />

        <!-- 审批区域 -->
        <template v-if="isPending">
          <NDivider>审批决策</NDivider>

          <NInput
            v-model:value="reason"
            type="textarea"
            placeholder="请输入决策原因（可选）"
            :rows="2"
            style="margin-bottom: 12px"
          />

          <template v-if="editMode">
            <NInput
              v-model:value="modificationsJson"
              type="textarea"
              placeholder="请输入修改内容（JSON格式）"
              :rows="4"
              style="margin-bottom: 12px"
            />
            <NSpace>
              <NButton type="info" :loading="submitting" @click="submitModification">确认修改</NButton>
              <NButton @click="editMode = false">取消</NButton>
            </NSpace>
          </template>

          <NSpace v-else justify="center" style="margin-top: 8px">
            <NButton type="success" :loading="submitting" @click="handleApprove">批准</NButton>
            <NButton type="info" @click="handleModify">修改后批准</NButton>
            <NButton type="error" :loading="submitting" @click="handleReject">驳回</NButton>
          </NSpace>
        </template>

        <!-- 已决策信息 -->
        <template v-else-if="command.decision">
          <NDivider>决策结果</NDivider>
          <NDescriptions bordered :column="2" label-placement="left" size="small">
            <NDescriptionsItem label="决策">{{ command.decision }}</NDescriptionsItem>
            <NDescriptionsItem label="决策人">{{ command.actorId || '-' }}</NDescriptionsItem>
            <NDescriptionsItem v-if="command.decisionReason" label="原因" :span="2">
              {{ command.decisionReason }}
            </NDescriptionsItem>
          </NDescriptions>
        </template>
      </template>

      <NAlert v-else type="info" title="未选择写回命令" />
    </NSpin>

    <template #footer>
      <NSpace justify="end">
        <NButton @click="handleClose">关闭</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
