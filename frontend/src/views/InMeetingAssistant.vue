<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  NLayout, NLayoutContent, NLayoutSider, NCard, NTabs, NTabPane,
  NSpace, NTag, NButton, NInput, NBadge, NDivider, NSpin, NEmpty,
} from 'naive-ui'
import {
  type HumanGate, type CrmWritebackCommand, type GateDecision,
  GATE_TYPE_LABELS,
} from '../api/v11'
import { useHumanGate, useCrmWriteback } from '../composables/useHumanGate'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import GuidancePanel from '../components/shell/GuidancePanel.vue'
import { usePageReferenceStore } from '../stores/pageReference'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import HumanGateDialog from '../components/HumanGateDialog.vue'
import CrmWritebackApproval from '../components/CrmWritebackApproval.vue'

const PAGE_ID = 'P15'
const OBJECT_TYPE = '互动 Interaction'

const router = useRouter()
const pageRefs = usePageReferenceStore()
const { journeyId } = useEngagementContext()

const { gates: pendingGates, loading: gatesLoading, error: gatesError, loadGates, decide: decideGate } = useHumanGate()
const { commands: crmCommands, loading: crmLoading, error: crmError, loadCommands, decideCommand } = useCrmWriteback()

const showGateDialog = ref(false)
const selectedGate = ref<HumanGate | null>(null)
const showCrmDialog = ref(false)
const selectedCrmCommand = ref<CrmWritebackCommand | null>(null)
const meetingNotes = ref('')
const isRecording = ref(false)
const deciding = ref(false)
const requested = ref(false)

let pollTimer: ReturnType<typeof setInterval> | null = null

const status = computed(() =>
  deriveResourceStatus({
    loading: gatesLoading.value || crmLoading.value,
    error: gatesError.value || crmError.value || '',
    hasData: requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => journeyId.value || '缺对象')
const pendingGateCount = computed(() => pendingGates.value.length)
const pendingCrmCount = computed(() => crmCommands.value.filter(c => c.status === 'PENDING').length)

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    recordId: journeyId.value || undefined,
    viewId: 'in_meeting',
    draftId: meetingNotes.value || undefined,
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(() => {
    loadWorkspace()
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function loadWorkspace() {
  requested.value = true
  await Promise.all([
    loadGates({ status: 'PENDING', journeyId: journeyId.value || undefined }),
    loadCommands({ journeyId: journeyId.value || undefined }),
  ])
}

function openGate(gate: HumanGate) {
  selectedGate.value = gate
  showGateDialog.value = true
}

function openCrmCommand(cmd: CrmWritebackCommand) {
  selectedCrmCommand.value = cmd
  showCrmDialog.value = true
}

function goCapture() {
  if (!journeyId.value) return
  persistReference()
  router.push({ name: 'MeetingCapture', params: { id: journeyId.value } })
}

function goCheckout() {
  if (!journeyId.value) return
  persistReference()
  router.push({ name: 'MeetingCheckout', params: { id: journeyId.value } })
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
                                modifications?: Record<string, unknown>[] | undefined, reason?: string) {
  deciding.value = true
  try {
    await decideCommand(commandId, { decision, modifications, reason: reason || '', actorId: 'current-user' })
    showCrmDialog.value = false
  } finally {
    deciding.value = false
  }
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  meetingNotes.value = restored.draftId ?? ''
  loadWorkspace()
  startPolling()
})

onBeforeUnmount(persistReference)

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="in-meeting" data-testid="p15-in-meeting">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="互动记录·会中工作区"
    />
    <div class="p15-body">
      <main class="p15-main">
        <div class="toolbar">
          <n-button size="small" :disabled="!journeyId" data-testid="p15-go-capture" @click="goCapture">
            进入实时捕获
          </n-button>
          <n-button size="small" :disabled="!journeyId" data-testid="p15-go-checkout" @click="goCheckout">
            进入离场确认
          </n-button>
          <DisabledAction
            label="记为正式 Claim"
            :disabled="true"
            reason="会中记录仅为草稿，禁止直接写成正式 Claim/Evidence"
            unlockPath="须经既有 HumanGate 后才能形成正式证据"
          />
        </div>

        <PageState :status="status" :error="gatesError || crmError || ''" idle-description="尚未加载会中工作区" @retry="loadWorkspace">
      <NLayout has-sider>
        <NLayoutSider bordered width="320" content-style="padding: 16px;">
          <h3>
            <NBadge :value="pendingGateCount" :max="99">待审批门禁</NBadge>
          </h3>
          <NSpin :show="gatesLoading">
            <NEmpty v-if="!pendingGates.length" description="暂无待审批门禁" />
            <NSpace v-else vertical :size="8">
              <NCard
                v-for="gate in pendingGates"
                :key="gate.gateId"
                size="small"
                hoverable
                style="cursor: pointer"
                @click="openGate(gate)"
              >
                <template #header>
                  <NTag size="small" type="warning">{{ GATE_TYPE_LABELS[gate.gateType] }}</NTag>
                </template>
                <div>{{ gate.subject }}</div>
              </NCard>
            </NSpace>
          </NSpin>

          <NDivider />

          <h3>
            <NBadge :value="pendingCrmCount" :max="99">CRM写回</NBadge>
          </h3>
          <NSpin :show="crmLoading">
            <NEmpty v-if="!crmCommands.filter(c => c.status === 'PENDING').length" description="暂无写回命令" />
            <NSpace v-else vertical :size="8">
              <NCard
                v-for="cmd in crmCommands.filter(c => c.status === 'PENDING')"
                :key="cmd.commandId"
                size="small"
                hoverable
                style="cursor: pointer"
                @click="openCrmCommand(cmd)"
              >
                <template #header>
                  <NTag size="small" type="info">{{ cmd.operation }}</NTag>
                </template>
                <div>{{ cmd.targetEntity }}</div>
              </NCard>
            </NSpace>
          </NSpin>
        </NLayoutSider>

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
                  placeholder="在此记录会议要点（候选草稿，非正式 Claim）"
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
      </NLayout>
    </PageState>
      </main>

      <GuidancePanel
        next-step="审批待决门禁 → 确认 CRM 写回 → 进入离场确认"
        business-rule="会中记录仅为候选草稿；正式 Claim/Evidence 须经 HumanGate 审批。"
        exception="门禁加载失败时保持上下文，展示原因与重试按钮。"
        contract-usage="REUSE_EXISTING：消费既有 HumanGate / CRMWriteback 契约；无支持能力时禁用或降级。"
      >
        <p class="gp-note">草稿不等于事实；AI 输出必须经人工确认才能写入权威。</p>
      </GuidancePanel>
    </div>

    <HumanGateDialog
      v-model:show="showGateDialog"
      :gate="selectedGate"
      :loading="deciding"
      @decide="handleGateDecide"
    />
    <CrmWritebackApproval
      v-model:show="showCrmDialog"
      :command="selectedCrmCommand"
      :loading="deciding"
      @decide="handleCrmDecide"
    />
  </div>
</template>

<style scoped>
.p15-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.p15-main {
  flex: 1;
  min-width: 0;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
h3 {
  margin: 0 0 12px;
}
</style>
