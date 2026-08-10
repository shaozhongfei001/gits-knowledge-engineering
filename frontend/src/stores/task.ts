import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchTasks, fetchOverdueTasks, createTask, updateTaskStatus,
  type Task, type TaskStatus
} from '../api/v11'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const overdueTasks = ref<Task[]>([])
  const loading = ref(false)
  const error = ref('')

  const pendingTasks = computed(() =>
    tasks.value.filter(t => t.status === 'PENDING')
  )

  const inProgressTasks = computed(() =>
    tasks.value.filter(t => t.status === 'IN_PROGRESS')
  )

  const completedTasks = computed(() =>
    tasks.value.filter(t => t.status === 'COMPLETED')
  )

  async function loadTasks(params?: {
    interactionId?: string
    customerId?: string
    operatingCaseId?: string
    assignedTo?: string
  }) {
    loading.value = true
    error.value = ''
    try {
      tasks.value = await fetchTasks(params ?? {})
    } catch (e: any) {
      error.value = e.message || '加载任务列表失败'
    } finally {
      loading.value = false
    }
  }

  async function loadOverdueTasks() {
    try {
      overdueTasks.value = await fetchOverdueTasks()
    } catch (e: any) {
      console.error('加载逾期任务失败:', e)
    }
  }

  async function addTask(task: Partial<Task>) {
    try {
      const created = await createTask(task)
      tasks.value.push(created)
      return created
    } catch (e: any) {
      error.value = e.message || '创建任务失败'
      throw e
    }
  }

  async function changeStatus(taskId: string, status: TaskStatus) {
    try {
      await updateTaskStatus(taskId, status)
      const idx = tasks.value.findIndex(t => t.taskId === taskId)
      if (idx >= 0) {
        tasks.value[idx] = { ...tasks.value[idx], status }
      }
    } catch (e: any) {
      error.value = e.message || '更新任务状态失败'
      throw e
    }
  }

  return {
    tasks, overdueTasks, loading, error,
    pendingTasks, inProgressTasks, completedTasks,
    loadTasks, loadOverdueTasks, addTask, changeStatus
  }
})
