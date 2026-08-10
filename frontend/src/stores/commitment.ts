import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchCommitments, fetchOverdueCommitments, createCommitment, updateCommitmentStatus,
  type Commitment, type CommitmentStatus
} from '../api/v11'

export const useCommitmentStore = defineStore('commitment', () => {
  const commitments = ref<Commitment[]>([])
  const overdueCommitments = ref<Commitment[]>([])
  const loading = ref(false)
  const error = ref('')

  const openCommitments = computed(() =>
    commitments.value.filter(c => c.status === 'OPEN')
  )

  const fulfilledCommitments = computed(() =>
    commitments.value.filter(c => c.status === 'FULFILLED')
  )

  async function loadCommitments(params?: {
    interactionId?: string
    customerId?: string
    status?: CommitmentStatus
  }) {
    loading.value = true
    error.value = ''
    try {
      commitments.value = await fetchCommitments(params ?? {})
    } catch (e: any) {
      error.value = e.message || '加载承诺列表失败'
    } finally {
      loading.value = false
    }
  }

  async function loadOverdueCommitments() {
    try {
      overdueCommitments.value = await fetchOverdueCommitments()
    } catch (e: any) {
      console.error('加载逾期承诺失败:', e)
    }
  }

  async function addCommitment(commitment: Partial<Commitment>) {
    try {
      const created = await createCommitment(commitment)
      commitments.value.push(created)
      return created
    } catch (e: any) {
      error.value = e.message || '创建承诺失败'
      throw e
    }
  }

  async function changeStatus(commitmentId: string, status: CommitmentStatus) {
    try {
      await updateCommitmentStatus(commitmentId, status)
      const idx = commitments.value.findIndex(c => c.commitmentId === commitmentId)
      if (idx >= 0) {
        commitments.value[idx] = { ...commitments.value[idx], status }
      }
    } catch (e: any) {
      error.value = e.message || '更新承诺状态失败'
      throw e
    }
  }

  return {
    commitments, overdueCommitments, loading, error,
    openCommitments, fulfilledCommitments,
    loadCommitments, loadOverdueCommitments, addCommitment, changeStatus
  }
})
