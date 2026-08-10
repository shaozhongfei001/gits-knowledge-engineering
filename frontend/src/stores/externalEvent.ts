import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchExternalEvents, fetchRecentExternalEvents, createExternalEvent,
  type ExternalEvent, type ExternalEventSeverity, type ExternalEventType
} from '../api/v11'

export const useExternalEventStore = defineStore('externalEvent', () => {
  const events = ref<ExternalEvent[]>([])
  const recentEvents = ref<ExternalEvent[]>([])
  const loading = ref(false)
  const error = ref('')

  const criticalEvents = computed(() =>
    events.value.filter(e => e.confidence === 'HIGH' || e.confidence === 'CRITICAL')
  )

  const bankUsableEvents = computed(() =>
    events.value.filter(e => e.bankUseAllowed)
  )

  async function loadEvents(params?: {
    eventType?: ExternalEventType
    customerId?: string
    industry?: string
    severity?: ExternalEventSeverity
  }) {
    loading.value = true
    error.value = ''
    try {
      events.value = await fetchExternalEvents(params ?? {})
    } catch (e: any) {
      error.value = e.message || '加载外部事件列表失败'
    } finally {
      loading.value = false
    }
  }

  async function loadRecentEvents(limit: number = 20) {
    try {
      recentEvents.value = await fetchRecentExternalEvents(limit)
    } catch (e: any) {
      console.error('加载近期事件失败:', e)
    }
  }

  async function addEvent(event: Partial<ExternalEvent>) {
    try {
      const created = await createExternalEvent(event)
      events.value.unshift(created)
      return created
    } catch (e: any) {
      error.value = e.message || '创建外部事件失败'
      throw e
    }
  }

  return {
    events, recentEvents, loading, error,
    criticalEvents, bankUsableEvents,
    loadEvents, loadRecentEvents, addEvent
  }
})
