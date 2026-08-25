<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { fetchKnowledgeMap, type KnowledgeElement } from '../api/knowledge'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P38'
const OBJECT_TYPE = '知识要素 KE（只读）'
const KNOWLEDGE_UNLOCK = '待产品适用边界写合同批准后由独立 Loop 启用；本页 KE 保持只读'

const pageRefs = usePageReferenceStore()
const loading = ref(true)
const error = ref('')
const requested = ref(false)
const grouped = ref<Record<string, KnowledgeElement[]>>({})

const itemOrder = computed(() => {
  const keys = Object.keys(grouped.value)
  return keys.sort((a, b) => {
    if (a === 'KI-009') return -1
    if (b === 'KI-009') return 1
    return a.localeCompare(b)
  })
})

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: requested.value,
    requested: requested.value,
  }),
)

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    viewId: 'knowledge_map_readonly',
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

function kiTitle(kiId: string): string {
  const first = grouped.value[kiId]?.[0]
  const count = grouped.value[kiId]?.length ?? 0
  return first ? `${kiId} · ${first.name}（${count} 要素）` : `${kiId}（${count} 要素）`
}

function kindClass(kind: string): string {
  return `kind-${kind.replace(/[^a-zA-Z]/g, '').toLowerCase()}` || ''
}

function authorityClass(authority: string): string {
  return `auth-${authority.toLowerCase()}`
}

async function load() {
  loading.value = true
  error.value = ''
  requested.value = true
  try {
    grouped.value = await fetchKnowledgeMap()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e)
    grouped.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="knowledge-map" data-testid="p38-knowledge-map">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      object-status="只读"
      title="知识卡与产品适用边界"
    />
    <div class="toolbar">
      <DisabledAction
        label="比较产品"
        :disabled="true"
        reason="产品适用边界比较写未授权，禁止比较产品写回"
        :unlock-path="KNOWLEDGE_UNLOCK"
      />
      <DisabledAction
        label="反馈知识"
        :disabled="true"
        reason="知识反馈写未授权，禁止从本页反馈知识"
        :unlock-path="KNOWLEDGE_UNLOCK"
      />
    </div>
    <p class="hint">KE 只读。权威源受控导航（人机共读）。本页不提供产品比较或知识反馈写回。</p>
    <PageState :status="status" :error="error" idle-description="尚未加载知识地图" @retry="load">
      <section v-if="itemOrder.length" class="ki-list" data-testid="p38-ki-list">
        <article v-for="ki in itemOrder" :key="ki" class="ki-group">
          <h2>{{ kiTitle(ki) }}</h2>
          <div class="ki-elements">
            <div v-for="element in grouped[ki]" :key="element.elementId" class="element-card">
              <div class="element-head">
                <span class="element-id">{{ element.elementId }}</span>
                <span class="element-name">{{ element.name }}</span>
                <span class="kind-tag" :class="kindClass(element.kind)">{{ element.kind }}</span>
                <span class="authority-tag" :class="authorityClass(element.source.authority)">
                  {{ element.source.authority }}
                </span>
                <span class="status-tag">{{ element.status }}</span>
              </div>
              <div class="element-body">
                <p class="element-content">{{ element.content }}</p>
                <p v-if="element.source?.sourceRef" class="element-source">
                  来源：{{ element.source.sourceRef }}
                </p>
              </div>
            </div>
            <div v-if="grouped[ki].length === 0" class="empty-row">该知识条目暂无要素</div>
          </div>
        </article>
      </section>
      <p v-else class="empty" data-testid="p38-empty">暂无知识地图数据（请确认后端知识要素已资产化）。</p>
    </PageState>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
}
.ki-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.ki-group h2 {
  margin: 0 0 8px;
  font-size: 15px;
}
.ki-elements {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.element-card {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 12px 14px;
  background: var(--bg-surface);
}
.element-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.element-id {
  font-family: ui-monospace, monospace;
  font-size: 12px;
}
.element-name {
  font-weight: 600;
  font-size: 14px;
}
.kind-tag,
.authority-tag,
.status-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid var(--border-light);
}
.auth-authoritative {
  background: #dcfce7;
  color: #166534;
}
.auth-reference {
  background: #e0f2fe;
  color: #075985;
}
.auth-derived {
  background: #fef9c3;
  color: #854d0e;
}
.element-content {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
}
.element-source,
.empty-row {
  margin: 6px 0 0;
  color: var(--text-tertiary);
  font-size: 12px;
}
</style>
