<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { preparePrevisit, type PreparedPrevisitResponse } from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P13'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()
const { customerId, journeyId, operatingCaseId, rmId } = useEngagementContext()

const prepared = ref<PreparedPrevisitResponse | null>(null)
const loading = ref(true)
const error = ref('')
const requested = ref(false)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (journeyId.value ? journeyId.value : '缺对象'))

const sources = computed(() => {
  if (!prepared.value) return []
  const rows: { kind: string; id: string; summary: string }[] = []
  if (prepared.value.outreachScript?.scriptId) {
    rows.push({
      kind: '外联脚本',
      id: prepared.value.outreachScript.scriptId,
      summary: prepared.value.outreachScript.objective || prepared.value.outreachScript.openingLine || '',
    })
  }
  if (prepared.value.meetingScript?.scriptId) {
    rows.push({
      kind: '会面脚本',
      id: prepared.value.meetingScript.scriptId,
      summary: prepared.value.meetingScript.meetingObjective || prepared.value.meetingScript.previsitSummary || '',
    })
  }
  if (prepared.value.previsitReport?.reportId) {
    rows.push({
      kind: '访前报告',
      id: prepared.value.previsitReport.reportId,
      summary: prepared.value.previsitReport.visitObjective || '',
    })
  }
  if (prepared.value.battleCard?.cardId) {
    rows.push({
      kind: '速战卡',
      id: prepared.value.battleCard.cardId,
      summary: prepared.value.battleCard.visitObjective || '',
    })
  }
  return rows
})

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    customerId: customerId.value || undefined,
    recordId: journeyId.value || undefined,
    viewId: 'previsit_evidence',
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function loadEvidence() {
  loading.value = true
  error.value = ''
  requested.value = true
  prepared.value = null
  if (!journeyId.value || !customerId.value || !operatingCaseId.value) {
    loading.value = false
    return
  }
  try {
    prepared.value = await preparePrevisit(
      journeyId.value,
      customerId.value,
      operatingCaseId.value,
      '访前调研',
      rmId.value,
    )
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法装配访前证据'
    prepared.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadEvidence)
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p13-previsit-evidence">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="访前知识证据装配"
    />
    <div class="toolbar">
      <DisabledAction
        label="生成无来源结论"
        :disabled="true"
        reason="无合同来源时禁止生成结论"
        unlockPath="先完成 preparePrevisit 并展示返回来源后只读消费"
      />
    </div>
    <PageState :status="status" :error="error" idle-description="尚未装配证据" @retry="loadEvidence">
      <p class="hint">只读消费既有 preparePrevisit 返回；无来源则空态，禁止无来源结论。</p>
      <ul v-if="sources.length" class="item-list" data-testid="p13-source-list">
        <li v-for="row in sources" :key="row.id" class="item">
          <span class="kind">来源·{{ row.kind }}</span>
          <span>{{ row.id }}</span>
          <span>{{ row.summary || '-' }}</span>
        </li>
      </ul>
      <p v-else class="empty">无来源，禁止生成无来源结论</p>
    </PageState>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
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
  grid-template-columns: 8rem 1fr 1.4fr;
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
