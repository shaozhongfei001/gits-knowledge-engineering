# 对公客户经理持续经营业务主链 — 华东精工长篇剧情流转设计

> 业务链：`市场慧眼 → 客户洞察 → KYC/访前 → 产品准备 → 客户触达 → 会中Interaction → Fact Reconciliation → 离场承诺 → 访后分析 → CRM受控写回 → 新Evidence → R7/R8`

---

## 0. 场景背景

### 0.1 客户画像

| 属性 | 值 |
|------|-----|
| 客户名称 | 华东精工装备集团有限公司 |
| 客户ID | CUST-001 |
| 行业 | 高端装备制造 |
| 客户层级 | 战略客户（STRATEGIC） |
| RM | 张伟 |
| 合作年限 | 8年 |
| 集团综合授信 | 1.5亿元，已用1.1亿，账面可用4000万 |
| 存款变化 | 3月约1.2亿 → 7月约7800万（下降35%） |
| 核心事件 | 智能制造二期项目备案（4800万）、客户提及"3000万支持" |

### 0.2 多日经营弧线

| 日期 | 关键节点 |
|------|----------|
| 2026-03-12 | CFO首次提到"下半年可能扩一条产线" |
| 2026-03-20 | 张伟完成初步融资思路，未形成正式客户反馈 |
| 2026-03-31 | 客户设备清单承诺到期未提供 |
| 2026-05-18 | 外部出现智能制造二期项目备案（总投资约4800万） |
| 2026-05-25 | 财务总经理电话称近期有设备款，但不是全部走本行 |
| 2026-06-20 | 上游伺服与精密传动交期拉长的行业背景出现 |
| 2026-06-28 | 王强希望银行先准备几个融资思路 |
| 2026-07-02 | 客户希望看三年期左右结构；预约进一步交流 |
| **2026-07-08** | **完整拜访日**（本设计主线） |
| 2026-07-10 | 客户发送设备清单初稿（3280万，抬头智能制造公司） |
| 2026-07-14 | 产品/风险/交易银行形成两条融资路径 |
| 2026-07-18 | CFO反馈三年期方向基本可接受，主体和金额仍需确认 |
| 2026-07-20 | R7更新；R8下一次访前继承全部新Evidence |

---

## 1. 环节一：市场慧眼 — 经营触发

> EVT-001 ~ EVT-002 | 08:25 – 08:35

### 1.1 剧情描述

张伟到岗，系统已基于昨夜至凌晨的外部事件、交易异动和承诺到期信息，生成今日经营队列。华东精工因5条复合信号进入Top 1。

### 1.2 触发信号来源

| 信号 | 来源 | 本体模型 | 物化方式 |
|------|------|----------|----------|
| 存款下降35% | `bank_relationship_snapshot` | BankRelationshipSnapshot | H2 表，按 `snapshot_month` 时序对比 |
| 设备供应商付款+32% | `transaction` | Transaction | H2 表，近45天 vs 前45天聚合 |
| 二期项目备案4800万 | 外部数据源 | ExternalEvent | H2 表，`source_type=PROJECT_FILING` |
| 设备清单承诺逾期 | `task` | Task | H2 表，`due_date < CURRENT_DATE AND status=PENDING` |
| 客户希望看三年结构 | `claim` | Claim (INTENT) | H2 表，`claim_type=INTENT` |

### 1.3 产出物：R0 每日经营简报

| 字段 | 内容 |
|------|------|
| 优先客户 | 华东精工（CUST-001） |
| 优先理由 | 存款下降 + 付款增加 + 项目备案 + 承诺逾期 + 客户意图 |
| 建议动作 | 09:00前完成COV与KYC Gap；确认14:30拜访 |
| 禁令 | 不把项目备案或"3000万支持"直接写成正式融资需求 |
| 数据限制 | 交易分析仅基于本行可见账户 |

### 1.4 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| ExternalEvent | `external_event` | `JdbcExternalEventRepository.findByEntity()` |
| BankRelationshipSnapshot | `bank_relationship_snapshot` | `JdbcBankRelationshipSnapshotRepository.findByCustomerId()` |
| Transaction | `transaction` | `JdbcTransactionRepository.findByCustomerId()` |
| Task | `task` | `JdbcTaskRepository.findByCaseId()` |
| Claim | `claim` | `JdbcClaimRepository.findByCaseId()` |

### 1.5 编排调用链

```
R0生成 = ExternalEvent扫描 + BankRelationshipSnapshot时序对比
       + Transaction聚合 + Task到期检测 + Claim意图匹配
       → OpportunitySignal(DETECTED) × N
       → 排序输出 Top N 客户队列
```

---

## 2. 环节二：客户洞察 — Customer Operating View

> EVT-003 | 08:42

### 2.1 剧情描述

张伟打开华东精工COV。视图不按"工商—账户—授信—产品"平铺，而按"最近发生了什么—哪些是事实—哪些只是信号—哪些承诺没闭环—下一步要核实什么"组织。

### 2.2 COV 数据组装

| COV 区域 | 本体模型 | 加载方式 |
|----------|----------|----------|
| 客户基本信息 | Customer | `JdbcCustomerRepository.findById("CUST-001")` |
| 授信与额度 | CreditFacility | `JdbcCreditFacilityRepository.findByCustomerId()` |
| 关系快照 | BankRelationshipSnapshot | `JdbcBankRelationshipSnapshotRepository.findByCustomerId()` |
| 集团结构 | LegalEntity + GroupRelationship | `JdbcLegalEntityRepository.findByGroupId()` + `JdbcGroupRelationshipRepository.findByGroupId()` |
| 交易流水 | Transaction | `JdbcTransactionRepository.findByCustomerId()` |
| 已确认事实 | Claim (VERIFIED_FACT) | `JdbcClaimRepository.findByCaseId()` → 过滤 |
| 待核实信号 | Claim (CANDIDATE) | 同上 → 过滤 |
| 机会信号 | OpportunitySignal | `JdbcOpportunitySignalRepository.findByOperatingCaseId()` |
| 未闭环承诺 | Task (PENDING) | `JdbcTaskRepository.findByCaseId()` → 过滤 |
| KYC缺口 | KycGapProfile | `JdbcKycGapProfileRepository.findByCustomerId()` |

### 2.3 涉及本体模型

| 模型 | 物化表 | 关键字段 |
|------|--------|----------|
| Customer | `customer` | customer_id, customer_name, industry, customer_tier, rm_id |
| CreditFacility | `credit_facility` | credit_total_cny, used_credit_cny, available_credit_cny |
| BankRelationshipSnapshot | `bank_relationship_snapshot` | avg_daily_deposit_cny, loan_balance_cny |
| LegalEntity | `legal_entity` | entity_id, group_id, name, role |
| GroupRelationship | `group_relationship` | from_entity_id, to_entity_id, ownership_ratio |
| Transaction | `transaction` | transaction_type, amount, counterparty, transaction_date |
| Claim | `claim` | claim_type, claim_status, statement_text |
| OpportunitySignal | `opportunity_signal` | signal_type, content, confidence, status |
| Task | `task` | title, priority, status, due_date |
| KycGapProfile | `kyc_gap_profile` | known_items, unknown_items, priority_questions |

### 2.4 编排调用链

```
COV = CustomerContextService.assembleCustomerOperatingView(customerId)
    → loadCustomer()           // Customer
    → loadCreditFacilities()   // CreditFacility[]
    → loadSnapshots()          // BankRelationshipSnapshot[]
    → loadGroupStructure()     // LegalEntity[] + GroupRelationship[]
    → loadRecentTransactions() // Transaction[]
    → loadClaims()             // Claim[] (按status分组)
    → loadSignals()            // OpportunitySignal[]
    → loadPendingTasks()       // Task[]
    → loadKycGap()             // KycGapProfile
    → 组装为 CustomerOperatingView record
```

---

## 3. 环节三：KYC Gap — 知识缺口分析

> EVT-006 | 09:08

### 3.1 剧情描述

系统生成KYC缺口画像，发现项目主体、资金语义、付款节奏等关键信息缺失。

### 3.2 缺口分析逻辑

| 缺口类别 | 华东精工实例 | 本体模型 |
|----------|-------------|----------|
| 已知 (known_items) | 综合授信1.5亿/已用1.1亿；合作8年；战略客户 | Claim (VERIFIED_FACT) |
| 部分已知 (partial_known_items) | 二期项目推进中，主体倾向智能制造公司但未过会 | Claim (CANDIDATE) |
| 过期 (stale_items) | 3月承诺的设备清单未提供 | Task (OVERDUE) |
| 冲突/模糊 (conflicting_or_ambiguous_items) | "3000万支持"语义不明 | Claim (CANDIDATE, ambiguous) |
| 未知 (unknown_items) | 他行方案细节；非设备投资构成；4000万经营用途 | KycGapProfile.unknown_items |

### 3.3 产出物：KycGapProfile

| 字段 | 值 |
|------|-----|
| profile_id | UUID |
| customer_id | CUST-001 |
| as_of | 2026-07-08 |
| known_items | [授信1.5亿/已用1.1亿, 合作8年, 战略客户] |
| partial_known_items | [二期项目推进中, 主体倾向智能制造公司] |
| stale_items | [3月设备清单承诺逾期] |
| conflicting_or_ambiguous_items | ["3000万支持"语义不明] |
| unknown_items | [他行方案细节, 非设备投资构成, 4000万经营用途] |
| priority_questions | [项目主体？3000万语义？设备清单？付款节奏？他行路径？] |
| overall_completeness | 0.42 |
| risk_impact | HIGH |

### 3.4 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| KycGapProfile | `kyc_gap_profile` | `JdbcKycGapProfileRepository.findByCustomerId()` |
| Claim | `claim` | `JdbcClaimRepository.findByCaseId()` → 按status分类 |
| Task | `task` | `JdbcTaskRepository.findByCaseId()` → 过滤过期 |
| ExternalEvent | `external_event` | `JdbcExternalEventRepository.findByEntity()` |

### 3.5 编排调用链

```
KycGapProfile = KycInsightService.generateKycGapProfile(customerId, caseId)
    → loadExistingProfile()     // KycGapProfile (如有)
    → loadVerifiedClaims()      // Claim[] VERIFIED_FACT → known_items
    → loadCandidateClaims()     // Claim[] CANDIDATE → partial_known / conflicting
    → loadOverdueTasks()        // Task[] OVERDUE → stale_items
    → loadExternalEvents()      // ExternalEvent[] → 交叉比对
    → 计算整体完整度 overall_completeness
    → 生成 priority_questions
    → saveKycGapProfile()
    → domainEventPublisher.publish(ClaimCandidateRecorded)
```

---

## 4. 环节四：访前准备 — R1访前报告 + R2作战卡

> EVT-007 ~ EVT-009 | 09:18 – 09:38

### 4.1 剧情描述

系统恢复3月设备清单承诺和历史客户原话（记忆恢复），生成R1访前作战报告和R2 60秒作战卡。产品知识校验发现设备更新贷款V2.1已失效，阻断旧版本引用。

### 4.2 产出物：R1 访前作战报告

**PrevisitReportContent** — 保存至 `previsit_report_content` 表

| 字段 | 值 |
|------|-----|
| id | UUID |
| analysis_id | 关联分析ID |
| journey_id | 当前旅程ID |
| operating_case_id | 当前案例ID |
| visit_objective | 核实项目主体、3000万语义、设备清单承诺 |
| content_json | 结构化报告（见下） |

**content_json 关键结构：**

- **一句话判断**：客户二期项目正从外部线索向可核实资金安排演进，但主体、额度可用性和正式结构仍未确认
- **主目标**：核实项目主体 / 核实"3000万支持"语义 / 拿到设备清单承诺
- **副目标**：供应商付款工具 / 集团现金管理 / 三方会商需求判断
- **事实与信号表**：

| 内容 | 类型 | 口径限制 |
|------|------|----------|
| 综合授信1.5亿/已用1.1亿/可用4000万 | FACT | 不等于项目可直接使用额度 |
| 二期项目备案总投资约4800万 | SIGNAL/EXTERNAL | 不等于正式融资需求 |
| 设备供应商付款+32% | FINDING | 仅本行可见交易 |
| 3月客户承诺设备清单 | COMMITMENT | 已逾期 |
| 客户希望看三年左右结构 | INTENT | 需结合项目主体和用途核实 |

- **7个必问问题**：项目主体？3000万语义？设备清单口径？付款节奏？4000万用途？他行路径？供应商接受度？
- **4条"不能说"**：不可承诺4000万可用 / 不可认定3000万新增授信 / 不可将备案等同融资需求 / 不可承诺审批

### 4.3 产出物：R2 60秒作战卡

**QuickBattleCard** — 场景视图Record，不持久化

| 字段 | 值 |
|------|-----|
| card_id | UUID |
| customer_id | CUST-001 |
| category | 设备更新融资 |
| title | 华东精工二期项目拜访 |
| summary | 核实主体+语义+清单；不把3000万写成新增授信 |
| key_points | [项目主体, 3000万语义, 设备清单承诺, 付款节奏] |
| risk_alerts | [不可承诺额度/价格, 不可假设4000万可用, 不可将信号当事实] |
| suggested_actions | [追问四种语义, 确认主体, 索要设备清单] |

### 4.4 产品知识版本校验

| 校验项 | 结果 |
|--------|------|
| 设备更新贷款 V2.2 | ✅ 当前有效版本 |
| 设备更新贷款 V2.1 | ❌ 2026-06-30失效，阻断引用 |

**ProductKnowledgeVersion** 校验链：V2.2(有效) ← previousVersion → V2.1(失效)

### 4.5 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| PrevisitReportContent | `previsit_report_content` | `JdbcPrevisitReportContentRepository.save()` |
| QuickBattleCard | Java Record（场景视图） | 内存组装，不持久化 |
| ProductKnowledgeVersion | `product_knowledge_version` | `JdbcProductKnowledgeVersionRepository.findByProductId()` |
| ProductCatalog | `product_catalog` | `JdbcProductCatalogRepository.findById()` |
| Claim | `claim` | `JdbcClaimRepository.findByCaseId()` |
| Task | `task` | `JdbcTaskRepository.findByCaseId()` |

### 4.6 编排调用链

```
EngagementOrchestrator.executePrevisitPhase(journeyId, customerId, operatingCaseId, visitObjective)
    → customerJourneyService.advancePhase(journeyId, PREVISIT_PREP)
    → previsitWorkflowService.generatePrevisitReport(...)
        → loadKycGapProfile()            // KycGapProfile
        → loadClaims()                   // Claim[]
        → loadPendingTasks()             // Task[]
        → loadProductCatalog()           // ProductCatalog
        → loadProductKnowledgeVersions() // ProductKnowledgeVersion[] → 版本校验
        → 组装 PrevisitReportContent
        → savePrevisitReportContent()
    → generateQuickBattleCard(...)       // QuickBattleCard
    → domainEventPublisher.publish(PrevisitReportGenerated)
    → return PrevisitWorkflowResult(previsitReport, battleCard)
```

---

## 5. 环节五：客户触达 — 外联与预约

> EVT-010 ~ EVT-014 | 10:05 – 10:40

### 5.1 剧情描述

张伟致电CFO未接，发微信简短说明，王强回复确认下午14:30拜访。系统记录触达状态，生成外联话术。

### 5.2 产出物：OutreachScript

| 字段 | 值 |
|------|-----|
| id | UUID |
| journey_id | 当前旅程ID |
| operating_case_id | 当前案例ID |
| script_type | WARM_INTRO |
| objective | 确认下午拜访时间和参会人员 |
| opening_line | "王总您好，关于上次沟通的设备更新事宜，想今天下午当面交流几个问题" |
| key_talking_points | [项目主体确认, 资金安排讨论, 设备清单索取] |
| closing_line | "那下午两点半见" |
| risk_considerations | [不提及具体额度, 不承诺审批结果] |
| generated_at | 2026-07-08T10:05:00 |

### 5.3 触达记录

| 触达 | 渠道 | 方向 | 结果 |
|------|------|------|------|
| 致电CFO | PHONE | OUTBOUND | 未接 |
| 微信说明 | WECHAT | OUTBOUND | 已读 |
| 王强回复 | WECHAT | INBOUND | 确认14:30 |

每次触达生成一条 **Interaction**（`interaction_type=OUTREACH`）。

### 5.4 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| OutreachScript | `outreach_script` | `JdbcOutreachScriptRepository.save()` |
| Interaction | `interaction` | `JdbcInteractionRepository.save()` |
| InteractionParticipant | `interaction_participant` | 随Interaction写入 |

### 5.5 编排调用链

```
OutreachScript = PrevisitWorkflowService.generateOutreachScript(journeyId, operatingCaseId, scriptType)
    → loadCustomer()               // Customer
    → loadPreviousInteractions()   // Interaction[]
    → 组装 OutreachScript
    → saveOutreachScript()

Interaction = recordOutreachInteraction(...)
    → 创建 Interaction(interaction_type=OUTREACH, channel=PHONE/WECHAT)
    → 创建 InteractionParticipant[]
    → saveInteraction()
```

---

## 6. 环节六：会中Interaction — 现场拜访

> EVT-018 ~ EVT-028 | 14:25 – 15:50

### 6.1 剧情描述

张伟到达客户现场。客户拒绝全程录音，选择现场笔记+访后口述。拜访中客户确认二期项目推进、提及"3000万支持"、说明设备清单和付款节奏、采购负责人加入讨论供应商、交易银行和现金管理话题。离场前确认双方承诺。

### 6.2 录音同意

**RecordingConsent** — 保存至 `recording_consent` 表

| 字段 | 值 |
|------|-----|
| consent_id | UUID |
| interaction_id | 本次拜访Interaction ID |
| consent_type | RECORDING |
| consent_given | false (DECLINED) |
| consent_method | VERBAL |
| consent_given_by | 李明 (CFO) |
| consent_given_at | 2026-07-08T14:25:00 |

> 影响链：`recording_consent=DECLINED` → 输入模式切换为"现场笔记+访后口述" → 不产生 `meeting_gold_transcript` → Interaction内容来源为 `post_visit_debrief_raw`

### 6.3 会中核心交互事件

| 时间 | 事件 | 本体模型产出 | 类型 |
|------|------|-------------|------|
| 14:30 | 开场重述上次沟通 | — | MEETING |
| 14:42 | 客户确认二期项目推进 | Claim(CANDIDATE, "项目推进中") | CLAIM |
| 14:42 | 项目主体倾向智能制造公司 | Claim(CANDIDATE, "主体倾向智能制造公司") | CLAIM |
| 14:53 | 王强说"增加3000万左右支持" | Claim(CANDIDATE, ambiguous, "3000万支持") | INTENT |
| 15:08 | 设备清单三千多万、付款30/40/30 | Claim(CANDIDATE, "设备3280万,付款30/40/30") | CLAIM |
| 15:22 | 三家供应商交期和预付款压力 | OpportunitySignal(SUPPLY_CHAIN) | SIGNAL |
| 15:33 | 供应商付款工具机会 | OpportunitySignal(TRANSACTION_BANKING) | SIGNAL |
| 15:40 | 子公司资金调拨手工协调 | OpportunitySignal(CASH_MANAGEMENT) | SIGNAL |
| 15:50 | 离场确认双方承诺 | Task × 3 | COMMITMENT |

### 6.4 Interaction 主记录

| 字段 | 值 |
|------|-----|
| interaction_id | UUID |
| case_id | 当前OperatingCase ID |
| journey_id | 当前CustomerJourney ID |
| interaction_type | IN_PERSON_VISIT |
| direction | OUTBOUND |
| channel | IN_PERSON |
| content_summary | 二期项目核实+3000万语义澄清+设备清单+供应商+现金管理 |
| outcome | PARTIAL_CONFIRMATION |
| initiator_id | 张伟 |
| initiator_role | RELATIONSHIP_MANAGER |
| occurred_at | 2026-07-08T14:30:00 |
| ended_at | 2026-07-08T15:50:00 |

### 6.5 会中产出的交互对象

| 对象 | 类型 | 内容 | 状态 |
|------|------|------|------|
| 项目推进确认 | CLAIM | 二期项目正在推进 | CANDIDATE |
| 主体倾向 | CLAIM | 更倾向智能制造公司 | CANDIDATE |
| 3000万支持 | INTENT | "希望银行增加3000万左右支持" | CANDIDATE (ambiguous) |
| 三年期偏好 | PREFERENCE | 客户希望期限与项目匹配 | CANDIDATE |
| 方案简单 | CONCERN | 客户要求方案简单 | CANDIDATE |
| 王强7/10设备清单 | COMMITMENT | 客户承诺7月10日前发设备清单 | PENDING |
| 张伟7/14结构建议 | COMMITMENT | 银行承诺7月14日前提供三年期结构 | PENDING |
| 王彬7/14供应商工具 | COMMITMENT | 交易银行承诺7月14日前补充 | PENDING |
| 设备融资信号 | OPPORTUNITY_SIGNAL | 设备更新融资机会 | DETECTED |
| 供应链信号 | OPPORTUNITY_SIGNAL | 供应商付款工具机会 | DETECTED |
| 现金管理信号 | OPPORTUNITY_SIGNAL | 集团现金管理机会 | DETECTED |

### 6.6 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| Interaction | `interaction` | `JdbcInteractionRepository.save()` |
| InteractionParticipant | `interaction_participant` | 随Interaction写入 |
| RecordingConsent | `recording_consent` | `JdbcRecordingConsentRepository.save()` |
| InteractionExtension | `interaction_extension` | 随Interaction写入 |
| Claim | `claim` | `JdbcClaimRepository.save()` |
| ClaimEvidence | `claim_evidence` | 随Claim写入 |
| Task | `task` | `JdbcTaskRepository.save()` |
| OpportunitySignal | `opportunity_signal` | `JdbcOpportunitySignalRepository.save()` |

### 6.7 编排调用链

```
会中Interaction = EngagementOrchestrator.executePostvisitPhase(...) 的输入阶段
    → 创建 Interaction(interaction_type=IN_PERSON_VISIT)
    → 创建 InteractionParticipant[] (张伟, 李明, 王强, 陈涛, 周倩)
    → 创建 RecordingConsent(consent_given=false)
    → 创建 InteractionExtension(recording_consent_id=...)
    
    → 会中实时产出:
        → Claim[] × 5 (CLAIM/INTENT/PREFERENCE/CONCERN)
        → Task[] × 3 (双方承诺)
        → OpportunitySignal[] × 3 (设备/供应链/现金管理)
```

---

## 7. 环节七：Fact Reconciliation — 事实对账

> EVT-022 ~ EVT-023 | 14:53 – 14:58

### 7.1 剧情描述

客户说"增加3000万左右支持"后，系统将该表达标为 **Ambiguous Intent**，提示张伟追问四种语义。CFO澄清为"第一阶段设备款资金安排，不等同正式新增授信申请"。这是全场最关键的业务判断点。

### 7.2 产出物：FactReconciliationCase

| 字段 | 值 |
|------|-----|
| reconciliation_id | UUID |
| case_id | 当前OperatingCase ID |
| topic | "3000万支持"语义澄清 |
| structured_fact | 客户表达"增加3000万左右支持" |
| interaction_claim | 第一阶段设备款资金安排 |
| external_fact | 项目备案4800万，设备清单3280万 |
| ontology_distinction | 新增额度 ≠ 现有额度提款 ≠ 用途调整 ≠ 项目公司融资 |
| correct_judgment | 不创建"新增授信3000万"事实；准备集团本部和项目公司两条结构 |
| wrong_output_examples | ["新增授信3000万", "4000万可用额度可直接用于该项目"] |
| next_action | 等待设备清单确认设备金额和主体 |
| status | RESOLVED |

### 7.3 DMN 决策引擎裁决

通过 `ClaimReconciliationPort` 委托 DMN 决策：

```
输入:
  conflictDetected = true   (3000万语义冲突)
  authoritativeMatch = false (无权威来源确认)
  evidenceComplete = false   (设备清单未到)

DMN裁决输出:
  reconciliationStatus = INSUFFICIENT
  action = REQUIRE_HUMAN_CLARIFICATION

→ 张伟追问 → CFO澄清 → status更新为 RESOLVED
→ Claim.claim_status 从 CANDIDATE 更新为 VERIFIED_FACT
```

### 7.4 Claim 状态变迁

```
CANDIDATE (ambiguous "3000万支持")
  → FactReconciliationCase 创建
  → DMN裁决: INSUFFICIENT → 需人工澄清
  → 张伟追问四种语义
  → CFO澄清: "第一阶段设备款资金安排"
  → claim_status → VERIFIED_FACT
  → ClaimLifecycleEvent(from=CANDIDATE, to=VERIFIED_FACT, reason="现场语义澄清")
```

### 7.5 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| FactReconciliationCase | `fact_reconciliation_case` | `JdbcFactReconciliationRepository.save()` |
| Claim | `claim` | `JdbcClaimRepository.save()` → 更新claim_status |
| ClaimLifecycleEvent | `claim_lifecycle_event` | 随Claim状态变更写入 |
| ClaimReconciliationPort | 接口（DMN/Fallback） | `DmnClaimReconciliationAdapter.reconcile()` |

### 7.6 编排调用链

```
FactReconciliation = KycInsightService.createReconciliation(
    caseId, topic, structuredFact, interactionClaim, externalFact,
    ontologyDistinction, correctJudgment, wrongOutputExamples, nextAction,
    conflictDetected=true, authoritativeMatch=false, evidenceComplete=false
)
    → claimReconciliationPort.reconcile(conflictDetected, authoritativeMatch, evidenceComplete)
        → DMN决策 → ReconciliationResult(status=INSUFFICIENT, action=REQUIRE_HUMAN_CLARIFICATION)
    → 创建 FactReconciliationCase
    → 更新 Claim.claim_status (CANDIDATE → 待澄清 → VERIFIED_FACT)
    → 创建 ClaimLifecycleEvent
    → domainEventPublisher.publish(ClaimCandidateRecorded)
```

---

## 8. 环节八：离场承诺 — 双方Task确认

> EVT-028 | 15:50

### 8.1 剧情描述

张伟离场前逐条复述双方下一步，确保每个承诺有明确责任人和截止日期。

### 8.2 承诺清单

| 承诺 | 责任方 | 截止日 | 本体模型 | 物化 |
|------|--------|--------|----------|------|
| 发送设备清单与付款节奏 | 王强（客户） | 7月10日 | Task (CUSTOMER_COMMITMENT) | `task` 表 |
| 提供三年期结构建议 | 张伟（银行） | 7月14日 | Task (INTERNAL_TASK) | `task` 表 |
| 补充供应商付款工具对照 | 王彬（交易银行） | 7月14日 | Task (INTERNAL_TASK) | `task` 表 |

### 8.3 涉及本体模型与数据加载

| 模型 | 物化表 | 关键字段 |
|------|--------|----------|
| Task | `task` | task_type, title, priority, status, assigned_to, due_date |
| InteractionExtension | `interaction_extension` | commitment_ids (关联承诺ID列表) |

### 8.4 编排调用链

```
离场承诺 = PostvisitProcessingService.recordExitCommitments(interactionId, commitments[])
    → 创建 Task[] × 3
    → 更新 InteractionExtension.commitment_ids
    → domainEventPublisher.publish(CommitmentRecorded)
```

---

## 9. 环节九：访后分析 — R4/R5/R6

> EVT-029 ~ EVT-032 | 16:10 – 17:05

### 9.1 剧情描述

张伟回程中做3分20秒访后口述。系统对照访前问题发现"他行方案细节"未获得。生成Interaction对象、Claim/Commitment/Task/Signal，生成R4访后分析、R5-A内部报告、R5-B CRM通话报告及专业协同Fact Pack。

### 9.2 访后口述输入

**PostvisitDebriefRaw** — 张伟3分20秒口述核心内容：

- 核心结论：不要把3000万写成新增授信
- 项目主体：倾向智能制造公司，但未最终过会
- 设备清单：三千多万，付款30/40/30
- 供应商：三家，预付款有压力
- 现金管理：子公司资金调拨手工协调
- 双方承诺：王强7/10清单、张伟7/14结构、王彬7/14工具
- KYC Gap：他行方案细节未获得
- 录音：未授权

### 9.3 产出物：R4 访后快速分析

**PostvisitAnalysisContent** — 保存至 `postvisit_analysis_content` 表

| 区域 | 内容 |
|------|------|
| 已确认 | 项目推进中 / 主体倾向智能制造公司 / 3000万=设备款资金安排 / 付款30/40/30 / 客户希望三年期 / 方案要简单 |
| 未确认 | 最终项目主体 / 4000万可用额度用途 / 4800万非设备投资构成 / 他行方案细节 |
| 双方下一步 | 王强7/10清单 / 张伟7/14结构 / 王彬7/14工具 |
| 机会状态 | 设备更新融资=OpportunitySignal / 供应链=Signal / 现金管理=Signal |

### 9.4 产出物：R5-A 内部客户经营分析

**RelationshipReport** (report_type=INTERNAL) — 保存至 `relationship_report` 表

| 维度 | 内容 |
|------|------|
| 经营变化 | 存款下降 / 付款增加 / 项目备案 → 三类信息构成"值得核实"触发，但均不足以单独证明正式新增融资需求 |
| 项目与主体 | 授信主体=集团本部ENT-001 / 项目主体倾向智能制造公司ENT-002 / 借/用/还/担保待确认 |
| 3000万事实对账 | 不创建"新增授信3000万"事实 / 不将4000万与3000万直接相抵 / 同时准备两条结构 |
| 产品方向 | 主候选：固定资产/设备更新融资 / 谨慎候选：现有流贷 / 条件候选：供应链票据 / 并行：现金管理 |
| 专业会商 | 产品经理核对V2.2 / 风险经理Preliminary Bankability / 交易银行供应商接受度 |
| 风险边界 | Bankability ≠ Approval / 不承诺额度/价格/政策资格 |

### 9.5 产出物：R5-B CRM通话报告

**RelationshipReport** (report_type=CRM) — 保存至 `relationship_report` 表

面向CRM的精简版，去除内部判断细节，保留客户可感知的服务内容和下一步。

### 9.6 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| PostvisitAnalysisContent | `postvisit_analysis_content` | `JdbcPostvisitAnalysisContentRepository.save()` |
| RelationshipReport | `relationship_report` | `JdbcRelationshipReportRepository.save()` |
| Claim | `claim` | `JdbcClaimRepository.save()` (访后补充) |
| Task | `task` | `JdbcTaskRepository.save()` (承诺确认) |
| OpportunitySignal | `opportunity_signal` | `JdbcOpportunitySignalRepository.save()` |
| MeetingTranscript | Java Record | 口述转录，不持久化 |

### 9.7 编排调用链

```
EngagementOrchestrator.executePostvisitPhase(journeyId, operatingCaseId, customerId, rawTranscript)
    → customerJourneyService.advancePhase(journeyId, POSTVISIT_REVIEW)
    → postvisitProcessingService.processTranscript(rawTranscript)
        → 生成 MeetingTranscript
        → 对照访前必问问题 → 发现"他行方案细节"未覆盖
    → reportGenerationService.generatePostvisitAnalysis(...)
        → 组装 PostvisitAnalysisContent (R4)
        → savePostvisitAnalysisContent()
    → reportGenerationService.generateInternalReport(...)
        → 组装 RelationshipReport(report_type=INTERNAL) (R5-A)
        → saveRelationshipReport()
    → reportGenerationService.generateCrmReport(...)
        → 组装 RelationshipReport(report_type=CRM) (R5-B)
        → saveRelationshipReport()
    → 生成 CrmWritebackCommand[] (见环节十)
    → domainEventPublisher.publish(PostvisitAnalysisCompleted)
    → return PostvisitWorkflowResult(transcript, analysis, internalReport, crmReport, crmCommands)
```

---

## 10. 环节十：CRM受控写回

> EVT-033 | 17:20

### 10.1 剧情描述

RM审核CRM写回内容。系统建议创建"设备更新融资机会"（正式Opportunity），张伟认为证据仍不足，选择 **EDIT**：只保留OpportunitySignal，不创建正式商机。这一动作成为后续Skill评测的重要反馈。

### 10.2 Human Gate 决策

**HumanConfirmation** — 保存至 `human_confirmation` 表

| 字段 | 值 |
|------|-----|
| confirmation_id | UUID |
| subject_type | CRM_WRITEBACK |
| subject_id | CrmWritebackCommand ID |
| decision | EDIT (非APPROVED，降级处理) |
| actor_id | 张伟 |
| actor_role | RELATIONSHIP_MANAGER |
| comment_text | "证据仍不足，只保留Signal，不创建正式商机" |

### 10.3 CrmWritebackCommand

| 字段 | 值 |
|------|-----|
| command_id | UUID |
| customer_id | CUST-001 |
| operating_case_id | 当前案例ID |
| journey_id | 当前旅程ID |
| action_type | UPDATE_OPPORTUNITY_SIGNAL |
| payload_json | {signal_type: "EQUIPMENT_FINANCING", content: "设备更新融资信号", status: "DETECTED"} |
| idempotency_key | CUST-001-EQ-FIN-20260708 |
| requested_at | 2026-07-08T17:20:00 |

### 10.4 写回通道

通过 `CrmWritebackChannel` 接口执行：

| 通道 | 实现 | 说明 |
|------|------|------|
| HttpCrmWritebackChannel | REST调用可配URL | 生产环境 |
| LoggingCrmWritebackChannel | 仅日志 | 开发/测试环境 |

### 10.5 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| HumanConfirmation | `human_confirmation` | 随Human Gate写入 |
| ControlledAction | `controlled_action` | 随写回命令写入 |
| CrmWritebackCommand | `crm_writeback_command` | `JdbcCrmWritebackCommandRepository` (如有) |
| CrmWritebackChannel | 接口 | `HttpCrmWritebackChannel.send()` |

### 10.6 编排调用链

```
CRM写回 = CrmWritebackService.executeWriteback(crmCommands[], humanConfirmation)
    → humanGate: HumanConfirmation(decision=EDIT)
    → 根据 EDIT 决策修改 CrmWritebackCommand:
        → action_type 从 CREATE_OPPORTUNITY 降级为 UPDATE_OPPORTUNITY_SIGNAL
    → crmWritebackChannel.send(command)
        → HttpCrmWritebackChannel: REST POST 到 CRM
        → LoggingCrmWritebackChannel: 日志记录
    → 创建 ControlledAction (审计轨迹)
    → domainEventPublisher.publish(ControlledActionRequested)
```

---

## 11. 环节十一：新Evidence — R7/R8迭代

> 2026-07-10 / 2026-07-20

### 11.1 剧情描述

7月10日，王强发送设备清单初稿：抬头"华东精工智能制造有限公司"，设备及配套金额3280万元，付款暂按30%/40%/30%。系统基于新证据生成R7更新报告和R8下次访前报告。

### 11.2 新Evidence

**Evidence** — 保存至 `evidence` 表

| 字段 | 值 |
|------|-----|
| evidence_id | UUID |
| source_uri | customer_message_20260710.txt |
| source_version | v1 |
| locator | 设备清单初稿 |
| content_hash | SHA-256 |
| permission_label | CUSTOMER_PROVIDED |
| license_ref | NULL |
| recorded_at | 2026-07-10T09:00:00 |

**EvidenceVersionLink** — 版本链接

| 字段 | 值 |
|------|-----|
| link_id | UUID |
| evidence_id | 当前Evidence ID |
| previous_version_id | NULL (首次) |
| version_number | 1 |
| change_type | CREATED |
| changed_by | 王强 |
| changed_at | 2026-07-10T09:00:00 |

### 11.3 产出物：R7 更新版客户经营报告

**RelationshipReport** (report_type=UPDATED) — 保存至 `relationship_report` 表

| 区域 | 内容 |
|------|------|
| 新Evidence | 设备清单初稿抬头智能制造公司，3280万，30/40/30 |
| 原判断变化 | "主体可能是智能制造公司"→证据增强但仍未正式确认 / "4800万vs3280万"→不同口径不冲突 / "3000万支持"→与设备付款联系增强但融资金额仍待确认 / 供应链信号→从弱Signal进入可做接受度分析 |
| 仍待核实 | 项目主体正式决议 / 4800万剩余1520万投资构成 / 借/用/还/担保安排 / 4000万可用额度经营用途 |

### 11.4 产出物：R8 下次访前报告

**PrevisitReportContent** — 继承R4/R5/R7全部新Evidence和未决问题

| 字段 | 值 |
|------|-----|
| visit_objective | 确认项目主体决议 / 核实1520万投资构成 / 讨论借/用/还/担保安排 |
| content_json | 继承R7的4条待核实 + R5-A的产品方向 + 专业会商结论 |

### 11.5 涉及本体模型与数据加载

| 模型 | 物化表 | 加载方式 |
|------|--------|----------|
| Evidence | `evidence` | `JdbcEvidenceRepository.save()` |
| EvidenceVersionLink | `evidence_version_link` | `JdbcEvidenceVersionLinkRepository.save()` |
| Claim | `claim` | 更新关联Evidence |
| ClaimEvidence | `claim_evidence` | 新增关联 |
| RelationshipReport | `relationship_report` | `JdbcRelationshipReportRepository.save()` |
| PrevisitReportContent | `previsit_report_content` | `JdbcPrevisitReportContentRepository.save()` |
| FactReconciliationCase | `fact_reconciliation_case` | 更新status |

### 11.6 编排调用链

```
EngagementOrchestrator.handleNewEvidence(journeyId, operatingCaseId, customerId, newEvidenceDescription, previousReportId)
    → 创建 Evidence + EvidenceVersionLink
    → 关联 ClaimEvidence (新证据 → 已有Claim)
    → reportGenerationService.generateUpdatedReport(...)
        → loadPreviousAnalysis()      // PostvisitAnalysisContent
        → loadExistingClaims()        // Claim[] (含FactReconciliationCase)
        → 评估新证据对原判断的影响
        → 组装 RelationshipReport(report_type=UPDATED) (R7)
        → saveRelationshipReport()
    → reportGenerationService.generateNextPrevisitReport(...)
        → 继承R7未决问题 + R5-A产品方向 + 专业会商结论
        → 组装 PrevisitReportContent (R8)
        → savePrevisitReportContent()
    → 更新 FactReconciliationCase.status (如有变化)
    → domainEventPublisher.publish(ClaimCandidateRecorded)
    → return NewEvidenceWorkflowResult(updatedReport, nextPrevisitReport)
```

---

## 12. 全链路本体模型物化总览

### 12.1 本体模型 → 数据库表 → 加载Repository 完整映射

| 本体模型 | 物化表 | 写入Repository | 读取Repository |
|----------|--------|---------------|---------------|
| Customer | `customer` | JdbcCustomerRepository | JdbcCustomerRepository |
| OperatingCase | `operating_case` | JdbcOperatingCaseRepository | JdbcOperatingCaseRepository |
| CustomerJourney | `customer_journey` | JdbcCustomerJourneyRepository | JdbcCustomerJourneyRepository |
| Interaction | `interaction` | JdbcInteractionRepository | JdbcInteractionRepository |
| InteractionParticipant | `interaction_participant` | JdbcCustomerJourneyRepository | JdbcCustomerJourneyRepository |
| Claim | `claim` | JdbcClaimRepository | JdbcClaimRepository |
| ClaimEvidence | `claim_evidence` | JdbcClaimRepository | JdbcClaimRepository |
| ClaimLifecycleEvent | `claim_lifecycle_event` | JdbcClaimRepository | JdbcClaimRepository |
| Evidence | `evidence` | JdbcEvidenceRepository | JdbcEvidenceRepository |
| EvidenceVersionLink | `evidence_version_link` | JdbcEvidenceVersionLinkRepository | JdbcEvidenceVersionLinkRepository |
| HumanConfirmation | `human_confirmation` | — | — |
| ControlledAction | `controlled_action` | — | — |
| ActionReceipt | `action_receipt` | — | — |
| Task | `task` | JdbcTaskRepository | JdbcTaskRepository |
| RecordingConsent | `recording_consent` | JdbcRecordingConsentRepository | JdbcRecordingConsentRepository |
| InteractionExtension | `interaction_extension` | — | — |
| OpportunitySignal | `opportunity_signal` | JdbcOpportunitySignalRepository | JdbcOpportunitySignalRepository |
| Opportunity | `opportunity` | JdbcOpportunityRepository | JdbcOpportunityRepository |
| FactReconciliationCase | `fact_reconciliation_case` | JdbcFactReconciliationRepository | JdbcFactReconciliationRepository |
| KycGapProfile | `kyc_gap_profile` | JdbcKycGapProfileRepository | JdbcKycGapProfileRepository |
| CreditFacility | `credit_facility` | JdbcCreditFacilityRepository | JdbcCreditFacilityRepository |
| BankRelationshipSnapshot | `bank_relationship_snapshot` | JdbcBankRelationshipSnapshotRepository | JdbcBankRelationshipSnapshotRepository |
| LegalEntity | `legal_entity` | JdbcLegalEntityRepository | JdbcLegalEntityRepository |
| GroupRelationship | `group_relationship` | JdbcGroupRelationshipRepository | JdbcGroupRelationshipRepository |
| Transaction | `transaction` | JdbcTransactionRepository | JdbcTransactionRepository |
| TransactionRecord | `transaction_record` | JdbcTransactionRecordRepository | JdbcTransactionRecordRepository |
| ExternalEvent | `external_event` | JdbcExternalEventRepository | JdbcExternalEventRepository |
| ProductCatalog | `product_catalog` | JdbcProductCatalogRepository | JdbcProductCatalogRepository |
| ProductKnowledgeVersion | `product_knowledge_version` | JdbcProductKnowledgeVersionRepository | JdbcProductKnowledgeVersionRepository |
| PolicyRule | `policy_rule` | JdbcPolicyRuleRepository | JdbcPolicyRuleRepository |
| PrevisitReportContent | `previsit_report_content` | JdbcPrevisitReportContentRepository | JdbcPrevisitReportContentRepository |
| PostvisitAnalysisContent | `postvisit_analysis_content` | JdbcPostvisitAnalysisContentRepository | JdbcPostvisitAnalysisContentRepository |
| RelationshipReport | `relationship_report` | JdbcRelationshipReportRepository | JdbcRelationshipReportRepository |
| OutreachScript | `outreach_script` | JdbcOutreachScriptRepository | JdbcOutreachScriptRepository |
| MeetingScript | `meeting_script` | JdbcMeetingScriptRepository | JdbcMeetingScriptRepository |
| CrmWritebackCommand | `crm_writeback_command` | — | — |

### 12.2 全链路事件流

```
startEngagementJourney(customerId)
    → OperatingCase(ACTIVE) + CustomerJourney(INSIGHT_ANALYSIS)
    → KycInsightService → KycGapProfile
    → domainEventPublisher.publish(controlledActionRequested)

executePrevisitPhase(...)
    → CustomerJourney(PREVISIT_PREP)
    → PrevisitReportContent (R1) + QuickBattleCard (R2)
    → ProductKnowledgeVersion 校验
    → domainEventPublisher.publish(PrevisitReportGenerated)

executePostvisitPhase(...)
    → CustomerJourney(POSTVISIT_REVIEW)
    → Interaction + Claim[] + Task[] + OpportunitySignal[]
    → FactReconciliationCase (3000万语义)
    → PostvisitAnalysisContent (R4)
    → RelationshipReport INTERNAL (R5-A) + CRM (R5-B)
    → CrmWritebackCommand[]
    → domainEventPublisher.publish(PostvisitAnalysisCompleted)

handleNewEvidence(...)
    → Evidence + EvidenceVersionLink
    → RelationshipReport UPDATED (R7)
    → PrevisitReportContent (R8)
    → FactReconciliationCase 更新
    → domainEventPublisher.publish(ClaimCandidateRecorded)

completeJourney(journeyId)
    → CustomerJourney(COMPLETED)
    → OperatingCase(COMPLETED)
```

### 12.3 全链路核心禁令执行点

| 禁令 | 执行环节 | 本体模型 | 机制 |
|------|----------|----------|------|
| #3 无确认不承诺 | 环节十 CRM写回 | HumanConfirmation + ControlledAction | Human Gate: 无APPROVED不执行写回 |
| #6 Claim≠Fact | 环节七 事实对账 | Claim + FactReconciliationCase + DMN | CANDIDATE不经DMN裁决不得变为VERIFIED_FACT |
| #7 Signal≠Opportunity | 环节十 CRM写回 | OpportunitySignal + HumanConfirmation | Human Gate: EDIT降级，Signal不自动升级为Opportunity |
| 数据限制声明 | 环节一 市场慧眼 | Transaction + BankRelationshipSnapshot | R0明确标注"仅本行可见" |
| 产品版本阻断 | 环节四 访前准备 | ProductKnowledgeVersion | V2.1失效不可引用，强制使用V2.2 |
| 录音同意 | 环节六 会中 | RecordingConsent | DECLINED → 切换输入模式，不依赖录音 |
