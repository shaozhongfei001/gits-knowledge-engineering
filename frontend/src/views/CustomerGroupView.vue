<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import CustomerRecordTabs from '../components/shell/CustomerRecordTabs.vue'
import { fetchCustomer, type Customer } from '../api/engagement'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { usePageReferenceStore } from '../stores/pageReference'

const PAGE_ID = 'P05'
const OBJECT_TYPE = '客户 Account'

const route = useRoute()
const pageRefs = usePageReferenceStore()

const customerId = computed(() => String(route.params.id || ''))
const customer = ref<Customer | null>(null)
const loading = ref(true)
const error = ref('')
const requested = ref(false)

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: customer.value != null,
    requested: requested.value,
  }),
)

const title = computed(() => customer.value?.customerName
  ? `${customer.value.customerName} · 集团关系`
  : '客户记录·集团关系')

const groupLabel = computed(() => {
  if (customer.value?.groupFlag === true) {
    return '集团客户'
  }
  return '非集团客户'
})

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    recordId: customerId.value,
    viewId: 'group_readonly',
    subtab: 'group',
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function loadRecord() {
  loading.value = true
  error.value = ''
  requested.value = true
  try {
    customer.value = await fetchCustomer(customerId.value)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法获取集团关系'
    customer.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadRecord)
watch(customerId, loadRecord)
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="group-view" data-testid="p05-group">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      object-status="集团关系"
      :title="title"
    />
    <CustomerRecordTabs :customer-id="customerId" />
    <div class="toolbar">
      <DisabledAction
        label="发起核验"
        :disabled="true"
        reason="C2：集团关系核验写操作无本 Loop 合同，仅展示既有 Customer 字段"
        unlockPath="待合同批准后由后续 Loop 启用核验 Action"
      />
    </div>
    <PageState :status="status" :error="error" idle-description="尚未请求集团关系" @retry="loadRecord">
      <p class="hint">只读字段来自既有 Customer（groupFlag、relationshipSummary），未请求关系图谱 API。</p>
      <dl class="facts">
        <div>
          <dt>客户名称</dt>
          <dd>{{ customer?.customerName }}</dd>
        </div>
        <div>
          <dt>集团标识</dt>
          <dd>{{ groupLabel }}</dd>
        </div>
        <div>
          <dt>上市状态</dt>
          <dd>{{ customer?.listedStatus || '-' }}</dd>
        </div>
        <div>
          <dt>关系摘要</dt>
          <dd>{{ customer?.relationshipSummary || '暂无集团关系说明' }}</dd>
        </div>
      </dl>
    </PageState>
  </div>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.hint {
  color: var(--text-tertiary);
  font-size: 13px;
}
.facts {
  display: grid;
  gap: 12px;
  margin: 16px 0 0;
}
.facts div {
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
}
dt {
  font-size: 12px;
  color: var(--text-tertiary);
}
dd {
  margin: 4px 0 0;
  font-size: 14px;
}
</style>
