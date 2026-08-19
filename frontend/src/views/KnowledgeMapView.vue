<template>
  <div class="knowledge-map">
    <div class="page-header">
      <h1>知识地图（只读）</h1>
      <p class="subtitle">
        业务场景 → 知识域 → 知识条目(KI) → 知识要素(KE)，权威源受控导航（人机共读）
      </p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-box">知识地图加载中...</div>

    <!-- 错误 -->
    <div v-else-if="error" class="error-box">
      <p>加载失败：{{ error }}</p>
      <button class="retry-btn" @click="load">重试</button>
    </div>

    <!-- 成功：KI 分组折叠 -->
    <n-collapse v-else accordion class="ki-collapse">
      <n-collapse-item v-for="ki in itemOrder" :key="ki" :title="kiTitle(ki)" :name="ki">
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
              <p class="element-source" v-if="element.source?.sourceRef">
                来源：{{ element.source.sourceRef }}
              </p>
            </div>
          </div>
          <div v-if="grouped[ki].length === 0" class="empty-row">该知识条目暂无要素</div>
        </div>
      </n-collapse-item>
    </n-collapse>

    <!-- 空状态 -->
    <div v-if="!loading && !error && itemOrder.length === 0" class="empty-box">
      暂无知识地图数据（请确认后端知识要素已资产化）。
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NCollapse, NCollapseItem } from 'naive-ui'
import { fetchKnowledgeMap, type KnowledgeElement } from '../api/knowledge'

const loading = ref(true)
const error = ref('')
const grouped = ref<Record<string, KnowledgeElement[]>>({})

// 按 KI 顺序展示（KI-009 在前，其余按字母序）
const itemOrder = computed(() => {
  const keys = Object.keys(grouped.value)
  return keys.sort((a, b) => {
    if (a === 'KI-009') return -1
    if (b === 'KI-009') return 1
    return a.localeCompare(b)
  })
})

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
  try {
    grouped.value = await fetchKnowledgeMap()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.knowledge-map {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}
.page-header h1 {
  font-size: 24px;
  margin: 0 0 4px;
  color: #1a1a2e;
}
.subtitle {
  color: #666;
  margin: 0 0 20px;
  font-size: 13px;
}
.ki-collapse {
  margin-top: 8px;
}
.ki-elements {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.element-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 14px;
  background: #fafbfc;
}
.element-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.element-id {
  font-family: monospace;
  font-size: 12px;
  color: #333;
  background: #eef2ff;
  padding: 2px 6px;
  border-radius: 4px;
}
.element-name {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 14px;
}
.kind-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid #c7d2fe;
  color: #4338ca;
  background: #eef2ff;
}
.authority-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
}
.auth-authoritative {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #86efac;
}
.auth-reference {
  background: #e0f2fe;
  color: #075985;
  border: 1px solid #7dd3fc;
}
.auth-derived {
  background: #fef9c3;
  color: #854d0e;
  border: 1px solid #fde047;
}
.status-tag {
  font-size: 11px;
  color: #666;
  border: 1px solid #e5e7eb;
  padding: 1px 6px;
  border-radius: 4px;
}
.element-content {
  margin: 0;
  color: #374151;
  font-size: 13px;
  line-height: 1.5;
}
.element-source {
  margin: 6px 0 0;
  color: #888;
  font-size: 12px;
}
.loading-box,
.empty-box {
  padding: 40px;
  text-align: center;
  color: #666;
}
.error-box {
  padding: 20px;
  text-align: center;
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
}
.retry-btn {
  margin-top: 12px;
  padding: 6px 16px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.empty-row {
  color: #999;
  font-size: 13px;
  padding: 8px;
}
</style>
