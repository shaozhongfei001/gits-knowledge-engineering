<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>客户经营概览</h1>
      <p class="subtitle">管理客户关系，洞察业务机会</p>
    </div>

    <div v-if="loading" class="loading-state">
      <n-spin size="large" />
      <span>加载客户数据...</span>
    </div>

    <div v-else-if="error" class="error-state">
      <n-result status="error" title="加载失败" :description="error">
        <template #footer>
          <n-button @click="loadCustomers">重试</n-button>
        </template>
      </n-result>
    </div>

    <template v-else>
      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-value">{{ customers.length }}</span>
          <span class="stat-label">客户总数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ highRiskCount }}</span>
          <span class="stat-label">高风险客户</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ strategicCount }}</span>
          <span class="stat-label">战略客户</span>
        </div>
        <div class="stat-item clickable" @click="router.push({ name: 'AuditTrace' })">
          <span class="stat-value">{{ pendingGateCount }}</span>
          <span class="stat-label">待审批</span>
        </div>
      </div>

      <div class="customer-grid">
        <CustomerCard
          v-for="customer in customers"
          :key="customer.customerId"
          :customer="customer"
          @click="goToCustomer(customer.customerId)"
        />
      </div>

      <n-empty v-if="!customers.length" description="暂无客户数据" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NResult, NButton, NEmpty } from 'naive-ui'
import CustomerCard from '../components/CustomerCard.vue'
import { fetchCustomers } from '../api/engagement'
import type { Customer, RiskLevel, CustomerTier } from '../api/engagement'
import { fetchHumanGates } from '../api/v11'

const router = useRouter()
const customers = ref<Customer[]>([])
const loading = ref(true)
const error = ref('')
const pendingGateCount = ref(0)

const highRiskCount = computed(() =>
  customers.value.filter(c => c.riskLevel === 'HIGH').length
)
const strategicCount = computed(() =>
  customers.value.filter(c => c.customerTier === 'STRATEGIC').length
)

function goToCustomer(id: string) {
  router.push({ name: 'CustomerOperatingView', params: { id } })
}

async function loadCustomers() {
  loading.value = true
  error.value = ''
  try {
    customers.value = await fetchCustomers()
    // 加载待审批门禁数量
    const gates = await fetchHumanGates({ status: 'PENDING' })
    pendingGateCount.value = gates.length
  } catch (e: any) {
    error.value = e.message || '无法获取客户列表'
  } finally {
    loading.value = false
  }
}

onMounted(loadCustomers)
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
}
.page-header {
  margin-bottom: var(--space-6);
}
.page-header h1 {
  font-size: var(--text-2xl);
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
  font-weight: 600;
}
.subtitle {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  margin: 0;
}
.stats-bar {
  display: flex;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
  padding: var(--space-5) var(--space-6);
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-light));
  border-radius: var(--radius-lg);
  color: var(--text-inverse);
  box-shadow: var(--shadow-md);
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}
.stat-value {
  font-size: var(--text-3xl);
  font-weight: 700;
}
.stat-label {
  font-size: var(--text-xs);
  opacity: 0.85;
  margin-top: 2px;
}
.customer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-4);
}
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12) 0;
  color: var(--text-tertiary);
}
.error-state {
  padding: var(--space-10) 0;
}
</style>
