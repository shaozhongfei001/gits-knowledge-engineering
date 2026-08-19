<template>
  <div class="knowledge-previsit">
    <!-- 任务区：任务定义（访前准备任务） -->
    <div class="task-header">
      <div class="task-title">
        <span class="task-badge">TASK-FRONT-001</span>
        <span class="task-name">访前准备任务</span>
      </div>
      <p class="task-objective">
        拜访目标：{{ report.visitObjective || '—' }}
        <span v-if="report.reportId">· {{ report.reportId }}</span>
      </p>
      <p class="task-mapping">
        知识地图任务映射：知识域(KD) → 知识条目(KI) → 知识要素(KE)，LLM 优先读图后生成
      </p>
    </div>

    <!-- 知识区：按 KI 分组 -->
    <div class="ki-grid">
      <!-- KI-009 企业客户基本信息 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-009</span> 企业客户基本信息</h3>
        <div class="ki-body" v-if="report.customerOverview">
          <div class="kv"><span class="k">行业</span><span class="v">{{ report.customerOverview.industry }}</span></div>
          <div class="kv"><span class="k">规模</span><span class="v">{{ report.customerOverview.enterpriseScale }}</span></div>
          <div class="kv"><span class="k">客户层级</span><span class="v">{{ report.customerOverview.customerTier }}</span></div>
          <div class="kv"><span class="k">注册资本</span><span class="v">{{ fmtMoney(report.customerOverview.registeredCapitalCny) }}</span></div>
          <div class="kv"><span class="k">风险等级</span><span class="v">{{ report.customerOverview.riskLevel }}</span></div>
          <div class="kv"><span class="k">关系摘要</span><span class="v">{{ report.customerOverview.relationshipSummary }}</span></div>
        </div>
      </section>

      <!-- KI-FRONT-001 公司供应链图谱 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-001</span> 公司供应链图谱</h3>
        <div class="ki-body">
          <p class="ki-note">（由 LLM 依据知识地图 KI-FRONT-001 供应链要素生成，见综合策略）</p>
        </div>
      </section>

      <!-- KI-FRONT-002 产业链八维研判 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-002</span> 产业链八维研判</h3>
        <div class="ki-body">
          <p class="strategy">{{ report.visitStrategy || '—' }}</p>
          <template v-if="report.riskReminders && report.riskReminders.length">
            <p class="ki-sub">风险提醒（K-Type-E 综合研判）</p>
            <ul><li v-for="(r, i) in report.riskReminders" :key="i">{{ r }}</li></ul>
          </template>
        </div>
      </section>

      <!-- KI-FRONT-003 行内变动行为 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-003</span> 行内变动行为</h3>
        <div class="ki-body">
          <p class="ki-note">（行内交易/结算/对账变动，LLM 依据 KI-FRONT-003 经营对账要素生成）</p>
        </div>
      </section>

      <!-- KI-FRONT-004 事实承诺事项 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-004</span> 事实承诺事项 / 沟通话术</h3>
        <div class="ki-body" v-if="report.keyQuestions && report.keyQuestions.length">
          <p class="ki-sub">待确认问题（K-Type-P 话术）</p>
          <ul><li v-for="(q, i) in report.keyQuestions" :key="i">{{ q }}</li></ul>
        </div>
      </section>

      <!-- KI-FRONT-005 KYC信息缺口 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-005</span> KYC信息缺口</h3>
        <div class="ki-body" v-if="report.kycGapSummary">
          <template v-if="report.kycGapSummary.knownItems?.length">
            <p class="ki-sub">已确认</p>
            <ul><li v-for="(k, i) in report.kycGapSummary.knownItems" :key="i">{{ k }}</li></ul>
          </template>
          <template v-if="report.kycGapSummary.unknownItems?.length">
            <p class="ki-sub">未知缺口</p>
            <ul><li v-for="(u, i) in report.kycGapSummary.unknownItems" :key="i">{{ u }}</li></ul>
          </template>
          <template v-if="report.kycGapSummary.priorityQuestions?.length">
            <p class="ki-sub">优先问题</p>
            <ul><li v-for="(p, i) in report.kycGapSummary.priorityQuestions" :key="i">{{ p }}</li></ul>
          </template>
        </div>
      </section>

      <!-- KI-FRONT-006 产品候选组合 -->
      <section class="ki-block">
        <h3 class="ki-title"><span class="ki-id">KI-FRONT-006</span> 产品候选组合</h3>
        <div class="ki-body" v-if="report.productSchemes?.length">
          <div v-for="scheme in report.productSchemes" :key="scheme.productId" class="product">
            <p class="product-name">{{ scheme.productName }} <span class="amount">{{ scheme.suggestedAmount || '' }}</span></p>
            <p class="product-reason">{{ scheme.matchReason }}</p>
            <p class="product-term" v-if="scheme.suggestedTerm">期限：{{ scheme.suggestedTerm }}</p>
          </div>
        </div>
      </section>
    </div>

    <!-- 速战卡（R2）按知识条目标注 -->
    <div class="battle-card" v-if="battleCard">
      <h3 class="bc-title">60秒速战卡 <span class="bc-id">R2</span></h3>
      <p class="bc-meta">{{ battleCard.customerName }} · {{ battleCard.visitObjective }}
        <span class="bc-tier">{{ battleCard.customerTier }} · {{ battleCard.riskLevel }}</span></p>
      <div class="bc-section">
        <p class="bc-sub">要点（KI-009/002）</p>
        <ul><li v-for="(p, i) in battleCard.keyPoints" :key="i">{{ p }}</li></ul>
      </div>
      <div class="bc-section">
        <p class="bc-sub">产品提示（KI-FRONT-006）</p>
        <ul><li v-for="(p, i) in battleCard.productHints" :key="i">{{ p }}</li></ul>
      </div>
      <div class="bc-section">
        <p class="bc-sub">别忘了（KI-FRONT-004/005）</p>
        <ul><li v-for="(p, i) in battleCard.dontForget" :key="i">{{ p }}</li></ul>
      </div>
      <p class="bc-bottom">底线：{{ battleCard.bottomLine }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface PrevisitReport {
  reportId?: string
  visitObjective?: string
  customerOverview?: { industry: string; enterpriseScale: string; customerTier: string; registeredCapitalCny: number; riskLevel: string; relationshipSummary: string }
  kycGapSummary?: { knownItems?: string[]; partialKnownItems?: string[]; unknownItems?: string[]; priorityQuestions?: string[] }
  productSchemes?: { productId: string; productName: string; matchReason: string; suggestedAmount?: string; suggestedTerm?: string }[]
  keyQuestions?: string[]
  riskReminders?: string[]
  visitStrategy?: string
}
interface BattleCard {
  customerName?: string
  visitObjective?: string
  customerTier?: string
  riskLevel?: string
  keyPoints?: string[]
  productHints?: string[]
  dontForget?: string[]
  bottomLine?: string
}
const props = defineProps<{ report: PrevisitReport; battleCard?: BattleCard }>()

function fmtMoney(v: number): string {
  if (v == null) return '—'
  return (v / 100000000).toFixed(0) + '亿'
}
</script>

<style scoped>
.knowledge-previsit { display: flex; flex-direction: column; gap: 14px; }
.task-header { background: #eef2ff; border: 1px solid #c7d2fe; border-radius: 10px; padding: 14px 16px; }
.task-title { display: flex; align-items: center; gap: 8px; }
.task-badge { font-family: monospace; font-size: 11px; color: #4338ca; background: #fff; border: 1px solid #c7d2fe; padding: 2px 6px; border-radius: 4px; }
.task-name { font-weight: 700; color: #1a1a2e; }
.task-objective { margin: 8px 0 0; color: #374151; font-size: 13px; }
.task-mapping { margin: 4px 0 0; color: #888; font-size: 12px; }
.ki-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ki-block { border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px 14px; background: #fafbfc; }
.ki-title { margin: 0 0 8px; font-size: 14px; color: #1a1a2e; display: flex; align-items: center; gap: 8px; }
.ki-id { font-family: monospace; font-size: 11px; color: #4338ca; background: #eef2ff; padding: 1px 6px; border-radius: 4px; }
.ki-body { font-size: 13px; color: #374151; }
.kv { display: flex; gap: 10px; padding: 2px 0; }
.k { color: #888; min-width: 64px; }
.v { color: #1a1a2e; }
.ki-sub { font-size: 12px; color: #888; margin: 6px 0 2px; }
.ki-note { color: #aaa; font-size: 12px; }
.strategy { line-height: 1.6; }
ul { margin: 4px 0; padding-left: 18px; }
li { margin: 2px 0; }
.product { border-top: 1px dashed #e5e7eb; padding: 6px 0; }
.product-name { font-weight: 600; margin: 0; }
.amount { color: #166534; font-size: 12px; }
.product-reason, .product-term { margin: 2px 0; color: #555; font-size: 12px; }
.battle-card { border: 2px solid #1e3a8a; border-radius: 10px; padding: 14px 16px; background: #f8fafc; }
.bc-title { margin: 0; font-size: 15px; color: #1e3a8a; display: flex; gap: 8px; align-items: center; }
.bc-id { font-family: monospace; font-size: 11px; color: #fff; background: #1e3a8a; padding: 1px 6px; border-radius: 4px; }
.bc-meta { margin: 6px 0; color: #374151; font-size: 13px; }
.bc-tier { color: #888; font-size: 12px; }
.bc-section { margin: 8px 0; }
.bc-sub { font-size: 12px; color: #888; margin: 0 0 2px; }
.bc-bottom { margin: 8px 0 0; font-weight: 600; color: #b91c1c; }
</style>
