<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { NTimeline, NTimelineItem, NDescriptions, NDescriptionsItem, NSpin, NAlert, NEmpty } from 'naive-ui'
import { fetchEvidenceVersions, type EvidenceVersion } from '../api/v11'

const props = defineProps<{
  evidenceId: string
}>()

const data = ref<EvidenceVersion[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function loadData() {
  if (!props.evidenceId) return
  loading.value = true
  error.value = null
  try {
    data.value = await fetchEvidenceVersions(props.evidenceId)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载证据版本失败'
  } finally {
    loading.value = false
  }
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString('zh-CN')
}

onMounted(loadData)
watch(() => props.evidenceId, loadData)
</script>

<template>
  <div class="evidence-version-chain">
    <NSpin :show="loading">
      <NAlert v-if="error" type="error" :title="error" />

      <NEmpty v-if="data.length === 0 && !loading" description="暂无版本记录" />

      <NTimeline v-if="data.length > 0">
        <NTimelineItem
          v-for="version in data"
          :key="version.versionId"
          :type="version.version === (data[0]?.version ?? 0) ? 'success' : 'default'"
          :title="`版本 ${version.version}`"
        >
          <NDescriptions bordered size="small" :column="1" label-placement="left">
            <NDescriptionsItem label="版本ID">{{ version.versionId }}</NDescriptionsItem>
            <NDescriptionsItem label="变更描述">{{ version.changeDescription || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="上一版本">{{ version.previousVersionId || '初始版本' }}</NDescriptionsItem>
            <NDescriptionsItem label="变更人">{{ version.createdBy || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="变更时间">{{ formatTime(version.createdAt) }}</NDescriptionsItem>
          </NDescriptions>
        </NTimelineItem>
      </NTimeline>
    </NSpin>
  </div>
</template>

<style scoped>
.evidence-version-chain {
  padding: 16px;
}
</style>
