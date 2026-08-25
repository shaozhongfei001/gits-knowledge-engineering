<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { executePostvisit, type PostvisitExecutionResponse } from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P18'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()
const { customerId, journeyId, operatingCaseId } = useEngagementContext()

const previousClaim = ref('')
const newEvidence = ref('')
const result = ref<PostvisitExecutionResponse | null>(null)
const confirmed = ref(false)
const loading = ref(false)
const error = ref('')
const requested = ref(true)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (journeyId.value ? journeyId.value : '缺对象'))
const canExecute = computed(() => Boolean(journeyId.value && customerId.value))

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    customerId: customerId.value || undefined,
    recordId: journeyId.value || undefined,
    viewId: 'postvisit_reconcile',
    draftId: previousClaim.value || undefined,
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function runExecute() {
  if (!confirmed.value || !canExecute.value) {
    return
  }
  loading.value = true
  error.value = ''
  try {
    const response = await executePostvisit(
      journeyId.value,
      customerId.value,
      operatingCaseId.value,
      newEvidence.value || previousClaim.value || '访后事实对账',
    )
    result.value = response
    const contractSide = [
      `analysisId=${response.analysisId}`,
      `transcriptId=${response.transcriptId}`,
      `internalReportId=${response.internalReportId}`,
      `crmReportId=${response.crmReportId}`,
    ].join('；')
    newEvidence.value = newEvidence.value
      ? `${newEvidence.value}\n${contractSide}`
      : contractSide
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '执行访后对账失败'
  } finally {
    loading.value = false
  }
}

function retry() {
  error.value = ''
  loading.value = false
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  previousClaim.value = restored.draftId ?? ''
})
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p18-postvisit">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="访后事实对账"
    />
    <PageState :status="status" :error="error" idle-description="尚未开始对账" @retry="retry">
      <p v-if="!canExecute" class="empty">缺对象，暂无访后对账可执行</p>
      <DisabledAction
        v-if="!canExecute"
        label="执行访后对账"
        :disabled="true"
        reason="缺少旅程/客户对象，禁止调用 executePostvisit"
        unlockPath="从互动记录选择客户并启动旅程后再对账"
      />
      <template v-else>
        <p class="hint">冲突保留双方版本，不静默覆盖。执行走既有 executePostvisit，且须人工确认。</p>
        <div class="dual" data-testid="p18-both-versions">
          <section data-testid="p18-previous-claim">
            <h2>先前主张</h2>
            <textarea
              v-model="previousClaim"
              data-testid="p18-previous-input"
              rows="6"
              placeholder="保留访前/会中主张，对账时不覆盖"
            />
          </section>
          <section data-testid="p18-new-evidence">
            <h2>新证据 / 合同结果</h2>
            <textarea
              v-model="newEvidence"
              data-testid="p18-new-input"
              rows="6"
              placeholder="访后新证据；冲突时与左侧并存"
            />
            <p v-if="result" class="hint">合同结果 analysisId={{ result.analysisId }}，命令数 {{ result.crmCommandCount }}</p>
          </section>
        </div>
        <label class="confirm">
          <input v-model="confirmed" type="checkbox" data-testid="p18-confirm" />
          我已对照双方版本并确认执行访后对账
        </label>
        <button
          type="button"
          class="link-btn"
          data-testid="p18-execute"
          :disabled="!confirmed"
          @click="runExecute"
        >
          执行访后对账
        </button>
      </template>
    </PageState>
  </div>
</template>

<style scoped>
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
}
.dual {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 12px 0;
}
h2 {
  margin: 0 0 8px;
  font-size: 14px;
}
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--border-normal);
  border-radius: 8px;
  font: inherit;
}
.confirm {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 12px 0;
  font-size: 13px;
}
.link-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-surface);
  cursor: pointer;
}
.link-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
