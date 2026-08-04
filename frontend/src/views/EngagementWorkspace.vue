<template>
  <div class="engagement-workspace">
    <div class="page-header">
      <h1>持续经营工作台</h1>
      <p class="subtitle">闭环流程管理，驱动客户关系持续深化</p>
    </div>

    <!-- 流程可视化 -->
    <div class="process-section">
      <h3 class="section-title">经营闭环流程</h3>
      <div class="process-flow">
        <div
          v-for="(step, index) in processSteps"
          :key="step.key"
          class="process-step"
          :class="{
            'step-active': currentStep === step.key,
            'step-completed': isStepCompleted(step.key),
            'step-pending': !isStepCompleted(step.key) && currentStep !== step.key
          }"
        >
          <div class="step-icon">{{ step.icon }}</div>
          <div class="step-name">{{ step.name }}</div>
          <div class="step-desc">{{ step.desc }}</div>
          <div v-if="index < processSteps.length - 1" class="step-arrow">&rarr;</div>
        </div>
      </div>
    </div>

    <!-- 操作面板 -->
    <div class="action-section">
      <h3 class="section-title">操作面板</h3>
      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-gi>
          <n-card hoverable class="action-card" @click="handleOutreach">
            <div class="action-icon">&#9993;</div>
            <div class="action-title">生成外联脚本</div>
            <div class="action-desc">基于客户画像和机会信号，自动生成外联沟通脚本</div>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card hoverable class="action-card" @click="handleMeeting">
            <div class="action-icon">&#128196;</div>
            <div class="action-title">生成会面脚本</div>
            <div class="action-desc">基于访前报告和KYC缺口，生成结构化会面提纲</div>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card hoverable class="action-card" @click="handlePrevisit">
            <div class="action-icon">&#128200;</div>
            <div class="action-title">执行访前报告</div>
            <div class="action-desc">汇总客户信息、KYC缺口、产品方案，生成访前报告</div>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card hoverable class="action-card" @click="handlePostvisit">
            <div class="action-icon">&#128203;</div>
            <div class="action-title">执行访后分析</div>
            <div class="action-desc">分析交互记录，提取关键发现、机会信号和事实对账</div>
          </n-card>
        </n-gi>
      </n-grid>
    </div>

    <!-- 脚本展示区域 -->
    <div v-if="scriptContent" class="script-section">
      <h3 class="section-title">
        {{ scriptType === 'OUTREACH' ? '外联脚本' : '会面脚本' }}
        <n-button text size="small" @click="scriptContent = ''">关闭</n-button>
      </h3>
      <n-card>
        <div class="script-content">{{ scriptContent }}</div>
      </n-card>
    </div>

    <!-- 报告展示区域 -->
    <div v-if="reportContent" class="report-section">
      <h3 class="section-title">
        {{ reportMode === 'previsit' ? '访前报告' : '访后分析' }}
        <n-button text size="small" @click="reportContent = ''">关闭</n-button>
      </h3>
      <n-card>
        <div class="report-content-display" v-html="reportContent" />
      </n-card>
    </div>

    <!-- 客户选择对话框 -->
    <n-modal v-model:show="showCustomerSelect" preset="dialog" title="选择客户">
      <n-input v-model:value="searchQuery" placeholder="搜索客户名称..." clearable />
      <div class="customer-select-list">
        <div
          v-for="customer in filteredCustomers"
          :key="customer.customerId"
          class="customer-select-item"
          @click="selectCustomer(customer)"
        >
          <span>{{ customer.customerName }}</span>
          <RiskBadge :level="customer.riskLevel" />
        </div>
        <n-empty v-if="!filteredCustomers.length" description="未找到客户" size="small" />
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NGrid, NGi, NCard, NButton, NModal, NInput, NEmpty, useMessage } from 'naive-ui'
import RiskBadge from '../components/RiskBadge.vue'
import {
  fetchCustomers, generateOutreachScript, generateMeetingScript,
  executePrevisit, executePostvisit
} from '../api/engagement'
import type { Customer } from '../api/engagement'

const message = useMessage()
const customers = ref<Customer[]>([])
const selectedCustomer = ref<Customer | null>(null)
const searchQuery = ref('')
const showCustomerSelect = ref(false)
const pendingAction = ref<'outreach' | 'meeting' | 'previsit' | 'postvisit' | null>(null)
const currentStep = ref('KYC_COLLECT')
const scriptContent = ref('')
const scriptType = ref<'OUTREACH' | 'MEETING'>('OUTREACH')
const reportContent = ref('')
const reportMode = ref<'previsit' | 'postvisit'>('previsit')

const processSteps = [
  { key: 'KYC_COLLECT', name: 'KYC采集', desc: '信息收集与认知', icon: '1' },
  { key: 'INSIGHT_ANALYSIS', name: '洞察分析', desc: '信号检测与评估', icon: '2' },
  { key: 'PRODUCT_MATCHING', name: '产品匹配', desc: '方案推荐与设计', icon: '3' },
  { key: 'PREVISIT_PREP', name: '访前准备', desc: '脚本与报告生成', icon: '4' },
  { key: 'POSTVISIT_REVIEW', name: '访后复盘', desc: '分析与事实对账', icon: '5' }
]

const filteredCustomers = computed(() => {
  if (!searchQuery.value) return customers.value.slice(0, 20)
  const q = searchQuery.value.toLowerCase()
  return customers.value.filter(c => c.customerName.toLowerCase().includes(q)).slice(0, 20)
})

function isStepCompleted(step: string): boolean {
  const order = processSteps.map(s => s.key)
  return order.indexOf(step) < order.indexOf(currentStep.value)
}

function handleOutreach() {
  pendingAction.value = 'outreach'
  showCustomerSelect.value = true
}
function handleMeeting() {
  pendingAction.value = 'meeting'
  showCustomerSelect.value = true
}
function handlePrevisit() {
  pendingAction.value = 'previsit'
  showCustomerSelect.value = true
}
function handlePostvisit() {
  pendingAction.value = 'postvisit'
  showCustomerSelect.value = true
}

async function selectCustomer(customer: Customer) {
  selectedCustomer.value = customer
  showCustomerSelect.value = false
  const action = pendingAction.value
  pendingAction.value = null

  try {
    if (action === 'outreach') {
      const result = await generateOutreachScript(customer.customerId)
      scriptType.value = 'OUTREACH'
      scriptContent.value = result.content
      message.success('外联脚本已生成')
    } else if (action === 'meeting') {
      const result = await generateMeetingScript(customer.customerId)
      scriptType.value = 'MEETING'
      scriptContent.value = result.content
      message.success('会面脚本已生成')
    } else if (action === 'previsit') {
      const result = await executePrevisit(customer.customerId)
      reportMode.value = 'previsit'
      reportContent.value = formatPrevisitReport(result)
      message.success('访前报告已生成')
    } else if (action === 'postvisit') {
      const result = await executePostvisit(customer.customerId, '')
      reportMode.value = 'postvisit'
      reportContent.value = formatPostvisitReport(result)
      message.success('访后分析已生成')
    }
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

function formatPrevisitReport(report: any): string {
  let html = `<h4>客户概览</h4><p>${report.customerOverview?.customerName || '-'}</p>`
  if (report.kycGaps) {
    html += `<h4>KYC缺口</h4><ul>`
    html += `<li>未知项: ${report.kycGaps.unknownItems?.length || 0}</li>`
    html += `<li>部分已知: ${report.kycGaps.partialKnownItems?.length || 0}</li>`
    html += `</ul>`
  }
  if (report.productRecommendations?.length) {
    html += `<h4>产品方案</h4><ul>`
    report.productRecommendations.forEach((p: string) => { html += `<li>${p}</li>` })
    html += `</ul>`
  }
  return html
}

function formatPostvisitReport(report: any): string {
  let html = ''
  if (report.keyFindings?.length) {
    html += `<h4>关键发现</h4><ul>`
    report.keyFindings.forEach((f: string) => { html += `<li>${f}</li>` })
    html += `</ul>`
  }
  if (report.commitments?.length) {
    html += `<h4>承诺事项</h4><ul>`
    report.commitments.forEach((c: string) => { html += `<li>${c}</li>` })
    html += `</ul>`
  }
  if (report.factReconciliation?.length) {
    html += `<h4>事实对账</h4><ul>`
    report.factReconciliation.forEach((r: any) => { html += `<li>${r.item}: ${r.status}</li>` })
    html += `</ul>`
  }
  if (report.nextSteps?.length) {
    html += `<h4>下一步行动</h4><ul>`
    report.nextSteps.forEach((s: string) => { html += `<li>${s}</li>` })
    html += `</ul>`
  }
  return html
}

onMounted(async () => {
  try {
    customers.value = await fetchCustomers()
  } catch (e) {
    console.error('Failed to load customers:', e)
  }
})
</script>

<style scoped>
.engagement-workspace { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 24px; color: #003366; margin: 0 0 4px 0; }
.subtitle { color: #8c8c8c; font-size: 14px; margin: 0; }
.section-title { font-size: 16px; color: #003366; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 2px solid #b8860b; display: inline-flex; align-items: center; gap: 8px; }
.process-section { margin-bottom: 32px; }
.process-flow { display: flex; align-items: flex-start; gap: 0; overflow-x: auto; padding: 16px 0; }
.process-step { display: flex; flex-direction: column; align-items: center; min-width: 140px; position: relative; padding: 16px; border-radius: 8px; transition: all 0.3s; }
.step-active { background: #003366; color: #fff; }
.step-completed { background: #fffbe6; }
.step-pending { background: #f5f5f5; color: #999; }
.step-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; margin-bottom: 8px; }
.step-active .step-icon { background: rgba(255,255,255,0.2); }
.step-completed .step-icon { background: #b8860b; color: #fff; }
.step-pending .step-icon { background: #e8e8e8; }
.step-name { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.step-desc { font-size: 11px; opacity: 0.8; text-align: center; }
.step-arrow { position: absolute; right: -20px; top: 50%; font-size: 20px; color: #b8860b; }
.action-section { margin-bottom: 32px; }
.action-card { cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; text-align: center; }
.action-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,51,102,0.15); }
.action-icon { font-size: 32px; margin-bottom: 8px; }
.action-title { font-size: 16px; font-weight: 600; color: #003366; margin-bottom: 4px; }
.action-desc { font-size: 13px; color: #666; }
.script-section, .report-section { margin-bottom: 32px; }
.script-content, .report-content-display { font-size: 14px; line-height: 1.8; color: #333; white-space: pre-wrap; }
.report-content-display :deep(h4) { color: #003366; margin: 12px 0 4px; }
.report-content-display :deep(ul) { padding-left: 20px; margin: 4px 0; }
.customer-select-list { max-height: 300px; overflow-y: auto; margin-top: 12px; }
.customer-select-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-radius: 4px; cursor: pointer; transition: background 0.2s; }
.customer-select-item:hover { background: #f5f7fa; }
</style>
