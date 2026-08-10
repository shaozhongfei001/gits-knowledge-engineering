import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchOpportunities, fetchActiveOpportunities, createOpportunity, updateOpportunityStage,
  type Opportunity, type OpportunityStage, type OpportunityType
} from '../api/v11'

export const useOpportunityStore = defineStore('opportunity', () => {
  const opportunities = ref<Opportunity[]>([])
  const loading = ref(false)
  const error = ref('')

  const activeOpportunities = computed(() =>
    opportunities.value.filter(o =>
      !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)
    )
  )

  const wonOpportunities = computed(() =>
    opportunities.value.filter(o => o.stage === 'CLOSED_WON')
  )

  const totalEstimatedAmount = computed(() =>
    activeOpportunities.value.reduce((sum, o) => sum + (o.estimatedAmount || 0), 0)
  )

  async function loadOpportunities(params?: {
    customerId?: string
    status?: OpportunityStage
    opportunityType?: OpportunityType
    assignedTo?: string
  }) {
    loading.value = true
    error.value = ''
    try {
      opportunities.value = await fetchOpportunities(params ?? {})
    } catch (e: any) {
      error.value = e.message || '加载机会列表失败'
    } finally {
      loading.value = false
    }
  }

  async function loadActiveOpportunities(customerId: string) {
    loading.value = true
    error.value = ''
    try {
      opportunities.value = await fetchActiveOpportunities(customerId)
    } catch (e: any) {
      error.value = e.message || '加载活跃机会失败'
    } finally {
      loading.value = false
    }
  }

  async function addOpportunity(opportunity: Partial<Opportunity>) {
    try {
      const created = await createOpportunity(opportunity)
      opportunities.value.push(created)
      return created
    } catch (e: any) {
      error.value = e.message || '创建机会失败'
      throw e
    }
  }

  async function changeStage(opportunityId: string, stage: OpportunityStage) {
    try {
      await updateOpportunityStage(opportunityId, stage)
      const idx = opportunities.value.findIndex(o => o.opportunityId === opportunityId)
      if (idx >= 0) {
        opportunities.value[idx] = { ...opportunities.value[idx], stage }
      }
    } catch (e: any) {
      error.value = e.message || '更新机会阶段失败'
      throw e
    }
  }

  return {
    opportunities, loading, error,
    activeOpportunities, wonOpportunities, totalEstimatedAmount,
    loadOpportunities, loadActiveOpportunities, addOpportunity, changeStage
  }
})
