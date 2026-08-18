<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  NLayout, NLayoutContent, NLayoutSider, NCard, NTabs, NTabPane,
  NSpace, NTag, NButton, NInput, NDescriptions, NDescriptionsItem,
  NAlert, NSpin, NEmpty, NBadge, NDivider
} from 'naive-ui'
import {
  type HumanGate, type CrmWritebackCommand, type GateDecision,
  GATE_TYPE_LABELS, HUMAN_GATE_STATUS_LABELS, GATE_DECISION_LABELS,
  CRM_WRITEBACK_STATUS_LABELS
} from '../api/v11'
import { useHumanGate, useCrmWriteback } from '../composables/useHumanGate'
import HumanGateDialog from '../components/HumanGateDialog.vue'
import CrmWritebackApproval from '../components/CrmWritebackApproval.vue'

const route = useRoute()
const journeyId = computed(() => route.params.id as string || '')

// Composables
const { gates: pendingGates, loading: gatesLoading, loadGates, decide: decideGate } = useHumanGate()
const { commands: crmCommands, loading: crmLoading, loadCommands, decideCommand } = useCrmWriteback()

// State
const showGateDialog = ref(false)
const selectedGate = ref<HumanGate | null>(null)
const showCrmDialog = ref(false)
const selectedCrmCommand = ref<CrmWritebackCommand | null>(null)
const meetingNotes = ref('')
const isRecording = ref(false)
const deciding = ref(false)

// Polling
let pollTimer: ReturnType<typeof setInterval> | null = null

function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(() => {
    loadGates({ status: 'PENDING', journeyId: journeyId.value })
    loadCommands({ journeyId: journeyId.value })
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// Computed
const pendingGateCount = computed(() => pendingGates.value.length)
const pendingCrmCount = computed(() => crmCommands.value.filter(c => c.status === 'PENDING').length)

// Handlers
function openGate(gate: HumanGate) {
  selectedGate.value = gate
  showGateDialog.value = true
}

function openCrmCommand(cmd: CrmWritebackCommand) {
  selectedCrmCommand.value = cmd
  showCrmDialog.value = true
}

async function handleGateDecide(gateId: string, decision: GateDecision,
                                 modification?: Record<string, unknown>, reason?: string) {
  deciding.value = true
  try {
    await decideGate(gateId, decision, modification, reason)
    showGateDialog.value = false
  } finally {
    deciding.value = false
  }
}

async function handleCrmDecide(commandId: string, decision: GateDecision,
                                modifications?: Record<string, unknown>[], reason?: string) {
  deciding.value = true
  try {
    await decideCommand(commandId, { decision, modifications, reason: reason || '', actorId: 'current-user' })
    showCrmDialog.value = false
  } finally {
    deciding.value = false
  }
}

onMounted(() => {
  loadGates({ status: 'PENDING', journeyId: journeyId.value })
  loadCommands({ journeyId: journeyId.value })
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <NLayout has-sider style="height: 100vh">
    <!-- 左侧：待审批门禁 -->
    <NLayoutSider bordered width="320" content-style="padding: 16px;">
      <h3 style="margin: 0 0 12px 0">
        <NBadge :value="pendingGateCount" :max="99">
          待审批门禁
        </NBadge>
      </h3>

      <NSpin :show="gatesLoading">
        <NEmpty v-if="!pendingGates.length" description="暂无待审批门禁" />

        <NSpace vertical :size="8" v-else>
          <NCard
            v-for="gate in pendingGates"
            :key="gate.gateId"
            size="small"
            hoverable
            @click="openGate(gate)"
            style="cursor: pointer"
          >
            <template #header>
              <NTag size="small" type="warning">{{ GATE_TYPE_LABELS[gate.gateType] }}</NTag>
            </template>
            <div style="font-size: 13px">{{ gate.subject }}</div>
          </NCard>
        </NSpace>
      </NSpin>

      <NDivider />

      <h3 style="margin: 0 0 12px 0">
        <NBadge :value="pendingCrmCount" :max="99">
          CRM写回
        </NBadge>
      </h3>

      <NSpin :show="crmLoading">
        <NEmpty v-if="!crmCommands.filter(c => c.status === 'PENDING').length" description="暂无写回命令" />

        <NSpace vertical :size="8" v-else>
          <NCard
            v-for="cmd in crmCommands.filter(c => c.status === 'PENDING')"
            :key="cmd.commandId"
            size="small"
            hoverable
            @click="openCrmCommand(cmd)"
            style="cursor: pointer"
          >
            <template #header>
              <NTag size="small" type="info">{{ cmd.operation }}</NTag>
            </template>
            <div style="font-size: 13px">{{ cmd.targetEntity }}</div>
          </NCard>
        </NSpace>
      </NSpin>
    </NLayoutSider>

    <!-- 右侧：会中内容 -->
    <NLayoutContent content-style="padding: 24px;">
      <NCard title="会中助手">
        <template #header-extra>
          <NSpace>
            <NTag :type="isRecording ? 'success' : 'default'">
              {{ isRecording ? '录音中' : '未录音' }}
            </NTag>
            <NButton size="small" @click="isRecording = !isRecording">
              {{ isRecording ? '停止录音' : '开始录音' }}
            </NButton>
          </NSpace>
        </template>

        <NTabs type="line">
          <NTabPane name="notes" tab="会议记录">
            <NInput
              v-model:value="meetingNotes"
              type="textarea"
              placeholder="在此记录会议要点..."
              :rows="12"
            />
          </NTabPane>

          <NTabPane name="actions" tab="待办事项">
            <NEmpty description="暂无待办事项" />
          </NTabPane>

          <NTabPane name="evidence" tab="证据收集">
            <NEmpty description="暂无证据" />
          </NTabPane>
        </NTabs>
      </NCard>
    </NLayoutContent>

    <!-- 门禁弹窗 -->
    <HumanGateDialog
      v-model:show="showGateDialog"
      :gate="selectedGate"
      :loading="deciding"
      @decide="handleGateDecide"
    />

    <!-- CRM写回弹窗 -->
    <CrmWritebackApproval
      v-model:show="showCrmDialog"
      :command="selectedCrmCommand"
      :loading="deciding"
      @decide="handleCrmDecide"
    />
  </NLayout>
</template>
