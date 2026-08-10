<template>
  <div class="ew">
    <div class="ew-header"><h1>持续经营工作台</h1><p>闭环流程管理，驱动客户关系持续深化</p></div>

    <!-- 状态栏 -->
    <div class="ew-bar">
      <span>当前客户：<n-tag v-if="sc" type="info">{{ sc.customerName }}</n-tag><em v-else>未选择</em></span>
      <span>旅程：<n-tag v-if="jid" :type="jpTag">{{ jpLabel }}</n-tag><em v-else>未启动</em></span>
      <span class="ew-bar-r">
        <n-button size="small" @click="showCS=true">{{ sc?'切换客户':'选择客户' }}</n-button>
        <n-button v-if="sc&&!jid" type="warning" size="small" @click="doStart" :loading="ld">启动旅程</n-button>
      </span>
    </div>

    <!-- 流程条 -->
    <div class="ew-flow">
      <div v-for="(s,i) in steps" :key="s.k" class="ew-step" :class="{active:cur===s.k,done:isDone(s.k)}" @click="clickStep(s.k)">
        <div class="ew-step-icon">{{isDone(s.k)?'✓':s.icon}}</div>
        <div class="ew-step-name">{{s.name}}</div>
        <div class="ew-step-desc">{{s.desc}}</div>
        <div v-if="i<steps.length-1" class="ew-arrow">→</div>
      </div>
    </div>

    <!-- 操作面板 -->
    <div class="ew-actions">
      <h3>操作面板</h3>
      <n-grid :cols="2" :x-gap="12" :y-gap="12">
        <n-gi v-for="a in actions" :key="a.k">
          <n-card hoverable class="ew-act" :class="{disabled:!jid}" @click="a.fn">
            <div class="ew-act-icon">{{a.icon}}</div>
            <div class="ew-act-title">{{a.title}}</div>
            <div class="ew-act-desc">{{a.desc}}</div>
            <div class="ew-act-hint"><span v-if="!jid">需先启动旅程</span><span v-else-if="a.done">已生成 ✓</span></div>
          </n-card>
        </n-gi>
      </n-grid>
    </div>

    <!-- 外联脚本 -->
    <div v-if="outR" class="ew-result">
      <h3>外联脚本 <n-tag size="small" type="info">{{chLabel(outR.channel)}}</n-tag> <n-button text size="small" @click="outR=null">关闭</n-button></h3>
      <n-card>
        <div class="sf"><b>沟通目标：</b>{{outR.objective}}</div>
        <div class="sf"><b>开场白：</b><span class="hl">{{outR.openingLine}}</span></div>
        <div class="sf"><b>话题要点：</b>
          <div v-for="(tp,i) in outR.talkingPoints" :key="i" class="tp">
            <n-tag size="small" :type="tp.priority<=2?'warning':'default'">P{{tp.priority}}</n-tag>
            <b>{{tp.topic}}</b> {{tp.detail}}
            <em v-if="tp.suggestedQuestion">建议提问：{{tp.suggestedQuestion}}</em>
          </div>
        </div>
        <div v-if="outR.riskReminders?.length" class="sf"><b>风险提醒：</b><ul><li v-for="(r,i) in outR.riskReminders" :key="i">{{r}}</li></ul></div>
        <div class="sf"><b>结束语：</b><span class="hl">{{outR.closingLine}}</span></div>
        <div class="sf"><b>后续行动：</b>{{outR.followUpAction}}</div>
      </n-card>
    </div>

    <!-- 会面脚本 -->
    <div v-if="meetR" class="ew-result">
      <h3>会面脚本 <n-button text size="small" @click="meetR=null">关闭</n-button></h3>
      <n-card>
        <div class="sf"><b>会面目标：</b>{{meetR.meetingObjective}}</div>
        <div class="sf"><b>访前摘要：</b>{{meetR.previsitSummary}}</div>
        <div class="sf"><b>议程安排：</b>
          <div v-for="(ag,i) in meetR.agendaItems" :key="i" class="ag-item">
            <div><n-tag size="small" type="info">{{ag.durationMinutes}}分钟</n-tag> <b>{{ag.topic}}</b></div>
            <div>{{ag.keyPoints}}</div>
            <div v-if="ag.expectedOutcome" class="kq-bg">预期成果：{{ag.expectedOutcome}}</div>
          </div>
        </div>
        <div class="sf"><b>KYC探查问题：</b>
          <div v-for="(q,i) in meetR.kycQuestions" :key="i" class="kq">
            <div><n-tag size="small" type="warning">{{q.gapArea}}</n-tag><n-tag size="small">{{q.expectedAnswerType}}</n-tag></div>
            <div>{{q.question}}</div>
            <div v-if="q.purpose" class="kq-bg">目的：{{q.purpose}}</div>
          </div>
        </div>
        <div class="sf"><b>产品讨论：</b>
          <div v-for="(p,i) in meetR.productDiscussions" :key="i" class="pd">
            <b>{{p.productName}}</b> — 讨论角度：{{p.discussionAngle}}
            <div v-if="p.keySellingPoints?.length" class="kq-bg">卖点：<span v-for="(s,j) in p.keySellingPoints" :key="j" class="gi">{{s}}</span></div>
          </div>
        </div>
        <div v-if="meetR.riskPoints?.length" class="sf"><b>风险提示：</b><ul><li v-for="(r,i) in meetR.riskPoints" :key="i">{{r}}</li></ul></div>
        <div class="sf"><b>结束语：</b>{{meetR.closingSummary}}</div>
      </n-card>
    </div>

    <!-- 访前报告 -->
    <div v-if="preR" class="ew-result">
      <h3>访前报告 & 速战卡 <n-button text size="small" @click="preR=null">关闭</n-button></h3>
      <n-grid :cols="2" :x-gap="12" :y-gap="12">
        <n-gi><n-card title="访前报告" size="small">
          <div class="sf"><b>拜访目标：</b>{{preR.previsitReport.visitObjective}}</div>
          <div class="sf"><b>客户概览：</b>
            <n-descriptions label-placement="left" :column="1" size="small" bordered>
              <n-descriptions-item label="行业">{{preR.previsitReport.customerOverview?.industry}}</n-descriptions-item>
              <n-descriptions-item label="规模">{{preR.previsitReport.customerOverview?.enterpriseScale}}</n-descriptions-item>
              <n-descriptions-item label="层级">{{preR.previsitReport.customerOverview?.customerTier}}</n-descriptions-item>
              <n-descriptions-item label="注册资本">{{preR.previsitReport.customerOverview?.registeredCapitalCny}}万</n-descriptions-item>
              <n-descriptions-item label="风险等级">{{preR.previsitReport.customerOverview?.riskLevel}}</n-descriptions-item>
            </n-descriptions>
          </div>
          <div class="sf"><b>KYC缺口：</b>
            <div class="gaps">
              <div v-if="preR.previsitReport.kycGapSummary?.knownItems?.length"><n-tag size="small" type="success">已知</n-tag><span v-for="(x,i) in preR.previsitReport.kycGapSummary.knownItems" :key="'k'+i" class="gi">{{x}}</span></div>
              <div v-if="preR.previsitReport.kycGapSummary?.partialKnownItems?.length"><n-tag size="small" type="warning">部分已知</n-tag><span v-for="(x,i) in preR.previsitReport.kycGapSummary.partialKnownItems" :key="'p'+i" class="gi">{{x}}</span></div>
              <div v-if="preR.previsitReport.kycGapSummary?.unknownItems?.length"><n-tag size="small" type="error">未知</n-tag><span v-for="(x,i) in preR.previsitReport.kycGapSummary.unknownItems" :key="'u'+i" class="gi">{{x}}</span></div>
              <div v-if="preR.previsitReport.kycGapSummary?.priorityQuestions?.length"><n-tag size="small" type="info">优先问题</n-tag><ul><li v-for="(q,i) in preR.previsitReport.kycGapSummary.priorityQuestions" :key="'q'+i">{{q}}</li></ul></div>
            </div>
          </div>
          <div v-if="preR.previsitReport.productSchemes?.length" class="sf"><b>产品方案：</b>
            <n-card v-for="(ps,i) in preR.previsitReport.productSchemes" :key="i" size="small" class="ps-card">
              <div class="ps-h"><b>{{ps.productName}}</b><n-tag size="small" type="info">{{ps.suggestedAmount}}</n-tag></div>
              <div>{{ps.matchReason}}</div>
              <div v-if="ps.suggestedTerm">期限：{{ps.suggestedTerm}}</div>
              <div v-if="ps.keyConditions?.length">关键条件：<span v-for="(c,j) in ps.keyConditions" :key="j" class="gi">{{c}}</span></div>
              <div v-if="ps.requiredMaterials?.length">所需材料：<span v-for="(m,j) in ps.requiredMaterials" :key="j" class="gi">{{m}}</span></div>
              <div v-if="ps.riskPoints?.length">风险点：<span v-for="(r,j) in ps.riskPoints" :key="j" class="gi ri">{{r}}</span></div>
            </n-card>
          </div>
          <div v-if="preR.previsitReport.keyQuestions?.length" class="sf"><b>关键问题：</b><ul><li v-for="(q,i) in preR.previsitReport.keyQuestions" :key="i">{{q}}</li></ul></div>
          <div v-if="preR.previsitReport.riskReminders?.length" class="sf"><b>风险提醒：</b><ul><li v-for="(r,i) in preR.previsitReport.riskReminders" :key="i" class="ri">{{r}}</li></ul></div>
          <div v-if="preR.previsitReport.visitStrategy" class="sf"><b>拜访策略：</b><span class="hl">{{preR.previsitReport.visitStrategy}}</span></div>
        </n-card></n-gi>
        <n-gi><n-card title="速战卡" size="small" type="warning">
          <div v-if="preR.battleCard">
            <div class="bc-h"><b>{{preR.battleCard.customerName}}</b><n-tag size="small" type="info">{{preR.battleCard.customerTier}}</n-tag><n-tag size="small" :type="preR.battleCard.riskLevel==='HIGH'?'error':'default'">{{preR.battleCard.riskLevel}}</n-tag></div>
            <div class="bc-obj">{{preR.battleCard.visitObjective}}</div>
            <div v-if="preR.battleCard.keyPoints?.length"><b>要点：</b><ul><li v-for="(p,i) in preR.battleCard.keyPoints" :key="i">{{p}}</li></ul></div>
            <div v-if="preR.battleCard.productHints?.length"><b>产品提示：</b><ul><li v-for="(h,i) in preR.battleCard.productHints" :key="i">{{h}}</li></ul></div>
            <div v-if="preR.battleCard.dontForget?.length"><b>别忘了：</b><ul><li v-for="(d,i) in preR.battleCard.dontForget" :key="i">{{d}}</li></ul></div>
            <div v-if="preR.battleCard.bottomLine"><b>底线：</b><span class="hl">{{preR.battleCard.bottomLine}}</span></div>
          </div>
        </n-card></n-gi>
      </n-grid>
    </div>

    <!-- 访后分析 -->
    <div v-if="postR" class="ew-result">
      <h3>访后分析 <n-button text size="small" @click="postR=null">关闭</n-button></h3>
      <n-card>
        <div class="sf"><b>拜访摘要：</b>{{postR.visitSummary}}</div>
        <div v-if="postR.keyFindings?.length" class="sf"><b>关键发现：</b>
          <div v-for="(f,i) in postR.keyFindings" :key="i" class="fi"><n-tag size="small">{{f.extractionType}}</n-tag>{{f.content}}<n-tag size="small" :type="f.confidence>=0.8?'success':f.confidence>=0.5?'warning':'error'">{{(f.confidence*100).toFixed(0)}}%</n-tag></div>
        </div>
        <div v-if="postR.opportunitySignals?.length" class="sf"><b>机会信号：</b>
          <div v-for="(s,i) in postR.opportunitySignals" :key="i" class="fi"><n-tag size="small" type="info">{{s.signalType}}</n-tag>{{s.description}}<n-tag size="small" :type="s.confidence>=0.8?'success':'warning'">{{(s.confidence*100).toFixed(0)}}%</n-tag></div>
        </div>
        <div v-if="postR.commitments?.length" class="sf"><b>承诺事项：</b>
          <div v-for="(c,i) in postR.commitments" :key="i" class="cm">{{c.content}}<span v-if="c.deadline||c.owner"> ({{c.owner||''}}{{c.deadline?'/ '+c.deadline:''}})</span></div>
        </div>
        <div v-if="postR.reconciliationItems?.length" class="sf"><b>事实对账：</b>
          <n-table size="small" :bordered="false"><thead><tr><th>事实</th><th>先前主张</th><th>新证据</th><th>结果</th></tr></thead>
          <tbody><tr v-for="(r,i) in postR.reconciliationItems" :key="i"><td>{{r.factDescription}}</td><td>{{r.previousClaim}}</td><td>{{r.newEvidence}}</td><td><n-tag size="small" :type="r.reconciliationResult==='CONFIRMED'?'success':r.reconciliationResult==='CONFLICT'?'error':'warning'">{{r.reconciliationResult}}</n-tag></td></tr></tbody></n-table>
        </div>
        <div v-if="postR.followUpActions?.length" class="sf"><b>后续行动：</b><ul><li v-for="(a,i) in postR.followUpActions" :key="i">{{a}}</li></ul></div>
        <div v-if="postR.nextStepRecommendation" class="sf"><b>下一步建议：</b><span class="hl">{{postR.nextStepRecommendation}}</span></div>
      </n-card>
    </div>

    <!-- 客户选择 -->
    <n-modal v-model:show="showCS" preset="dialog" title="选择客户" style="width:500px">
      <n-input v-model:value="sq" placeholder="搜索客户名称..." clearable />
      <div class="cs-list">
        <div v-for="c in fc" :key="c.customerId" class="cs-item" @click="pickCustomer(c)">
          <span>{{c.customerName}}</span><RiskBadge :level="c.riskLevel" />
        </div>
        <n-empty v-if="!fc.length" description="未找到客户" size="small" />
      </div>
    </n-modal>

    <!-- 渠道选择 -->
    <n-modal v-model:show="showCH" preset="dialog" title="选择外联渠道" style="width:400px">
      <div class="ch-opts">
        <n-card v-for="ch in channels" :key="ch.v" hoverable class="ch-card" @click="doOutreach(ch.v)">
          <div class="ch-icon">{{ch.icon}}</div><div class="ch-name">{{ch.label}}</div><div class="ch-desc">{{ch.desc}}</div>
        </n-card>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NGrid, NGi, NCard, NButton, NModal, NInput, NEmpty, NTag, NDescriptions, NDescriptionsItem, NTable, useMessage } from 'naive-ui'
import RiskBadge from '../components/RiskBadge.vue'
import { fetchCustomers, generateOutreachScript, generateMeetingScript, executePrevisit, executePostvisit, startJourney } from '../api/engagement'
import type { Customer, OutreachScriptResponse, MeetingScriptResponse, PrevisitExecutionResponse, PostvisitAnalysisContent } from '../api/engagement'

const msg = useMessage()
const ld = ref(false)
const custs = ref<Customer[]>([])
const sc = ref<Customer|null>(null)
const sq = ref('')
const showCS = ref(false)
const showCH = ref(false)
const jid = ref('')
const jph = ref('')
const outR = ref<OutreachScriptResponse|null>(null)
const meetR = ref<MeetingScriptResponse|null>(null)
const preR = ref<PrevisitExecutionResponse|null>(null)
const postR = ref<PostvisitAnalysisContent|null>(null)
const done = ref<Set<string>>(new Set())

const steps = [
  {k:'START',name:'启动旅程',desc:'选择客户，启动经营旅程',icon:'1'},
  {k:'OUTREACH',name:'外联触达',desc:'生成外联脚本，主动触达客户',icon:'2'},
  {k:'PREVISIT',name:'访前准备',desc:'生成访前报告和速战卡',icon:'3'},
  {k:'MEETING',name:'会面执行',desc:'生成会面脚本，执行拜访',icon:'4'},
  {k:'POSTVISIT',name:'访后复盘',desc:'分析结果，事实对账',icon:'5'}
]

const channels = [
  {v:'PHONE',label:'电话',icon:'📞',desc:'电话外联，适合首次触达'},
  {v:'WECHAT',label:'微信',icon:'💬',desc:'微信沟通，适合日常维护'},
  {v:'EMAIL',label:'邮件',icon:'📧',desc:'正式邮件，适合方案推送'},
  {v:'FACE_TO_FACE',label:'面谈',icon:'🤝',desc:'当面拜访，适合深度沟通'}
]

const actions = computed(() => [
  {k:'outreach',title:'生成外联脚本',desc:'基于客户画像和机会信号，生成电话/微信/邮件/面谈外联沟通脚本',icon:'✉',fn:handleOutreach,done:!!outR.value},
  {k:'meeting',title:'生成会面脚本',desc:'基于访前报告和KYC缺口，生成结构化会面提纲',icon:'📋',fn:handleMeeting,done:!!meetR.value},
  {k:'previsit',title:'执行访前报告',desc:'汇总客户信息、KYC缺口、产品方案，生成访前报告和速战卡',icon:'📈',fn:handlePrevisit,done:!!preR.value},
  {k:'postvisit',title:'执行访后分析',desc:'分析交互记录，提取关键发现、机会信号和事实对账',icon:'📊',fn:handlePostvisit,done:!!postR.value}
])

const cur = computed(() => {
  if (!jid.value) return 'START'
  if (!done.value.has('OUTREACH')) return 'OUTREACH'
  if (!done.value.has('PREVISIT')) return 'PREVISIT'
  if (!done.value.has('MEETING')) return 'MEETING'
  return 'POSTVISIT'
})

const jpLabel = computed(() => ({PRE_VISIT:'访前',IN_VISIT:'拜访中',POST_VISIT:'访后',CONTINUOUS_ENGAGEMENT:'持续经营',COMPLETED:'已完成'}[jph.value]||jph.value))
const jpTag = computed(() => ({PRE_VISIT:'warning',IN_VISIT:'info',POST_VISIT:'success',CONTINUOUS_ENGAGEMENT:'info',COMPLETED:'success'}[jph.value] as string||'default') as 'warning'|'info'|'success'|'default')
const fc = computed(() => { if(!sq.value) return custs.value.slice(0,20); const q=sq.value.toLowerCase(); return custs.value.filter(c=>c.customerName.toLowerCase().includes(q)).slice(0,20) })

function isDone(k:string) { return k==='START'?!!jid.value:done.value.has(k) }
function chLabel(v:string) { return channels.find(c=>c.v===v)?.label||v }

function clickStep(k:string) {
  if(k==='START'){if(!sc.value)showCS.value=true;else if(!jid.value)doStart()}
  else if(k==='OUTREACH')handleOutreach()
  else if(k==='PREVISIT')handlePrevisit()
  else if(k==='MEETING')handleMeeting()
  else if(k==='POSTVISIT')handlePostvisit()
}

async function pickCustomer(c:Customer) {
  sc.value=c; showCS.value=false
  jid.value=''; jph.value=''; outR.value=null; meetR.value=null; preR.value=null; postR.value=null; done.value=new Set()
  msg.info(`已选择客户：${c.customerName}，请点击"启动旅程"开始经营流程`)
}

async function doStart() {
  if(!sc.value){msg.warning('请先选择客户');showCS.value=true;return}
  ld.value=true
  try{const r=await startJourney(sc.value.customerId);jid.value=r.journeyId;jph.value=r.phase;done.value.add('START');msg.success(`旅程已启动 (ID: ${r.journeyId.substring(0,8)}...)`)}
  catch(e:any){msg.error(`启动旅程失败：${e.message||'未知错误'}`)}
  finally{ld.value=false}
}

function handleOutreach(){if(!jid.value){msg.warning('请先选择客户并启动旅程');return}showCH.value=true}

async function doOutreach(ch:string) {
  showCH.value=false; if(!sc.value||!jid.value)return; ld.value=true
  try{const r=await generateOutreachScript(sc.value.customerId,sc.value.rmId,'',jid.value,ch);outR.value=r;done.value.add('OUTREACH');msg.success('外联脚本已生成')}
  catch(e:any){msg.error(`生成外联脚本失败：${e.message||'未知错误'}`)}
  finally{ld.value=false}
}

async function handleMeeting() {
  if(!jid.value){msg.warning('请先选择客户并启动旅程');return} ld.value=true
  try{const r=await generateMeetingScript(sc.value!.customerId,sc.value!.rmId,'',jid.value);meetR.value=r;done.value.add('MEETING');msg.success('会面脚本已生成')}
  catch(e:any){msg.error(`生成会面脚本失败：${e.message||'未知错误'}`)}
  finally{ld.value=false}
}

async function handlePrevisit() {
  if(!jid.value){msg.warning('请先选择客户并启动旅程');return} ld.value=true
  try{const r=await executePrevisit(jid.value,sc.value!.customerId);preR.value=r;done.value.add('PREVISIT');msg.success('访前报告已生成')}
  catch(e:any){msg.error(`执行访前报告失败：${e.message||'未知错误'}`)}
  finally{ld.value=false}
}

async function handlePostvisit() {
  if(!jid.value){msg.warning('请先选择客户并启动旅程');return} ld.value=true
  try{
    const r=await executePostvisit(jid.value,sc.value!.customerId)
    postR.value={analysisId:r.analysisId||'',journeyId:jid.value,visitSummary:'访后分析已完成',keyFindings:[],opportunitySignals:[],commitments:[],reconciliationItems:[],followUpActions:['CRM回写已触发：'+(r.crmCommandCount||0)+'条命令'],nextStepRecommendation:r.allCommandsRequireHumanConfirm?'部分操作需要人工确认，请检查CRM回写命令':'所有操作已自动执行'} as PostvisitAnalysisContent
    done.value.add('POSTVISIT');msg.success('访后分析已完成')
  }catch(e:any){msg.error(`执行访后分析失败：${e.message||'未知错误'}`)}
  finally{ld.value=false}
}

onMounted(async()=>{try{custs.value=await fetchCustomers()}catch(e){console.error('Failed to load customers:',e)}})
</script>

<style scoped>
.ew {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
}

.ew-header h1 {
  font-size: var(--text-2xl);
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
  font-weight: 600;
}

.ew-header p {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  margin: 0 0 var(--space-5);
}

/* Status Bar */
.ew-bar {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-6);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.ew-bar em {
  color: var(--text-tertiary);
  font-style: italic;
}

.ew-bar-r {
  margin-left: auto;
  display: flex;
  gap: var(--space-2);
}

/* Flow Steps */
.ew-flow {
  display: flex;
  align-items: flex-start;
  overflow-x: auto;
  padding: var(--space-4) 0;
  margin-bottom: var(--space-8);
  gap: var(--space-2);
}

.ew-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 140px;
  position: relative;
  padding: var(--space-4);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid transparent;
}

.ew-step.active {
  background: var(--brand-primary);
  color: var(--text-inverse);
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-md);
}

.ew-step.done {
  background: var(--color-success-bg);
  border-color: var(--color-success-border);
  color: var(--color-success);
}

.ew-step:not(.active):not(.done) {
  background: var(--bg-surface-soft);
  color: var(--text-tertiary);
  border-color: var(--border-light);
}

.ew-step:not(.active):not(.done):hover {
  border-color: var(--border-normal);
  color: var(--text-secondary);
}

.ew-step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-base);
  font-weight: 700;
  margin-bottom: var(--space-2);
}

.active .ew-step-icon {
  background: rgba(255, 255, 255, 0.2);
}

.done .ew-step-icon {
  background: var(--color-success);
  color: var(--text-inverse);
}

.ew-step-name {
  font-size: var(--text-sm);
  font-weight: 600;
  margin-bottom: var(--space-1);
}

.ew-step-desc {
  font-size: var(--text-xs);
  opacity: 0.8;
  text-align: center;
}

.ew-arrow {
  position: absolute;
  right: -16px;
  top: 50%;
  font-size: 16px;
  color: var(--border-normal);
}

/* Actions Panel */
.ew-actions {
  margin-bottom: var(--space-8);
}

.ew-actions h3 {
  font-size: var(--text-lg);
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 2px solid var(--brand-primary);
}

.ew-act {
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  text-align: center;
}

.ew-act:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.ew-act.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ew-act.disabled:hover {
  transform: none;
  box-shadow: none;
}

.ew-act-icon {
  font-size: 28px;
  margin-bottom: var(--space-2);
}

.ew-act-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.ew-act-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
}

.ew-act-hint {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--brand-accent);
}

/* Result Panels */
.ew-result {
  margin-bottom: var(--space-8);
  animation: fadeIn 0.3s ease-out;
}

.ew-result h3 {
  font-size: var(--text-lg);
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 2px solid var(--brand-primary);
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.sf {
  margin-bottom: var(--space-3);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
}

.sf b {
  color: var(--text-primary);
}

.sf ul {
  padding-left: var(--space-5);
  margin: var(--space-1) 0;
  color: var(--text-secondary);
}

.hl {
  background: var(--brand-primary-lighter);
  padding: 2px var(--space-2);
  border-radius: var(--radius-xs);
  font-weight: 500;
  color: var(--brand-primary-dark);
}

.tp {
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--border-light);
}

.tp em {
  color: var(--brand-accent);
  font-size: var(--text-xs);
  margin-left: var(--space-2);
}

.ag-item {
  margin-bottom: var(--space-2);
}

.kq {
  margin-bottom: var(--space-2);
  padding: var(--space-2);
  background: var(--bg-surface-soft);
  border-radius: var(--radius-sm);
}

.kq-bg {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

.pd {
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--border-light);
}

.gaps > div {
  margin-bottom: var(--space-2);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-1);
}

.gi {
  background: var(--bg-hover);
  padding: 2px var(--space-2);
  border-radius: var(--radius-xs);
  font-size: var(--text-xs);
  margin: 2px;
}

.ri {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.ps-card {
  margin-bottom: var(--space-2);
}

.ps-h {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.bc-h {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.bc-obj {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.fi {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--border-light);
}

.cm {
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--border-light);
}

/* Customer Select Modal */
.cs-list {
  max-height: 300px;
  overflow-y: auto;
  margin-top: var(--space-3);
}

.cs-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.cs-item:hover {
  background: var(--bg-hover);
}

/* Channel Select Modal */
.ch-opts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}

.ch-card {
  cursor: pointer;
  text-align: center;
  transition: border-color var(--transition-fast);
}

.ch-card:hover {
  border-color: var(--brand-primary);
}

.ch-icon {
  font-size: 28px;
  margin-bottom: var(--space-1);
}

.ch-name {
  font-weight: 600;
  color: var(--text-primary);
}

.ch-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
</style>
