<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import ObjectHeader from '../components/shell/ObjectHeader.vue'
import PageState from '../components/shell/PageState.vue'
import DisabledAction from '../components/shell/DisabledAction.vue'
import GuidancePanel from '../components/shell/GuidancePanel.vue'
import { deriveResourceStatus } from '../composables/useResourceStatus'
import { useEngagementContext } from '../composables/useEngagementContext'
import { usePageReferenceStore } from '../stores/pageReference'
import { usePrevisitStore } from '../stores/previsit'

const PAGE_ID = 'P13'
const OBJECT_TYPE = '互动 Interaction'

const msg = useMessage()
const router = useRouter()
const pageRefs = usePageReferenceStore()
const previsitStore = usePrevisitStore()
const { customerId, journeyId, operatingCaseId, rmId } = useEngagementContext()

const requested = ref(false)
const loading = ref(false)

// 本页只读消费 store 中的一键访前结果，绝不自动触发 KERT。
const status = computed(() =>
  deriveResourceStatus({
    loading: loading.value || previsitStore.loading,
    error: previsitStore.error,
    hasData: previsitStore.previsitDone || requested.value,
    requested: requested.value,
  }),
)

const objectStatus = computed(() => (journeyId.value || '缺对象'))

const sources = computed(() => {
  const p = previsitStore.previsitResult
  if (!p) return []
  const rows: { kind: string; id: string; summary: string }[] = []
  if (p.outreachScript?.scriptId) {
    rows.push({
      kind: '外联脚本',
      id: p.outreachScript.scriptId,
      summary: p.outreachScript.objective || p.outreachScript.openingLine || '',
    })
  }
  if (p.meetingScript?.scriptId) {
    rows.push({
      kind: '会面脚本',
      id: p.meetingScript.scriptId,
      summary: p.meetingScript.meetingObjective || '',
    })
  }
  if (p.previsitReport?.reportId) {
    rows.push({
      kind: '访前报告',
      id: p.previsitReport.reportId,
      summary: p.previsitReport.visitObjective || '',
    })
  }
  if (p.battleCard?.cardId) {
    rows.push({
      kind: '速战卡',
      id: p.battleCard.cardId,
      summary: p.battleCard.visitObjective || '',
    })
  }
  return rows
})

function persistReference() {
  pageRefs.capture(PAGE_ID, {
    objectType: OBJECT_TYPE,
    customerId: customerId.value || undefined,
    recordId: journeyId.value || undefined,
    viewId: 'previsit_evidence',
    scrollAnchor: typeof window !== 'undefined' ? window.scrollY : 0,
  })
}

function syncContext() {
  requested.value = true
  previsitStore.setContext({
    journeyId: journeyId.value,
    operatingCaseId: operatingCaseId.value,
    customerId: customerId.value,
    rmId: rmId.value,
  })
}

async function generatePack() {
  if (!journeyId.value || !customerId.value || !operatingCaseId.value) {
    msg.warning('缺少旅程/客户上下文，请先从互动记录·访前路径启动旅程')
    return
  }
  loading.value = true
  const result = await previsitStore.runPrevisit('访前调研')
  loading.value = false
  if (result) {
    msg.success('访前包已从 DKWS 返回（外联 + 会面 + R1 报告 + 速战卡）')
  } else {
    msg.error(previsitStore.error || '一键访前失败')
  }
}

onMounted(syncContext)
onBeforeUnmount(persistReference)
</script>

<template>
  <div class="slice-page" data-testid="p13-previsit-evidence">
    <ObjectHeader
      :page-id="PAGE_ID"
      :object-type="OBJECT_TYPE"
      :object-status="objectStatus"
      title="访前知识证据装配"
    />

    <div class="p13-layout">
      <main class="p13-main">
        <div class="toolbar">
          <button
            type="button"
            class="link-btn link-btn--primary"
            data-testid="p13-generate-pack"
            :disabled="!journeyId || !customerId || !operatingCaseId || previsitStore.loading"
            @click="generatePack"
          >
            {{ previsitStore.loading ? '生成中…' : '生成访前包' }}
          </button>
          <DisabledAction
            label="生成无来源结论"
            :disabled="true"
            reason="无合同来源时禁止生成结论"
            unlockPath="先完成 preparePrevisit 并展示返回来源后只读消费"
          />
        </div>

        <PageState :status="status" :error="previsitStore.error" idle-description="尚未装配证据" @retry="generatePack">
          <p class="hint">
            只读消费既有 preparePrevisit 返回（唯一 KERT 入口）；无来源则空态，禁止无来源结论。
          </p>

          <template v-if="previsitStore.previsitDone">
            <!-- 装配轨迹（DERIVED_READ_ONLY） -->
            <h2 class="section-title">装配轨迹</h2>
            <ul v-if="previsitStore.assemblyTrace.length" class="item-list trace-list" data-testid="p13-trace-list">
              <li v-for="(step, i) in previsitStore.assemblyTrace" :key="i" class="item trace">
                <span class="kind" :class="{ 'kind-skip': step.status === 'skipped' }">
                  {{ step.status === 'skipped' ? '跳过' : step.phase }}
                </span>
                <span v-if="step.kiId" class="mono">{{ step.kiId }}</span>
                <span class="msg">{{ step.message }}</span>
              </li>
            </ul>
            <p v-else class="empty">DKWS 未返回装配轨迹</p>

            <!-- 派生来源 -->
            <h2 class="section-title">证据来源（派生视图）</h2>
            <ul v-if="sources.length" class="item-list" data-testid="p13-source-list">
              <li v-for="row in sources" :key="row.id" class="item">
                <span class="kind">来源·{{ row.kind }}</span>
                <span class="mono">{{ row.id }}</span>
                <span>{{ row.summary || '-' }}</span>
              </li>
            </ul>

            <!-- T1: P13→P14 导航按钮 -->
            <div class="nav-next">
              <button
                type="button"
                class="link-btn link-btn--primary"
                data-testid="p13-goto-p14"
                @click="router.push({ name: 'PrevisitPack' })"
              >
                去访前包预览 →
              </button>
            </div>
          </template>
          <p v-else class="empty" data-testid="p13-empty">
            尚未执行一键访前。请先点击「生成访前包」触发 KERT（外联 + 会面 + R1 报告），装配轨迹将在此展示。
          </p>
        </PageState>
      </main>

      <GuidancePanel
        next-step="逐条显示来源、日期、权限和用途；过期/冲突证据不得静默进入结论"
        business-rule="过期、冲突或权限不足证据不得静默进入结论。"
        exception="检索超时则保留人工证据夹，不得生成无来源结论。"
        contract-usage="DERIVED_READ_ONLY：前端编排既有对象与证据形成派生视图，不新增持久化契约。"
      >
        <p class="gp-note">「生成访前包」是唯一 KERT 入口（3 Skill 并行）；本页其余内容为只读派生。</p>
      </GuidancePanel>
    </div>
  </div>
</template>

<style scoped>
.p13-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}
.link-btn {
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--border-normal);
  border-radius: 6px;
  background: var(--bg-surface);
  cursor: pointer;
}
.link-btn--primary {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  color: #fff;
  font-weight: 600;
}
.link-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.hint,
.empty {
  color: var(--text-tertiary);
  font-size: 13px;
}
.nav-next {
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px solid var(--border-normal);
}
.gp-note {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.5;
}
.section-title {
  margin: 16px 0 8px;
  font-size: 14px;
  color: var(--text-primary);
}
.item-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0;
}
.item {
  display: grid;
  grid-template-columns: 8rem 1fr 1.4fr;
  gap: 8px;
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 13px;
}
.item.trace {
  grid-template-columns: 6rem 1fr 2fr;
}
.kind {
  color: var(--text-tertiary);
}
.kind-skip {
  color: var(--gits-amber-800, #7a4b00);
}
.mono {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-secondary);
}
.msg {
  color: var(--text-primary);
}
@media (max-width: 900px) {
  .p13-layout {
    grid-template-columns: 1fr;
  }
}
</style>
