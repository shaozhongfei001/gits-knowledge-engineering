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

const router = useRouter()
const customers = ref<Customer[]>([])
const loading = ref(true)
const error = ref('')

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
  padding: 24px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 24px;
  color: #003366;
  margin: 0 0 4px 0;
}
.subtitle {
  color: #8c8c8c;
  font-size: 14px;
  margin: 0;
}
.stats-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #003366, #004d99);
  border-radius: 8px;
  color: #fff;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
}
.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}
.customer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: #8c8c8c;
}
.error-state {
  padding: 40px 0;
}
</style>
