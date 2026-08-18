<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NDataTable, NTag, NSpin, NAlert, NSpace, NInput } from 'naive-ui'
import { fetchAuditTrace, type AuditTraceEntry } from '../api/v11'

const entityType = ref('')
const entityId = ref('')
const actorId = ref('')
const data = ref<AuditTraceEntry[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function loadData() {
  loading.value = true
  error.value = null
  try {
    data.value = await fetchAuditTrace({
      entityType: entityType.value || undefined,
      entityId: entityId.value || undefined,
      actorId: actorId.value || undefined
    })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载审计追踪失败'
  } finally {
    loading.value = false
  }
}

const columns = [
  { title: '时间', key: 'occurredAt', width: 180 },
  { title: '实体类型', key: 'entityType', width: 120 },
  { title: '实体ID', key: 'entityId', width: 120 },
  { title: '操作', key: 'operation', width: 100 },
  { title: '操作人', key: 'actorId', width: 120 },
  { title: '角色', key: 'actorRole', width: 100 },
  { title: '关联ID', key: 'correlationId', width: 120 }
]

onMounted(loadData)
</script>

<template>
  <div class="audit-trace-list">
    <NSpace vertical :size="12">
      <NSpace>
        <NInput v-model:value="entityType" placeholder="实体类型" clearable style="width: 150px" />
        <NInput v-model:value="entityId" placeholder="实体ID" clearable style="width: 150px" />
        <NInput v-model:value="actorId" placeholder="操作人" clearable style="width: 150px" />
      </NSpace>

      <NAlert v-if="error" type="error" :title="error" />

      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="data"
          :bordered="true"
          size="small"
          :pagination="{ pageSize: 20 }"
          :scroll-x="900"
        />
      </NSpin>
    </NSpace>
  </div>
</template>

<style scoped>
.audit-trace-list {
  padding: 16px;
}
</style>
