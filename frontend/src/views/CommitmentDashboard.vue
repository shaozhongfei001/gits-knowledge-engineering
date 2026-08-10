<template>
  <div class="commitment-dashboard">
    <div class="page-header">
      <h1>承诺与任务管理</h1>
      <p class="subtitle">跟踪客户承诺履行和跟进任务执行</p>
    </div>

    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-value">{{ commitmentStore.openCommitments.length }}</span>
        <span class="stat-label">待履行承诺</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ commitmentStore.overdueCommitments.length }}</span>
        <span class="stat-label">逾期承诺</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ taskStore.pendingTasks.length }}</span>
        <span class="stat-label">待处理任务</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ taskStore.overdueTasks.length }}</span>
        <span class="stat-label">逾期任务</span>
      </div>
    </div>

    <div class="dashboard-grid">
      <section class="section">
        <div class="section-header">
          <h2>承诺列表</h2>
          <button class="btn-add" @click="showCommitmentForm = true">+ 新增承诺</button>
        </div>
        <CommitmentList
          :commitments="commitmentStore.commitments"
          :loading="commitmentStore.loading"
          @select="onCommitmentSelect"
        />
      </section>

      <section class="section">
        <div class="section-header">
          <h2>任务列表</h2>
          <button class="btn-add" @click="showTaskForm = true">+ 新增任务</button>
        </div>
        <TaskList
          :tasks="taskStore.tasks"
          :loading="taskStore.loading"
          @select="onTaskSelect"
        />
      </section>
    </div>

    <!-- 机会管线 -->
    <section class="section full-width">
      <h2>机会管线</h2>
      <OpportunityPipeline
        :opportunities="opportunityStore.opportunities"
        @select="onOpportunitySelect"
      />
    </section>

    <!-- 承诺表单弹窗 -->
    <div v-if="showCommitmentForm" class="modal-overlay" @click.self="showCommitmentForm = false">
      <div class="modal-content">
        <h3>创建承诺</h3>
        <CommitmentForm
          @submit="onCreateCommitment"
          @cancel="showCommitmentForm = false"
        />
      </div>
    </div>

    <!-- 任务表单弹窗 -->
    <div v-if="showTaskForm" class="modal-overlay" @click.self="showTaskForm = false">
      <div class="modal-content">
        <h3>创建任务</h3>
        <TaskForm
          @submit="onCreateTask"
          @cancel="showTaskForm = false"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCommitmentStore } from '../stores/commitment'
import { useTaskStore } from '../stores/task'
import { useOpportunityStore } from '../stores/opportunity'
import CommitmentList from '../components/CommitmentList.vue'
import TaskList from '../components/TaskList.vue'
import OpportunityPipeline from '../components/OpportunityPipeline.vue'
import CommitmentForm from '../components/CommitmentForm.vue'
import TaskForm from '../components/TaskForm.vue'
import type { Commitment, Task, Opportunity } from '../api/v11'

const commitmentStore = useCommitmentStore()
const taskStore = useTaskStore()
const opportunityStore = useOpportunityStore()

const showCommitmentForm = ref(false)
const showTaskForm = ref(false)

onMounted(async () => {
  await Promise.all([
    commitmentStore.loadCommitments(),
    commitmentStore.loadOverdueCommitments(),
    taskStore.loadTasks(),
    taskStore.loadOverdueTasks(),
    opportunityStore.loadOpportunities()
  ])
})

function onCommitmentSelect(c: Commitment) {
  console.log('Selected commitment:', c.commitmentId)
}

function onTaskSelect(t: Task) {
  console.log('Selected task:', t.taskId)
}

function onOpportunitySelect(o: Opportunity) {
  console.log('Selected opportunity:', o.opportunityId)
}

async function onCreateCommitment(data: any) {
  try {
    await commitmentStore.addCommitment(data)
    showCommitmentForm.value = false
  } catch (e) {
    console.error('创建承诺失败:', e)
  }
}

async function onCreateTask(data: any) {
  try {
    await taskStore.addTask(data)
    showTaskForm.value = false
  } catch (e) {
    console.error('创建任务失败:', e)
  }
}
</script>

<style scoped>
.commitment-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
}
.page-header { margin-bottom: var(--space-6); }
.page-header h1 { font-size: var(--text-2xl); color: var(--text-primary); margin: 0 0 var(--space-1); font-weight: 600; }
.subtitle { color: var(--text-tertiary); font-size: var(--text-sm); margin: 0; }
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
.stat-item { display: flex; flex-direction: column; align-items: center; min-width: 80px; }
.stat-value { font-size: var(--text-3xl); font-weight: 700; }
.stat-label { font-size: var(--text-xs); opacity: 0.85; margin-top: 2px; }
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}
.section { }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}
.section h2 { font-size: var(--text-lg); color: var(--text-primary); margin: 0 0 var(--space-3); font-weight: 600; }
.btn-add {
  padding: var(--space-2) var(--space-3);
  background: var(--brand-primary);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--text-sm);
  transition: background var(--transition-fast);
}
.btn-add:hover {
  background: var(--brand-primary-light);
}
.full-width { grid-column: 1 / -1; }
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: var(--bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
}
.modal-content {
  background: var(--bg-surface);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xl);
  min-width: 400px;
  max-width: 600px;
}
.modal-content h3 { font-size: var(--text-lg); color: var(--text-primary); margin: 0 0 var(--space-4); font-weight: 600; }
</style>
