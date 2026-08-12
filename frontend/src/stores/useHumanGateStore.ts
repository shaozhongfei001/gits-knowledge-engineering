import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchHumanGates,
  decideHumanGate,
  type HumanGate,
  type HumanGateStatus,
  type GateType,
  type GateDecision
} from '../api/v11'

export const useHumanGateStore = defineStore('humanGate', () => {
  // State
  const gates = ref<HumanGate[]>([])
  const selectedGate = ref<HumanGate | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const pendingGates = computed(() => gates.value.filter(g => g.status === 'PENDING'))
  const pendingCount = computed(() => pendingGates.value.length)

  const gatesByType = computed(() => {
    const map = new Map<GateType, HumanGate[]>()
    for (const gate of gates.value) {
      const list = map.get(gate.gateType) || []
      list.push(gate)
      map.set(gate.gateType, list)
    }
    return map
  })

  // Actions
  async function loadGates(params?: {
    status?: HumanGateStatus
    gateType?: GateType
    journeyId?: string
    customerId?: string
  }) {
    loading.value = true
    error.value = null
    try {
      gates.value = await fetchHumanGates(params ?? {})
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载门禁失败'
    } finally {
      loading.value = false
    }
  }

  async function decide(gateId: string, decision: GateDecision,
                         modification?: Record<string, unknown>, reason?: string) {
    loading.value = true
    error.value = null
    try {
      const updated = await decideHumanGate(gateId, {
        decision,
        modification,
        reason,
        actorId: 'current-user'
      })
      const idx = gates.value.findIndex(g => g.gateId === gateId)
      if (idx >= 0) {
        gates.value[idx] = updated
      }
      if (selectedGate.value?.gateId === gateId) {
        selectedGate.value = updated
      }
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '决策失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  function selectGate(gate: HumanGate | null) {
    selectedGate.value = gate
  }

  return {
    gates,
    selectedGate,
    loading,
    error,
    pendingGates,
    pendingCount,
    gatesByType,
    loadGates,
    decide,
    selectGate
  }
})
