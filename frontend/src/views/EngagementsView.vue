<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { NInput } from 'naive-ui'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import {
  listInteractions,
  INTERACTION_CHANNEL_LABELS,
  type InteractionChannel,
  type ListedInteraction,
} from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P10'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()

const interactions = ref<ListedInteraction[]>([])
const loading = ref(true)
const error = ref('')
const requested = ref(false)
const filter = ref('')
const subtab = ref('list')

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: interactions.value.length > 0 || requested.value,
    requested: requested.value,
  }),
)

const visible = computed(() => {
  const q = filter.value.trim()
  if (!q) {
    return interactions.value
  }
  return interactions.value.filter(item =>
    item.interactionId.includes(q)
    || item.customerId.includes(q)
    || (item.summary ?? '').includes(q)
    || String(item.channel).includes(q),
  )
})

function channelLabel(channel: string): string {
  return INTERACTION_CHANNEL_LABELS[channel as InteractionChannel] || channel
}

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    viewId: 'interaction_list',
    filter: filter.value,
    subtab: subtab.value,
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function loadInteractions() {
  loading.value = true
  error.value = ''
  requested.value = true
  try {
    interactions.value = await listInteractions()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法获取互动列表'
    interactions.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  filter.value = restored.filter ?? ''
  subtab.value = restored.subtab ?? 'list'
  loadInteractions()
})

onBeforeUnmount(persistReference)
</script>

<template>
  <div class="engagements-view" data-testid="p10-engagements">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      object-status="列表"
      title="互动对象主页"
    />
    <div class="toolbar">
      <DisabledAction
        label="同步日历"
        :disabled="true"
        reason="日历同步为写操作且无本 Loop 合同"
        unlockPath="待合同批准后由后续 Loop 启用"
      />
      <n-input
        v-model:value="filter"
        data-testid="p10-filter"
        placeholder="筛选互动编号、客户或摘要"
        style="max-width: 260px"
        @update:value="persistReference"
      />
    </div>
    <p class="hint">数据来源：GET /api/v1/interactions（operationId=listInteractions）。持续经营工作台仍在 /engagement，本页不替换它。</p>
    <PageState :status="status" :error="error" idle-description="尚未请求互动列表" @retry="loadInteractions">
      <ul v-if="visible.length" class="item-list" data-testid="p10-interaction-list">
        <li v-for="item in visible" :key="item.interactionId" class="item">
          <span class="id">{{ item.interactionId }}</span>
          <span>{{ item.customerId }}</span>
          <span>{{ channelLabel(String(item.channel)) }}</span>
          <span>{{ item.summary || item.transcript || '-' }}</span>
          <span>{{ item.interactionDate || item.createdAt || '-' }}</span>
        </li>
      </ul>
      <p v-else class="empty">暂无互动记录</p>
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
  grid-template-columns: 1fr 0.8fr 0.6fr 1.4fr 1fr;
  gap: 8px;
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 13px;
}
.id {
  font-family: var(--font-mono);
}
</style>
