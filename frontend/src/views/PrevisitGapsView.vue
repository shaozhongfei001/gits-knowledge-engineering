<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import GuidancePanel from '../components/shell/GuidancePanel.vue'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'
import { usePrevisitStore } from '../stores/previsit'

const PAGE_ID = 'P12'
const OBJECT_TYPE = '互动 Interaction'

const router = useRouter()
const pageRefs = usePageReferenceStore()
const previsitStore = usePrevisitStore()
const { customerId, journeyId, operatingCaseId, rmId } = useEngagementContext()

const loading = ref(true)
const error = ref('')
const requested = ref(false)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value || previsitStore.kycLoading,
    error: error.value || previsitStore.kycError,
    hasData: previsitStore.kycGapProfile != null || requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (customerId.value || '缺对象'))

const gapItems = computed(() => {
  const profile = previsitStore.kycGapProfile
  if (!profile) return []
  return [
    ...profile.unknownItems.map((item) => ({ kind: '未知', item })),
    ...profile.partialKnownItems.map((item) => ({ kind: '部分已知', item })),
    ...profile.staleItems.map((item) => ({ kind: '过期', item })),
    ...profile.conflictingOrAmbiguousItems.map((item) => ({ kind: '冲突', item })),
    ...profile.priorityQuestions.map((item) => ({ kind: '优先问题', item })),
    ...profile.knownItems.map((item) => ({ kind: '已知', item })),
  ]
})

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    customerId: customerId.value || undefined,
    recordId: journeyId.value || undefined,
    viewId: 'previsit_gaps',
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function loadProfile() {
  loading.value = true
  error.value = ''
  requested.value = true
  // 同步 store 上下文（支持深链直接进入 P12）
  previsitStore.setContext({
    journeyId: journeyId.value,
    operatingCaseId: operatingCaseId.value,
    customerId: customerId.value,
    rmId: rmId.value,
  })
  if (customerId.value) {
    await previsitStore.loadKycGap(customerId.value)
  }
  loading.value = false
}

function goEvidence() {
  persistReference()
  router.push({
    path: '/engagement/previsit/evidence',
    query: {
      ...(customerId.value ? { customerId: customerId.value } : {}),
      ...(journeyId.value ? { journeyId: journeyId.value } : {}),
      ...(operatingCaseId.value ? { operatingCaseId: operatingCaseId.value } : {}),
      ...(rmId.value ? { rmId: rmId.value } : {}),
    },
  })
}

onMounted(loadProfile)
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p12-previsit-gaps">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="访前目标与信息缺口"
    />

    <div class="p12-layout">
      <main class="p12-main">
        <div class="toolbar">
          <button
            type="button"
            class="link-btn link-btn--primary"
            data-testid="p12-go-evidence"
            :disabled="!customerId"
            @click="goEvidence"
          >
            进入证据装配
          </button>
          <DisabledAction
            label="自动填补缺口"
            :disabled="true"
            reason="缺口填补为写操作且无本 Loop 合同"
            unlockPath="待合同批准后由后续 Loop 启用"
          />
        </div>
        <PageState :status="status" :error="error || previsitStore.kycError" idle-description="尚未请求缺口画像" @retry="loadProfile">
          <p v-if="!customerId" class="empty">缺客户对象，暂无访前缺口</p>
          <template v-else-if="previsitStore.kycGapProfile">
            <p class="hint">数据来源：GET /api/v1/engagement/kyc/{customerId}/gap-profile（纯查询，不触发 KERT）。</p>
            <ul v-if="gapItems.length" class="item-list" data-testid="p12-gap-list">
              <li v-for="(row, idx) in gapItems" :key="idx" class="item">
                <span class="kind">{{ row.kind }}</span>
                <span>{{ row.item }}</span>
              </li>
            </ul>
            <p v-else class="empty">暂无信息缺口</p>
          </template>
        </PageState>
      </main>

      <GuidancePanel
        next-step="点击信息缺口直接创建问题；完成阻断项后才能生成访前包"
        business-rule="工作假设必须显式标识，信息缺口可转为会谈问题。"
        exception="依赖失败或权限不足时保持上下文，展示原因、重试与返回路径。"
        contract-usage="REUSE_EXISTING：仅消费既有查询、状态与对象契约；无支持能力时禁用或降级。"
      >
        <p class="gp-note">缺口信息是触发一键访前的决策依据；本页不触发 KERT。</p>
      </GuidancePanel>
    </div>
  </div>
</template>

<style scoped>
.p12-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
.link-btn {
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-surface);
  cursor: pointer;
}
.link-btn--primary {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  color: #fff;
  font-weight: 600;
}
.link-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
.item-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0;
}
.item {
  display: grid;
  grid-template-columns: 7rem 1fr;
  gap: 8px;
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 13px;
}
.kind {
  color: var(--text-tertiary);
}
@media (max-width: 900px) {
  .p12-layout {
    grid-template-columns: 1fr;
  }
}
</style>
