<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import {
  decideHumanGate,
  fetchHumanGates,
  GATE_TYPE_LABELS,
  type HumanGate,
} from '../api/v11'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'
import GuidancePanel from '../components/shell/GuidancePanel.vue'

const PAGE_ID = 'P17'
const OBJECT_TYPE = '互动 Interaction'

const pageRefs = usePageReferenceStore()
const { journeyId } = useEngagementContext()

const gates = ref<HumanGate[]>([])
const ending = ref(false)
const loading = ref(true)
const error = ref('')
const requested = ref(false)
const checklist = ref([
  { id: 'agenda', label: '双方已确认议题（草稿）', checked: false },
  { id: 'actions', label: '双方已确认待办（草稿）', checked: false },
  { id: 'next', label: '双方已确认下一步（草稿）', checked: false },
])

const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value,
    error: error.value,
    hasData: requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (journeyId.value ? journeyId.value : '缺对象'))

const exitGate = computed(() =>
  gates.value.find(gate => gate.gateType === 'E01_EXIT_CONFIRM' && gate.status === 'PENDING') ?? null,
)

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    recordId: journeyId.value || undefined,
    viewId: 'meeting_checkout',
    draftId: checklist.value.filter(item => item.checked).map(item => item.id).join(','),
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

async function loadGates() {
  loading.value = true
  error.value = ''
  requested.value = true
  try {
    gates.value = await fetchHumanGates({
      journeyId: journeyId.value || undefined,
      gateType: 'E01_EXIT_CONFIRM',
    })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '无法加载离场确认门禁'
    gates.value = []
  } finally {
    loading.value = false
  }
}

async function endMeeting() {
  if (!exitGate.value) {
    return
  }
  ending.value = true
  try {
    await decideHumanGate(exitGate.value.gateId, {
      decision: 'APPROVE',
      actorId: 'current-user',
      reason: '离场确认',
    })
    await loadGates()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '离场确认提交失败'
  } finally {
    ending.value = false
  }
}

onMounted(() => {
  const restored = pageRefs.restore(PAGE_ID, OBJECT_TYPE)
  const ids = new Set((restored.draftId || '').split(',').filter(Boolean))
  checklist.value = checklist.value.map(item => ({ ...item, checked: ids.has(item.id) }))
  loadGates()
})
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p17-meeting-checkout">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="离场确认"
    />
    <div class="p17-body">
      <main class="p17-main">
        <div class="toolbar">
          <button
            v-if="exitGate"
            type="button"
            class="link-btn"
            data-testid="p17-end-meeting"
            :disabled="ending"
            @click="endMeeting"
          >
            结束会谈
          </button>
          <DisabledAction
            v-else
            label="结束会谈"
            :disabled="true"
            reason="无待处理 E01_EXIT_CONFIRM HumanGate，禁止提交结束会谈"
            unlockPath="仅当既有 HumanGate 类型 E01_EXIT_CONFIRM 处于 PENDING 时才可提交"
          />
        </div>
        <PageState :status="status" :error="error" idle-description="尚未加载离场确认" @retry="loadGates">
          <p class="hint">
            双方确认清单为只读/草稿。结束会谈必须走既有 HumanGate（{{ GATE_TYPE_LABELS.E01_EXIT_CONFIRM }} / E01_EXIT_CONFIRM）。
          </p>
          <ul class="item-list" data-testid="p17-checklist">
            <li v-for="item in checklist" :key="item.id" class="item">
              <label>
                <input v-model="item.checked" type="checkbox" />
                {{ item.label }}
              </label>
            </li>
          </ul>
        </PageState>
      </main>

      <GuidancePanel
        next-step="确认离场清单 → 审批 E01_EXIT_CONFIRM 门禁 → 会谈结束"
        business-rule="结束会谈必须走既有 HumanGate（E01_EXIT_CONFIRM）；清单为草稿，不写入权威。"
        exception="门禁加载失败时保持上下文，展示原因与重试按钮。"
        contract-usage="REUSE_EXISTING：消费既有 HumanGate 契约；无支持能力时禁用。"
      >
        <p class="gp-note">离场确认是人工审批节点，不可自动跳过。</p>
      </GuidancePanel>
    </div>
  </div>
</template>

<style scoped>
.p17-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.p17-main {
  flex: 1;
  min-width: 0;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.hint {
  color: var(--text-tertiary);
  font-size: 13px;
}
.link-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-surface);
  cursor: pointer;
}
.item-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item {
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 13px;
}
</style>
