<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { fetchKycGapProfile, type KycGapProfile } from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P12'
const OBJECT_TYPE = '互动 Interaction'

const router = useRouter()
const pageRefs = usePageReferenceStore()
const { customerId, journeyId, operatingCaseId, rmId } = useEngagementContext()

const profile = ref<KycGapProfile | null>(null)
const loading = ref(true)
const error = ref('')
const requested = ref(false)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: profile.value != null || requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (customerId.value ? customerId.value : '缺对象'))

const gapItems = computed(() => {
  if (!profile.value) return []
  return [
    ...profile.value.unknownItems.map(item => ({ kind: '未知', item })),
    ...profile.value.partialKnownItems.map(item => ({ kind: '部分已知', item })),
    ...profile.value.staleItems.map(item => ({ kind: '过期', item })),
    ...profile.value.conflictingOrAmbiguousItems.map(item => ({ kind: '冲突', item })),
    ...profile.value.priorityQuestions.map(item => ({ kind: '优先问题', item })),
    ...profile.value.knownItems.map(item => ({ kind: '已知', item })),
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
  if (!customerId.value) {
    profile.value = null
    loading.value = false
    return
  }
  try {
    profile.value = await fetchKycGapProfile(customerId.value)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法获取 KYC 缺口画像'
    profile.value = null
  } finally {
    loading.value = false
  }
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
    <div class="toolbar">
      <button type="button" class="link-btn" data-testid="p12-go-evidence" @click="goEvidence">
        进入证据装配
      </button>
      <DisabledAction
        label="自动填补缺口"
        :disabled="true"
        reason="缺口填补为写操作且无本 Loop 合同"
        unlockPath="待合同批准后由后续 Loop 启用"
      />
    </div>
    <PageState :status="status" :error="error" idle-description="尚未请求缺口画像" @retry="loadProfile">
      <p v-if="!customerId" class="empty">缺客户对象，暂无访前缺口</p>
      <template v-else-if="profile">
        <p class="hint">数据来源：GET /api/v1/engagement/kyc/{customerId}/gap-profile（fetchKycGapProfile）。</p>
        <ul v-if="gapItems.length" class="item-list" data-testid="p12-gap-list">
          <li v-for="(row, idx) in gapItems" :key="idx" class="item">
            <span class="kind">{{ row.kind }}</span>
            <span>{{ row.item }}</span>
          </li>
        </ul>
        <p v-else class="empty">暂无信息缺口</p>
      </template>
    </PageState>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
.link-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-surface);
  cursor: pointer;
}
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
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
</style>
