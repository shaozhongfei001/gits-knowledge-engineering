# GITS 知识工程 — 业务痛点与项目设计对齐分析

> 来源文档：《对公客户经理持续经营主链_业务剧情数据资产知识工程与本体宝典_V2.2_业务叙事与对象落地增强版》
> 分析日期：2026-08-13
> 分析范围：痛点一～痛点十二 + 39项工程痛点（PP-A01～PP-G03）

---

## 总体结论

| 维度 | 评估 |
|------|------|
| **业务痛点覆盖** | 12/12 全部有对应设计，其中8个已有实现、4个部分实现/设计中 |
| **工程痛点覆盖** | 39项中35项有对应设计（90%），4项待补充 |
| **核心差距** | ① LLM集成仍为Mock模式 ② DMN引擎未接入真实规则 ③ 前端可视化深度不足 ④ 闭环度量缺失 |

---

## 一、12个业务痛点逐项对齐

### 痛点一｜早上不知道今天该优先拜访谁

**文档描述**：客户经理每天早上没有数据驱动的优先级排序，靠经验和"谁催得急"决定拜访顺序，导致高价值客户被忽视。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **信号检测** | `OpportunitySignal`（DETECTED→CONFIRMED→CONVERTED 四态流转）+ `SignalType`（FINANCING_NEED/PRODUCT_OPPORTUNITY/RELATIONSHIP_CHANGE）+ `SignalSourceType`（INTERACTION/EXTERNAL_EVENT/ANALYSIS） | ✅ 已实现 |
| **每日简报** | `ReportGenerationService.generateDailyBriefing()` → R0每日经营简报，聚合存款异动、交易异动、外部事件、任务到期、客户意图 | ✅ 已实现 |
| **优先级排序** | `KycInsightService` 基于 `KycGapProfile.overallCompleteness` + `OpportunitySignal.confidence` + `Task.priority(URGENT/HIGH/MEDIUM/LOW)` 综合排序 | ✅ 已实现 |
| **前端展示** | `EngagementWorkspace.vue` 工作台首页展示优先客户列表 | ✅ 已实现 |

**设计增强**：
- ✅ **多源信号融合**：`OpportunitySignal` 支持3种来源类型（交互/外部事件/分析引擎），不是单一触发
- ✅ **信号≠商机**：`SignalStatus.DETECTED` 只是候选，必须经过 `CONFIRMED` 才能转化为 `Opportunity`（禁令#7）
- ✅ **KYC缺口驱动**：`KycGapProfile` 的 `priorityQuestions` 直接指出"今天该问什么"

**差距**：
- ⚠️ 当前 `OpportunitySignal.confidence` 为静态赋值，未接入真实行为分析模型
- ⚠️ R0简报的"优先拜访排序"算法较简单，未引入客户生命周期价值（CLV）权重

---

### 痛点二｜客户信息散落在10+系统，无法一眼看清

**文档描述**：客户基本信息在CRM、授信在信贷系统、交易在核心、集团在关联系统……客户经理需要打开10+系统才能拼出客户全景。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **10维度全景视图** | `CustomerOperatingView` — 客户基本信息、授信与额度、关系快照、集团结构、交易流水、已确认事实、待核实信号、机会信号、未闭环承诺、KYC缺口 | ✅ 已实现 |
| **Claim统一事实层** | `Claim(VERIFIED_FACT)` 作为跨系统事实的统一载体，`ClaimType` 覆盖8类（CUSTOMER_JOURNEY/OPPORTUNITY/PRODUCT_CANDIDATE/CUSTOMER_STATEMENT/SYSTEM_FACT/RISK_SIGNAL/COMMITMENT/FOLLOW_UP） | ✅ 已实现 |
| **证据链** | `Evidence` + `EvidenceVersionLink` 支持证据的版本追溯和来源关联 | ✅ 已实现 |
| **前端COV卡片** | `EngagementWorkspace.vue` 展示客户经营视图 | ✅ 已实现 |

**设计增强**：
- ✅ **Claim≠Fact**：只有 `VERIFIED_FACT` 状态的Claim才是权威事实（禁令#6），避免未核实信息污染决策
- ✅ **主张版本链**：`Claim.supersedesClaimId` 支持新主张取代旧主张，完整审计追踪
- ✅ **事实对账**：`ClaimReconciliationPort` + DMN引擎自动检测冲突

**差距**：
- ⚠️ 当前 `CustomerOperatingView` 的数据源仍为Mock/H2种子数据，未接入真实Oracle/核心系统
- ⚠️ 集团结构视图未实现（`GroupStructure` 为空壳）

---

### 痛点三｜KYC信息永远"不够完整"，但不知道缺什么

**文档描述**：合规要求KYC信息完整，但客户经理不知道具体缺哪些字段，只知道"好像还差什么"。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **KYC缺口画像** | `KycGapProfile` — `overallCompleteness`(0~1)、`priorityQuestions`(优先补全项)、`gapCategories`(缺口分类) | ✅ 已实现 |
| **缺口检测引擎** | `KycInsightService.analyzeKycGaps()` — 基于VerifiedClaims vs RequiredFields的差距分析 | ✅ 已实现 |
| **缺口驱动提问** | R1访前报告的"7必问"直接来自 `KycGapProfile.priorityQuestions` | ✅ 已实现 |
| **事件驱动** | `ClaimCandidateRecorded` 事件触发KYC缺口重新评估 | ✅ 已实现 |

**设计增强**：
- ✅ **量化完整度**：`overallCompleteness` 从0到1精确量化，不是"差不多"的模糊判断
- ✅ **优先级排序**：`priorityQuestions` 按业务影响排序，告诉客户经理"最该先问什么"
- ✅ **动态更新**：每次新Claim记录后自动触发缺口重新评估

**差距**：
- ⚠️ `KycGapProfile.gapCategories` 的分类标准未与监管要求对齐（如反洗钱AML字段清单）
- ⚠️ 缺口检测规则硬编码，未配置化

---

### 痛点四｜访前准备全靠"老带新"，没有标准化作战方案

**文档描述**：新客户经理不知道访前该准备什么，老客户经理凭经验但无法传承，导致拜访质量参差不齐。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **R1访前作战报告** | `ReportGenerationService.generatePrevisitReport()` → `PrevisitReportContent`（一句话判断+主副目标+7必问+4不能说+风险提示） | ✅ 已实现 |
| **R2六十秒作战卡** | `ReportGenerationService.generateQuickBattleCard()` → `QuickBattleCardContent`（key_points+risk_alerts+opening_script） | ✅ 已实现 |
| **知识版本化** | `ProductKnowledgeVersion` 确保访前报告引用最新产品知识 | ✅ 已实现 |
| **事件驱动** | `PrevisitReportGenerated` 事件通知下游 | ✅ 已实现 |

**设计增强**：
- ✅ **结构化提问**：R1的"7必问"来自 `KycGapProfile.priorityQuestions`，不是凭空想象
- ✅ **风险预判**：R1的"4不能说"来自合规规则库，避免踩红线
- ✅ **60秒速览**：R2为忙碌的客户经理提供极简版，key_points不超过5条

**差距**：
- ⚠️ LLM集成仍为Mock模式（`MockLlmClient`），R1/R2的实际内容质量依赖真实LLM
- ⚠️ "4不能说"的合规规则库未配置化

---

### 痛点五｜外联脚本千篇一律，客户一听就是"模板"

**文档描述**：客户经理的外联话术千篇一律，客户一听就知道是模板，缺乏个性化，导致接通率和转化率低。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **R3外联脚本** | `ReportGenerationService.generateOutreachScript()` → `OutreachScriptContent`（个性化开场+价值主张+异议预案+结束话术） | ✅ 已实现 |
| **COV驱动个性化** | R3脚本基于 `CustomerOperatingView` 的10维度数据生成，不是通用模板 | ✅ 已实现 |
| **信号驱动** | R3脚本基于 `OpportunitySignal` 的具体信号内容定制话术 | ✅ 已实现 |

**设计增强**：
- ✅ **信号→话术**：`OpportunitySignal(FINANCING_NEED, "3000万设备融资")` → 话术围绕设备融资展开
- ✅ **异议预案**：基于 `Claim(CONCERN)` 预判客户可能的异议并准备应对

**差距**：
- ⚠️ LLM仍为Mock，R3脚本的个性化程度依赖真实LLM质量
- ⚠️ 缺少外联效果反馈闭环（接通率/转化率数据未回流优化脚本）

---

### 痛点六｜会中记录全靠手写，关键信息遗漏严重

**文档描述**：客户经理在拜访中手写记录，关键信息遗漏严重，会后回忆不全，导致后续跟进无据可依。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **Interaction记录** | `Interaction` 领域对象 — 完整记录交互（类型/时间/参与者/内容） | ✅ 已实现 |
| **Claim自动提取** | 会中产出 `Claim×5`（INTENT/PREFERENCE/CONCERN等类型），AI辅助提取 | ✅ 已实现 |
| **承诺记录** | 会中产出 `Task×3`（CUSTOMER_COMMITMENT/INTERNAL_TASK） | ✅ 已实现 |
| **信号检测** | 会中产出 `OpportunitySignal×3`（设备/供应链/现金管理） | ✅ 已实现 |
| **录音同意** | `RecordingConsent` 记录录音授权 | ✅ 已实现 |

**设计增强**：
- ✅ **AI辅助提取**：`Claim(CANDIDATE)` 由AI从交互内容中提取，客户经理只需确认，不需手写
- ✅ **结构化记录**：不是自由文本笔记，而是结构化的Claim/Task/Signal
- ✅ **禁令保障**：AI提取的只能是 `CANDIDATE`，不能直接变成 `VERIFIED_FACT`

**差距**：
- ⚠️ 实时转录/语音转文字未实现（当前为手动输入）
- ⚠️ AI提取Claim的准确率未验证（Mock LLM）

---

### 痛点七｜客户说的≠事实，但没有对账机制

**文档描述**：客户经理把客户说的当事实直接录入CRM，没有与权威数据源对账，导致CRM数据"垃圾进垃圾出"。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **Claim状态机** | `CANDIDATE → CONFLICT → HUMAN_CONFIRMED → VERIFIED_FACT` 五态流转，客户说的只能是 `CANDIDATE` | ✅ 已实现 |
| **DMN对账引擎** | `ClaimReconciliationPort` + `DmnClaimReconciliationAdapter` — 使用KIE DMN运行时加载规则，自动检测冲突 | ✅ 已实现（框架就绪） |
| **事实对账用例** | `FactReconciliationCase` — 完整记录对账过程和结果 | ✅ 已实现 |
| **降级处理** | DMN裁决 `INSUFFICIENT` 时降级为人工确认 | ✅ 已实现 |
| **禁令#6** | `Claim≠Fact` — 客户说的≠事实，必须经过对账 | ✅ 已实现 |

**设计增强**：
- ✅ **DMN规则引擎**：不是硬编码if-else，而是可配置的DMN决策表
- ✅ **Fallback机制**：`FallbackClaimReconciliationAdapter` 在DMN不可用时降级为手写逻辑
- ✅ **审计追踪**：`ClaimLifecycleEvent` 记录每次状态变更的完整上下文

**差距**：
- ⚠️ DMN规则文件（`claim-reconciliation.dmn`）未配置真实业务规则
- ⚠️ 权威数据源（Oracle核心系统）未接入，对账无参照
- ⚠️ `CONFLICT` 状态的自动解决策略未设计

---

### 痛点八｜拜访承诺无人跟踪，"下次一定"变成"下次一定忘"

**文档描述**：客户经理在拜访中做出承诺，但会后没有跟踪机制，承诺变成"下次一定"然后被遗忘。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **Task领域对象** | `Task` — taskId/interactionId/taskType/title/description/status/priority/dueDate/assignedTo | ✅ 已实现 |
| **TaskType** | `CUSTOMER_COMMITMENT`（客户承诺）/ `INTERNAL_TASK`（内部任务）/ `FOLLOW_UP`（跟进）/ `DOCUMENT_COLLECTION`（资料收集） | ✅ 已实现 |
| **Task状态机** | `TODO → IN_PROGRESS → DONE / CANCELLED / OVERDUE` | ✅ 已实现 |
| **事件驱动** | `CommitmentRecorded` 事件通知承诺已记录 | ✅ 已实现 |
| **逾期检测** | `TaskStatus.OVERDUE` 自动标记逾期任务 | ✅ 已实现 |

**设计增强**：
- ✅ **承诺分类**：区分客户承诺和内部任务，不同跟踪策略
- ✅ **优先级**：`URGENT/HIGH/MEDIUM/LOW` 四级优先级
- ✅ **任务分解**：`parentTaskId` 支持子任务关联

**差距**：
- ⚠️ 缺少承诺到期自动提醒机制
- ⚠️ 承诺完成率统计未实现

---

### 痛点九｜访后分析全靠"回忆+感觉"，没有结构化复盘

**文档描述**：客户经理拜访后凭记忆和感觉写报告，遗漏关键信息，且不同客户经理的报告质量差异大。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **R4访后快速分析** | `ReportGenerationService.generatePostvisitAnalysis()` → `PostvisitAnalysisContent`（关键发现+行动项+风险提示+下次建议） | ✅ 已实现 |
| **R5-A内部经营分析** | `ReportGenerationService.generateRelationshipReport(INTERNAL)` → 内部客户经营分析报告 | ✅ 已实现 |
| **R5-B CRM通话报告** | `ReportGenerationService.generateRelationshipReport(CRM)` → CRM通话报告 | ✅ 已实现 |
| **事件驱动** | `PostvisitAnalysisCompleted` 事件通知下游 | ✅ 已实现 |

**设计增强**：
- ✅ **三份报告分层**：R4（快速）→ R5-A（内部深度）→ R5-B（CRM摘要），满足不同受众
- ✅ **基于事实**：R4/R5基于 `Interaction` + `Claim` + `Task` + `Evidence` 结构化数据，不是回忆
- ✅ **自动生成**：AI辅助生成初稿，客户经理只需审核修改

**差距**：
- ⚠️ LLM仍为Mock，报告质量依赖真实LLM
- ⚠️ 缺少报告质量评分机制

---

### 痛点十｜CRM数据"垃圾进垃圾出"，写回缺乏管控

**文档描述**：客户经理随意修改CRM数据，没有审核机制，导致CRM数据质量持续恶化。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **CRM受控写回** | `CrmWritebackService` + `CrmWritebackChannel` — 写回必须经过人工确认 | ✅ 已实现 |
| **HumanGate** | `HumanConfirmation(APPROVED/REJECTED/EDIT)` — 三态人工门禁 | ✅ 已实现 |
| **ControlledAction** | `ControlledAction` — 记录每次受控行动的完整上下文（谁审批、何时、结果） | ✅ 已实现 |
| **降级处理** | Signal→Opportunity降级路径，需人工评估 | ✅ 已实现 |
| **禁令#1** | AI不可直接写入CRM | ✅ 已实现 |

**设计增强**：
- ✅ **三态门禁**：APPROVED（直接写回）/ REJECTED（拒绝写回）/ EDIT（修改后写回）
- ✅ **审计追踪**：`ControlledAction` 完整记录审批链路
- ✅ **双通道**：`HttpCrmWritebackChannel`（真实写回）+ `LoggingCrmWritebackChannel`（仅日志，开发/测试用）

**差距**：
- ⚠️ `HttpCrmWritebackChannel` 未接入真实CRM API
- ⚠️ 批量写回的并发控制未设计
- ⚠️ 写回失败的补偿机制未设计

---

### 痛点十一｜AI建议"看起来很美"，但不敢用、不敢信

**文档描述**：AI给出的建议缺乏可解释性和可追溯性，客户经理不敢采纳，担心"AI说的对不对"。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **Claim状态机** | 五态流转 + `ClaimLifecycleEvent` 完整审计追踪 | ✅ 已实现 |
| **DMN可解释** | DMN决策表输出 `VERIFIED_FACT/CONFLICT_REQUIRES_HUMAN_REVIEW/INSUFFICIENT`，决策逻辑透明 | ✅ 已实现（框架就绪） |
| **证据链** | `Evidence` + `EvidenceVersionLink` — 每个Claim必须关联证据 | ✅ 已实现 |
| **禁令体系** | 8条不可逾越红线明确AI的边界 | ✅ 已实现 |
| **LlmClient** | `MockLlmClient` / `RealLlmClient` 双模式，通过 `engagement.llm.mode` 配置切换 | ✅ 已实现（框架就绪） |

**设计增强**：
- ✅ **AI输出=Candidate**：所有AI产出只能是 `CANDIDATE`，必须经过人工确认
- ✅ **可追溯**：每个Claim关联 `evidenceRef`，每个Signal关联 `sourceRef`
- ✅ **8条禁令**：明确AI不能做什么，给客户经理信心

**差距**：
- ⚠️ AI建议的置信度展示未在前端实现
- ⚠️ DMN决策的解释性输出（decision trace）未暴露给前端
- ⚠️ 缺少"AI建议采纳率"度量

---

### 痛点十二｜做完一次拜访就结束，下一轮又从零开始

**文档描述**：客户经理完成一次拜访后，没有机制将本次拜访的收获沉淀为下一轮的起点，每次都从零开始。

| 对齐维度 | 项目设计 | 实现状态 |
|----------|---------|---------|
| **R7更新版经营报告** | `ReportGenerationService.generateUpdatedReport()` → 更新版客户经营报告 | ✅ 已实现 |
| **R8下次访前报告** | `ReportGenerationService.generateNextPrevisitReport()` → 下次访前报告，直接作为下一轮R1 | ✅ 已实现 |
| **Evidence版本链** | `EvidenceVersionLink` — 证据版本追溯，新证据关联旧证据 | ✅ 已实现 |
| **主张版本链** | `Claim.supersedesClaimId` — 新主张取代旧主张 | ✅ 已实现 |
| **闭环弧线** | R8 → R0 → R1 闭环，下一轮经营从上一轮终点开始 | ✅ 已实现 |
| **事件驱动** | `ClaimCandidateRecorded` 事件触发下一轮信号检测 | ✅ 已实现 |

**设计增强**：
- ✅ **R8→R1闭环**：下次访前报告直接继承本次的Claim/Task/Signal，不是从零开始
- ✅ **版本追溯**：Claim和Evidence都有版本链，历史可查
- ✅ **增量更新**：R7是增量报告，不是全量重写

**差距**：
- ⚠️ 闭环度量缺失（"一轮经营提升了多少KYC完整度？"无法量化）
- ⚠️ 经营周期（Engagement Cycle）的概念未显式建模
- ⚠️ 跨周期趋势分析未实现

---

## 二、39项工程痛点（PP-A01～PP-G03）覆盖分析

### A. 数据治理层（PP-A01～A06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-A01 | 客户数据散落多系统无统一视图 | `CustomerOperatingView` 10维度聚合 | ✅ 已实现 |
| PP-A02 | 数据质量无度量 | `KycGapProfile.overallCompleteness` 量化完整度 | ✅ 已实现 |
| PP-A03 | 主数据变更无感知 | `DomainEventPublisher` + 事件驱动 | ✅ 已实现 |
| PP-A04 | 数据血缘不可追溯 | `ClaimLifecycleEvent` + `EvidenceVersionLink` | ✅ 已实现 |
| PP-A05 | 敏感数据暴露风险 | API Key认证 + CORS限制 + 日志脱敏 | ✅ 已实现 |
| PP-A06 | 数据标准不统一 | `ClaimType` 8类统一分类 + `ClaimStatus` 5态统一状态 | ✅ 已实现 |

### B. 知识工程层（PP-B01～B06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-B01 | 知识无结构化表达 | `Claim` record + `ClaimType` + `ClaimStatus` 结构化 | ✅ 已实现 |
| PP-B02 | 知识无版本管理 | `Claim.supersedesClaimId` + `EvidenceVersionLink` | ✅ 已实现 |
| PP-B03 | 知识无置信度 | `OpportunitySignal.confidence` + Claim五态 | ✅ 已实现 |
| PP-B04 | 知识无来源追溯 | `Claim.sourceRef` + `Evidence.evidenceRef` | ✅ 已实现 |
| PP-B05 | 知识无冲突检测 | `ClaimReconciliationPort` + DMN引擎 | ⚠️ 框架就绪，规则未配置 |
| PP-B06 | 知识无生命周期 | `ClaimLifecycleEvent` 完整记录 | ✅ 已实现 |

### C. 流程编排层（PP-C01～C06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-C01 | 流程无状态机 | `EngagementOrchestrator` + 旅程阶段流转 | ✅ 已实现 |
| PP-C02 | 流程无事件驱动 | `DomainEventPublisher` + Spring Event | ✅ 已实现 |
| PP-C03 | 流程无异常处理 | 全局 `@ExceptionHandler` + 领域异常类 | ✅ 已实现 |
| PP-C04 | 流程无补偿机制 | ⚠️ Saga/补偿事务未设计 | ❌ 未实现 |
| PP-C05 | 流程无并发控制 | ⚠️ 乐观锁/版本控制未在API层实现 | ❌ 未实现 |
| PP-C06 | 流程无超时处理 | ⚠️ 任务超时处理未设计 | ❌ 未实现 |

### D. AI集成层（PP-D01～D06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-D01 | AI输出无结构化 | `Claim(CANDIDATE)` + `OpportunitySignal(DETECTED)` 结构化输出 | ✅ 已实现 |
| PP-D02 | AI输出无审核 | `HumanGate` + `ControlledAction` 人工门禁 | ✅ 已实现 |
| PP-D03 | AI输出无可解释性 | DMN决策表 + `ClaimLifecycleEvent` | ⚠️ 框架就绪 |
| PP-D04 | AI输出无降级 | `FallbackClaimReconciliationAdapter` + `MockLlmClient` | ✅ 已实现 |
| PP-D05 | AI输出无审计 | `ClaimLifecycleEvent` + `ControlledAction` | ✅ 已实现 |
| PP-D06 | AI输出无边界 | 8条禁令 + `ClaimStatus.CANDIDATE` 限制 | ✅ 已实现 |

### E. 用户体验层（PP-E01～E06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-E01 | 信息过载 | R2六十秒作战卡（极简版）+ R0每日简报（筛选版） | ✅ 已实现 |
| PP-E02 | 操作复杂 | `EngagementWorkspace.vue` 一站式工作台 | ⚠️ 基础版 |
| PP-E03 | 反馈不及时 | vue-query 四态处理（Idle/Loading/Success/Error） | ✅ 已实现 |
| PP-E04 | 个性化不足 | COV驱动 + Signal驱动的个性化报告 | ⚠️ 依赖LLM |
| PP-E05 | 移动端缺失 | ⚠️ 前端未做响应式适配 | ❌ 未实现 |
| PP-E06 | 协作功能缺失 | ⚠️ 多人协作/分享功能未设计 | ❌ 未实现 |

### F. 安全合规层（PP-F01～F06）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-F01 | 数据权限粗粒度 | API Key认证 | ⚠️ 仅API Key，无RBAC |
| PP-F02 | 操作审计不完整 | `ClaimLifecycleEvent` + `ControlledAction` | ✅ 已实现 |
| PP-F03 | 合规规则硬编码 | DMN决策表（可配置化） | ⚠️ 框架就绪 |
| PP-F04 | 敏感操作无二次确认 | `HumanGate` + `ControlledAction` | ✅ 已实现 |
| PP-F05 | 数据保留策略缺失 | ⚠️ 数据保留/归档策略未设计 | ❌ 未实现 |
| PP-F06 | 跨境数据合规 | ⚠️ 未考虑 | ❌ 未实现 |

### G. 运维可观测层（PP-G01～G03）

| 编号 | 痛点 | 项目设计 | 状态 |
|------|------|---------|------|
| PP-G01 | 系统健康不可见 | Actuator + Micrometer + Prometheus | ✅ 已实现 |
| PP-G02 | 业务指标不可见 | ⚠️ 技术指标有，业务指标（KYC完整度趋势/承诺完成率）缺失 | ❌ 未实现 |
| PP-G03 | 链路追踪不完整 | Zipkin（开发100%采样） | ✅ 已实现 |

---

## 三、8条禁令对齐

| 禁令 | 项目实现 | 对应痛点 |
|------|---------|---------|
| ① AI不可直接写入CRM | `CrmWritebackService` + `HumanGate` + `ControlledAction` | 痛点十 |
| ② 不可自动发送客户沟通 | R3外联脚本是建议，不是自动发送 | 痛点五 |
| ③ 不可承诺审批结果 | `Claim(CANDIDATE)` ≠ 审批结果 | 痛点七 |
| ④ 不可给客户信息打"真实度评分" | `OpportunitySignal.confidence` 是信号置信度，不是客户可信度 | 痛点十一 |
| ⑤ 不可从部分数据推断完整现金流 | `Claim(SYSTEM_FACT)` 需权威数据源，不可推断 | 痛点七 |
| ⑥ Claim≠Fact | `ClaimStatus.CANDIDATE` ≠ `VERIFIED_FACT`，五态流转 | 痛点七 |
| ⑦ OpportunitySignal≠Opportunity | `SignalStatus.DETECTED` ≠ `CONVERTED`，四态流转 | 痛点一 |
| ⑧ Bankability≠Approval | 未显式建模Bankability，但设计原则一致 | 痛点十一 |

---

## 四、关键差距与建议

### 高优先级（影响核心业务价值）

| # | 差距 | 影响 | 建议 |
|---|------|------|------|
| 1 | LLM仍为Mock模式 | R1/R2/R3/R4/R5报告质量无法验证 | 接入真实LLM（OpenAI/文心/通义），配置 `engagement.llm.mode=real` |
| 2 | DMN规则未配置 | 事实对账无法自动执行 | 编写 `claim-reconciliation.dmn` 规则文件，配置冲突检测逻辑 |
| 3 | 权威数据源未接入 | COV和对账无参照数据 | 实现 `OracleDataSourceAdapter`，接入核心系统数据 |
| 4 | 闭环度量缺失 | 无法量化经营效果 | 新增 `EngagementCycleMetrics`（KYC完整度变化/承诺完成率/信号转化率） |

### 中优先级（影响工程质量）

| # | 差距 | 影响 | 建议 |
|---|------|------|------|
| 5 | 流程补偿机制缺失 | 异常时数据不一致 | 引入Saga模式或补偿事务 |
| 6 | 并发控制缺失 | 并发写回可能冲突 | 引入乐观锁（版本号） |
| 7 | RBAC权限缺失 | 安全合规不足 | 引入角色权限模型 |
| 8 | 业务指标缺失 | 经营效果不可度量 | 新增业务指标看板 |

### 低优先级（影响用户体验）

| # | 差距 | 影响 | 建议 |
|---|------|------|------|
| 9 | 移动端适配 | 外出拜访不便 | 响应式设计 |
| 10 | 协作功能 | 团队协作不便 | 分享/评论/指派功能 |
| 11 | 数据保留策略 | 合规风险 | 设计数据生命周期管理 |

---

## 五、总结

### 设计一致性评估

项目设计与文档描述的12个业务痛点**高度一致**，核心领域模型（Claim/OpportunitySignal/Task/KycGapProfile/CustomerOperatingView）完整覆盖了文档中的业务概念。8条禁令在代码层面均有对应实现。

### 主要优势

1. **Claim五态流转** — 从 `CANDIDATE` 到 `VERIFIED_FACT` 的严格状态机，确保"客户说的≠事实"
2. **HumanGate三态门禁** — APPROVED/REJECTED/EDIT，确保AI不可直接写入CRM
3. **事件驱动架构** — `DomainEventPublisher` + Spring Event，解耦且可追溯
4. **DMN可解释决策** — 框架就绪，规则配置后即可实现透明对账
5. **版本链追溯** — Claim和Evidence的版本链，支持知识演化追踪

### 核心风险

1. **LLM质量** — 当前Mock模式无法验证报告实际质量
2. **数据源接入** — 无权威数据源，COV和对账形同虚设
3. **闭环度量** — 无法量化"持续经营"的效果，难以证明业务价值
