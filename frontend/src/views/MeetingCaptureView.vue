<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { fetchJourney, type CustomerJourney } from '../api/engagement'
import { fetchLatestRecordingConsent } from '../api/v11'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'
import GuidancePanel from '../components/shell/GuidancePanel.vue'

const PAGE_ID = 'P16'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()
const { journeyId } = useEngagementContext()

const journey = ref<CustomerJourney | null>(null)
const notes = ref('')
const transcriptionFailed = ref(false)
const loading = ref(true)
const error = ref('')
const requested = ref(false)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: journey.value != null || requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (journey.value?.customerName || journeyId.value || '缺对象'))

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    recordId: journeyId.value || undefined,
    viewId: 'meeting_capture',
    draftId: notes.value || undefined,
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function tryTranscribe() {
  if (!journeyId.value) {
    transcriptionFailed.value = true
    return
  }
  try {
    const consent = await fetchLatestRecordingConsent(journeyId.value)
    if (consent.consentStatus !== 'GRANTED') {
      transcriptionFailed.value = true
      return
    }
    transcriptionFailed.value = true
  } catch {
    transcriptionFailed.value = true
  }
}

async function loadCapture() {
  loading.value = true
  error.value = ''
  requested.value = true
  if (!journeyId.value) {
    journey.value = null
    loading.value = false
    transcriptionFailed.value = true
    return
  }
  try {
    journey.value = await fetchJourney(journeyId.value)
    await tryTranscribe()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法加载会中捕获上下文'
    journey.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  notes.value = restored.draftId ?? ''
  loadCapture()
})
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p16-meeting-capture">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="会中实时捕获"
    />
    <div class="p16-body">
      <main class="p16-main">
        <div class="toolbar">
          <span class="candidate" data-testid="p16-candidate-label">候选 · 非正式 Claim / Evidence</span>
          <DisabledAction
            label="提交正式 Claim"
            :disabled="true"
            reason="会中草稿不是正式 Claim，禁止直接 recordClaim"
            unlockPath="须经既有 HumanGate 完成后才能形成正式证据"
          />
        </div>
        <PageState :status="status" :error="error" idle-description="尚未打开捕获" @retry="loadCapture">
          <p v-if="transcriptionFailed" class="banner" data-testid="p16-manual-fallback">
            转写失败，已切手工速记。草稿仅保存在本地。
          </p>
          <label class="hint">手工速记（候选）</label>
          <textarea
            v-model="notes"
            class="draft"
            data-testid="p16-draft"
            rows="8"
            placeholder="记录会中要点，此内容为候选草稿"
            @change="persistReference"
          />
          <p v-if="!notes" class="empty">暂无捕获草稿</p>
        </PageState>
      </main>

      <GuidancePanel
        next-step="完成会中捕获 → 返回会中工作区 → 审批门禁"
        business-rule="会中草稿仅为候选，禁止直接 recordClaim；须经 HumanGate 审批。"
        exception="转写失败时自动降级为手工速记；草稿仅存本地，刷新丢失。"
        contract-usage="REUSE_EXISTING：消费既有 Journey / RecordingConsent 契约。"
      >
        <p class="gp-note">草稿 ≠ 事实；AI 输出须经人工确认。</p>
      </GuidancePanel>
    </div>
  </div>
</template>

<style scoped>
.p16-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.p16-main {
  flex: 1;
  min-width: 0;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
.candidate {
  font-size: 12px;
  color: var(--color-warning);
  padding: 6px 8px;
  border: 1px dashed var(--border-normal);
  border-radius: 6px;
}
.banner {
  color: var(--color-warning);
  font-size: 13px;
  margin-bottom: 8px;
}
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
}
.draft {
  width: 100%;
  margin-top: 8px;
  padding: 10px;
  border: 1px solid var(--border-normal);
  border-radius: 8px;
  font: inherit;
}
</style>
