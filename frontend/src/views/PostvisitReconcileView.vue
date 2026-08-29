<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import GuidancePanel from '../components/shell/GuidancePanel.vue'
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

const suggestion = computed(() => {
  if (!result.value) return ''
  if (result.value.allCommandsRequireHumanConfirm) {
    return `共 ${result.value.crmCommandCount} 条 CRM 写回命令需人工确认，请进入 CRM 受控回写逐条预览。`
  }
  return '访后事实已对账，CRM 写回命令已生成。'
})

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

    <div class="p18-layout">
      <main class="p18-main">
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

            <div v-if="result" class="suggestion" data-testid="p18-suggestion">
              <b>处理建议：</b>{{ suggestion }}
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
      </main>

      <GuidancePanel
        next-step="规则给出处理建议；最终状态由有权人员确认并记录理由"
        business-rule="冲突事实必须保留原值、新值、证据和处理理由。"
        exception="冲突无法裁决时保留双方版本并升级给有权人员。"
        contract-usage="REUSE_EXISTING：仅消费既有查询、状态与对象契约；无支持能力时禁用或降级。"
      >
        <p class="gp-note">冲突不得自动合并；并列显示来源、版本、时间与建议处理，最终由有权人员裁决。</p>
      </GuidancePanel>
    </div>
  </div>
</template>

<style scoped>
.p18-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
}
.gp-note {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.5;
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
.suggestion {
  margin: 12px 0;
  padding: 10px 12px;
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  border-radius: 8px;
  font-size: 13px;
  color: var(--color-warning);
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
@media (max-width: 900px) {
  .p18-layout {
    grid-template-columns: 1fr;
  }
}
</style>
