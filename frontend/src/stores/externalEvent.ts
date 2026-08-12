import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchExternalEvents, fetchRecentExternalEvents, createExternalEvent,
  type ExternalEvent, type ExternalEventConfidence, type ExternalEventType
} from '../api/v11'

export const useExternalEventStore = defineStore('externalEvent', () => {
  const events = ref<ExternalEvent[]>([])
  const recentEvents = ref<ExternalEvent[]>([])
  const loading = ref(false)
  const error = ref('')

  const highConfidenceEvents = computed(() =>
    events.value.filter(e => e.confidence === 'HIGH')
  )

  const bankUsableEvents = computed(() =>
    events.value.filter(e => e.bankUseAllowed)
  )

  async function loadEvents(params?: {
    eventType?: ExternalEventType
    customerId?: string
    industry?: string
    confidence?: ExternalEventConfidence
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
    highConfidenceEvents, bankUsableEvents,
    loadEvents, loadRecentEvents, addEvent
  }
})
