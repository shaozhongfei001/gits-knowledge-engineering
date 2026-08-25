<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { executePrevisit, type PrevisitExecutionResponse } from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P14'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()
const { customerId, journeyId, operatingCaseId } = useEngagementContext()

const pack = ref<PrevisitExecutionResponse | null>(null)
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
    viewId: 'previsit_pack',
    draftId: confirmed.value ? 'confirmed' : undefined,
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
    pack.value = await executePrevisit(journeyId.value, customerId.value, operatingCaseId.value, '访前包预览')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '执行访前包失败'
    pack.value = null
  } finally {
    loading.value = false
  }
}

function retry() {
  error.value = ''
  pack.value = null
  loading.value = false
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  confirmed.value = restored.draftId === 'confirmed'
})
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p14-previsit-pack">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="访前包预览"
    />
    <PageState :status="status" :error="error" idle-description="尚未预览访前包" @retry="retry">
      <p v-if="!canExecute" class="empty">缺对象，暂无访前包可执行</p>
      <DisabledAction
        v-if="!canExecute"
        label="执行访前包"
        :disabled="true"
        reason="缺少旅程/客户对象，禁止调用 executePrevisit"
        unlockPath="从互动记录·访前路径选择客户并启动旅程后再预览"
      />
      <template v-else>
        <p class="hint">可调用既有 executePrevisit，但必须人工确认。失败走四态，不自动提交。</p>
        <label class="confirm">
          <input v-model="confirmed" type="checkbox" data-testid="p14-confirm" />
          我已预览访前包并确认执行
        </label>
        <button
          type="button"
          class="link-btn"
          data-testid="p14-execute"
          :disabled="!confirmed"
          @click="runExecute"
        >
          执行访前包
        </button>
        <div v-if="pack" class="item" data-testid="p14-pack-result">
          <span>来源·访前报告 {{ pack.previsitReport.reportId }}</span>
          <span>{{ pack.previsitReport.visitObjective }}</span>
          <span>速战卡 {{ pack.battleCard.cardId }}</span>
        </div>
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
.item {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 13px;
}
</style>
