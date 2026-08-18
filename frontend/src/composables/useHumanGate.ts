import { ref, onUnmounted } from 'vue'
import {
  fetchHumanGates,
  fetchHumanGate,
  decideHumanGate,
  type HumanGate,
  type HumanGateStatus,
  type GateType,
  type HumanGateDecisionRequest,
  type GateDecision,
  fetchCrmWritebackCommands,
  fetchCrmWritebackCommand,
  decideCrmWritebackCommand,
  type CrmWritebackCommand,
  type CrmWritebackDecisionRequest
} from '../api/v11'

/**
 * Human Gate composable — 门禁查询、决策、轮询
 */
export function useHumanGate() {
  const gates = ref<HumanGate[]>([])
  const selectedGate = ref<HumanGate | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pendingCount = ref(0)
  let pollingTimer: ReturnType<typeof setInterval> | null = null

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
      pendingCount.value = gates.value.filter(g => g.status === 'PENDING').length
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载门禁失败'
    } finally {
      loading.value = false
    }
  }

  async function loadGate(gateId: string) {
    loading.value = true
    error.value = null
    try {
      selectedGate.value = await fetchHumanGate(gateId)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载门禁详情失败'
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
      pendingCount.value = gates.value.filter(g => g.status === 'PENDING').length
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '决策失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  function startPolling(intervalMs: number = 5000) {
    if (pollingTimer) return
    pollingTimer = setInterval(async () => {
      try {
        const result = await fetchHumanGates({ status: 'PENDING' })
        pendingCount.value = result.length
      } catch {
        // 忽略轮询错误
      }
    }, intervalMs)
  }

  function stopPolling() {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  onUnmounted(() => {
    stopPolling()
  })

  return {
    gates,
    selectedGate,
    loading,
    error,
    pendingCount,
    loadGates,
    loadGate,
    decide,
    startPolling,
    stopPolling
  }
}

/**
 * CRM写回 composable — 写回命令查询、决策
 */
export function useCrmWriteback() {
  const commands = ref<CrmWritebackCommand[]>([])
  const selectedCommand = ref<CrmWritebackCommand | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadCommands(params?: {
    status?: string
    journeyId?: string
    customerId?: string
  }) {
    loading.value = true
    error.value = null
    try {
      commands.value = await fetchCrmWritebackCommands(params ?? {})
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载写回命令失败'
    } finally {
      loading.value = false
    }
  }

  async function loadCommand(commandId: string) {
    loading.value = true
    error.value = null
    try {
      selectedCommand.value = await fetchCrmWritebackCommand(commandId)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载写回命令详情失败'
    } finally {
      loading.value = false
    }
  }

  async function decideCommand(commandId: string, request: CrmWritebackDecisionRequest) {
    loading.value = true
    error.value = null
    try {
      const updated = await decideCrmWritebackCommand(commandId, request)
      const idx = commands.value.findIndex(c => c.commandId === commandId)
      if (idx >= 0) {
        commands.value[idx] = updated
      }
      if (selectedCommand.value?.commandId === commandId) {
        selectedCommand.value = updated
      }
      return updated
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '决策失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    commands,
    selectedCommand,
    loading,
    error,
    loadCommands,
    loadCommand,
    decideCommand
  }
}

/**
 * 通用轮询 composable
 */
export function usePolling(fetchFn: () => Promise<void>, intervalMs: number = 5000) {
  let timer: ReturnType<typeof setInterval> | null = null
  const isPolling = ref(false)

  function start() {
    if (timer) return
    isPolling.value = true
    timer = setInterval(fetchFn, intervalMs)
    fetchFn()
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    isPolling.value = false
  }

  onUnmounted(() => {
    stop()
  })

  return { isPolling, start, stop }
}

export type { HumanGate, HumanGateStatus, GateType, GateDecision }
