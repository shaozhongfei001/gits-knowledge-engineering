# GITS-KNE 本体模型中英文对照清单

> 从 `modules/` 7 个领域模块提取，共 62 个 record/class + 30+ 个 enum。

---

## 一、核心领域对象（Record / Class）

### 1. 运营本体模块（operational-ontology）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| OperatingCase | 经营案件 | record | 核心聚合根，承载客户经营全生命周期 |
| Claim | 主张 | record | 知识声明：候选→确认→冲突→验证事实 |
| OpportunitySignal | 机会信号 | record | 系统检测到的客户需求信号 |
| Interaction | 交互 | record | 银行与客户间的一次接触记录 |
| HumanGate | 人工关卡 | record | 需人工审批的决策节点 |
| Commitment | 承诺 | record | 客户或银行的承诺事项 |
| ExternalEvent | 外部事件 | record | 来自新闻/监管/行业的外部信息 |
| AuditTraceEntry | 审计追踪条目 | record | 操作审计日志 |
| BankRelationshipSnapshot | 银行关系快照 | record | 客户与银行关系的时点快照 |
| CreditFacility | 授信额度 | record | 客户授信信息 |
| Customer | 客户主档 | record | 客户基本信息与经营标签 |
| FactReconciliationCase | 事实对账案件 | record | 结构化事实与交互主张的对账 |
| GroupRelationship | 集团关系 | record | 集团客户关联关系 |
| PolicyRule | 策略规则 | record | 合规/业务规则定义 |
| ProductKnowledgeCard | 产品知识卡 | record | 产品知识条目 |
| ProductKnowledgeVersion | 产品知识版本 | record | 产品知识版本管理 |
| RelationshipReport | 关系报告 | record | 客户关系变化报告 |
| Transaction | 交易 | record | 客户账户交易记录 |
| ActionReceipt | 动作回执 | record | 受控动作执行后的回执 |
| HumanConfirmation | 人工确认 | record | 人工对AI提议的确认/修改/拒绝 |
| ControlledAction | 受控动作 | record | 需人工确认后执行的动作 |

### 2. 场景信使模块（scenario-hermes）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| CustomerOperatingView | 客户经营视图 | record | 聚合客户全维度经营信息 |
| CrmWritebackCommand | CRM回写命令 | record | 向CRM系统回写数据的指令 |
| OutreachScript | 外呼话术 | record | 客户经理外呼/触达的话术脚本 |
| MeetingScript | 面谈话术 | record | 客户面谈的话术脚本 |
| MeetingTranscript | 面谈记录 | record | 面谈原始记录与提取结果 |
| PostvisitAnalysisContent | 访后分析内容 | record | 访后分析报告的完整内容 |
| PrevisitReportContent | 访前报告内容 | record | 访前报告的完整内容 |
| QuickBattleCard | 速战卡 | record | 客户经理快速参考卡片 |
| InteractionExtraction | 交互提取 | record | 从交互中提取的结构化信息 |

### 3. 客户旅程模块（scenario-customer-journey）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| CustomerJourney | 客户旅程 | record | 客户经营旅程的螺旋迭代 |
| InsightClaim | 洞察主张 | record | AI生成的客户洞察 |
| ProductCandidateClaim | 产品候选主张 | record | AI推荐的产品候选 |
| PrevisitReport | 访前报告 | record | 访前准备报告摘要 |
| PostvisitAnalysis | 访后分析 | record | 访后分析摘要 |

### 4. 人工动作模块（human-action）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| RecordingConsent | 录音授权 | record | 交互录音的授权同意 |
| Task | 任务 | record | 人工待办任务 |

### 5. 评估模块（evaluation）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| EvaluationContext | 评估上下文 | record | 评估所需的输入上下文 |
| EvaluationResult | 评估结果 | record | 评估得分与维度结果 |
| RunManifest | 运行清单 | record | 一次评估运行的元数据 |

### 6. 上下文证据模块（context-evidence）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| EvidenceBundle | 证据束 | record | 聚合案件相关的事实、候选主张与证据 |
| Evidence | 证据 | record | 单条证据记录 |

### 7. 语义运行时模块（semantic-runtime）

| 英文术语 | 中文术语 | 类型 | 说明 |
|----------|---------|------|------|
| SemanticPackage | 语义包 | record | OWL本体+SHACL约束的版本化包 |

---

## 二、枚举类型

### 1. 案件与主张状态

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| CaseStatus | 案件状态 | OPEN（开启）, IN_PROGRESS（进行中）, WAITING_FOR_HUMAN（等待人工）, CLOSED（已关闭）, CANCELLED（已取消） |
| CaseType | 案件类型 | CONTINUOUS_ENGAGEMENT（持续经营）, CLAIM_RECONCILIATION（主张对账） |
| ClaimStatus | 主张状态 | CANDIDATE（候选）, CONFLICT（冲突）, REJECTED（已拒绝）, HUMAN_CONFIRMED（人工确认）, VERIFIED_FACT（验证事实） |
| ClaimType | 主张类型 | CUSTOMER_JOURNEY（客户旅程）, OPPORTUNITY（机会信号）, PRODUCT_CANDIDATE（产品候选）, CUSTOMER_STATEMENT（客户陈述）, SYSTEM_FACT（系统事实）, RISK_SIGNAL（风险信号）, COMMITMENT（承诺）, FOLLOW_UP（跟进） |
| ReconciliationStatus | 对账状态 | OPEN（开放）, RESOLVED（已解决）, ESCALATED（已升级） |

### 2. 交互与渠道

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| Channel | 渠道 | PHONE（电话沟通）, IN_PERSON（面谈）, EMAIL（邮件）, INSTANT_MESSAGE（即时消息）, VIDEO_CONFERENCE（视频会议）, SYSTEM_PUSH（系统推送）, CRM_PUSH（CRM推送）, RISK_SIGNAL_ENGINE（风险信号引擎）, AI_INSIGHT_ENGINE（AI洞察引擎）, PRODUCT_MATCH_ENGINE（产品匹配引擎）, FACE_TO_FACE（面对面拜访）, PHONE_CALL（电话回访） |
| InteractionType | 交互类型 | SIGNAL_TRIGGER（信号触发）, AI_INSIGHT_PUSH（AI洞察推送）, PHONE_CALL（电话沟通）, FACE_TO_FACE_VISIT（面对面拜访）, VIDEO_CONFERENCE（视频会议）, INSTANT_MESSAGE（即时消息）, EMAIL（邮件往来）, PRODUCT_PRESENTATION（产品推介）, CUSTOMER_COMPLAINT（客户投诉）, FOLLOW_UP（回访跟进） |
| Direction | 交互方向 | OUTBOUND（出站：银行主动）, INBOUND（入站：客户主动） |
| InteractionOutcome | 交互结果 | COMPLETED（已完成）, CUSTOMER_AGREED（客户同意）, CUSTOMER_DECLINED（客户拒绝）, CUSTOMER_DEFERRED（客户延后）, FOLLOW_UP_REQUIRED（需跟进）, INTERRUPTED（中断）, INFORMATION_GATHERED（信息采集） |
| Participant.Role | 参与者角色 | RELATIONSHIP_MANAGER（客户经理）, CUSTOMER（客户）, AI_AGENT（AI智能体）, COMPLIANCE_OFFICER（合规审核员）, PRODUCT_SPECIALIST（产品专家） |

### 3. 信号与机会

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| SignalType | 信号类型 | FINANCING_NEED（融资需求）, PRODUCT_OPPORTUNITY（产品机会）, RELATIONSHIP_CHANGE（关系变化） |
| SignalSourceType | 信号来源类型 | INTERACTION（交互）, EXTERNAL_EVENT（外部事件）, ANALYSIS（分析） |
| SignalStatus | 信号状态 | DETECTED（已检测）, CONFIRMED（已确认）, DISMISSED（已驳回）, CONVERTED（已转化） |

### 4. 人工关卡

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| GateType | 关卡类型 | A01_OUTREACH（触达）, A02_SIGNAL_CONFIRM（信号确认）, A03_OPPORTUNITY_VALIDATE（机会验证）, B01_CONTEXT_ENRICH（上下文丰富）, B02_FACT_VALIDATE（事实验证）, C01_PREVISIT_APPROVE（访前审批）, C02_REPORT_APPROVE（报告审批）, D01_PRODUCT_RECOMMEND（产品推荐）, E01_EXIT_CONFIRM（退出确认）, F01_CRM_WRITEBACK（CRM回写）, F02_CREDIT_CHECK（授信检查）, F03_PRICE_APPROVE（定价审批）, F04_RISK_REVIEW（风险审查）, F05_RECORDING_APPROVE（录音审批）, F06_CONTROLLED_ACTION（受控动作） |
| GateDecision | 关卡决策 | APPROVE（批准）, REJECT（拒绝）, MODIFY（修改）, HOLD（搁置）, DECLINE（婉拒） |
| HumanGateStatus | 人工关卡状态 | PENDING（待审）, APPROVED（已批准）, REJECTED（已拒绝）, MODIFIED（已修改） |
| HumanConfirmation.Decision | 人工确认决策 | APPROVED（批准）, MODIFIED_AND_APPROVED（修改后批准）, REJECTED（拒绝） |

### 5. 承诺与动作

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| CommitmentType | 承诺类型 | CUSTOMER_COMMITMENT（客户承诺）, BANK_COMMITMENT（银行承诺） |
| CommitmentStatus | 承诺状态 | OPEN（待履行）, FULFILLED（已履行）, OVERDUE（逾期）, CANCELLED（已取消） |
| ControlledAction.Status | 受控动作状态 | REQUESTED（已请求）, DISPATCHED（已派发）, SUCCEEDED（成功）, FAILED（失败）, COMPENSATION_REQUIRED（需补偿）, COMPENSATED（已补偿） |
| ControlledAction.Target.Operation | 目标操作 | CREATE_TASK（创建任务）, UPDATE_WHITELISTED_FIELDS（更新白名单字段） |

### 6. 客户分类

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| CustomerTier | 客户层级 | STRATEGIC（战略客户）, KEY（重点客户）, GROWTH（成长客户）, GENERAL（一般客户） |
| EnterpriseScale | 企业规模 | LARGE（大型）, MEDIUM（中型）, SMALL（小型）, MICRO（微型） |
| Industry | 行业 | MANUFACTURING（制造业）, FINANCE（金融）, TECHNOLOGY（科技）, REAL_ESTATE（房地产）, ENERGY（能源）, HEALTHCARE（医疗）, AGRICULTURE（农业）, LOGISTICS（物流）, RETAIL（零售）, OTHER（其他） |
| ListedStatus | 上市状态 | LISTED（已上市）, UNLISTED（未上市）, DELISTED（已退市） |
| RiskLevel | 风险等级 | HIGH（高）, MEDIUM（中）, LOW（低） |

### 7. 交易与外部事件

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| TransactionType | 交易类型 | DEPOSIT（存款）, WITHDRAWAL（取款）, TRANSFER_IN（转入）, TRANSFER_OUT（转出）, LOAN_DISBURSE（放款）, LOAN_REPAY（还款）, TRADE_SETTLEMENT（贸易结算）, FEE（手续费） |
| ExternalEvent.SourceType | 外部事件来源 | NEWS（新闻）, REGULATORY（监管）, INDUSTRY（行业）, SOCIAL_MEDIA（社交媒体）, OFFICIAL_ANNOUNCEMENT（官方公告） |
| ExternalEvent.Confidence | 置信度 | HIGH（高）, MEDIUM（中）, LOW（低） |
| ExternalEvent.Reliability | 可靠性 | VERIFIED（已验证）, UNVERIFIED（未验证）, DISPUTED（有争议） |

### 8. 评估与质量

| 英文术语 | 中文术语 | 值域 |
|----------|---------|------|
| EvaluationResult.GateState | 评估关卡状态 | DEV_SELF_CHECK_PASS（开发自检通过）, READY_FOR_INDEPENDENT_QA（就绪独立QA）, QA_PASS（QA通过）, BLOCKED（阻塞） |
| PolicyRule.Severity | 规则严重度 | CRITICAL（致命）, HIGH（高）, MEDIUM（中） |
| ActionReceipt.Status | 回执状态 | SUCCEEDED（成功）, FAILED（失败）, COMPENSATED（已补偿） |

---

## 三、端口接口（Port Interface）

| 英文术语 | 中文术语 | 模块 | 核心方法 |
|----------|---------|------|---------|
| OperatingCaseRepository | 经营案件仓库 | operational-ontology | findById, findByCustomerId, save |
| ClaimRepository | 主张仓库 | operational-ontology | findById, findByCaseId, save |
| OpportunitySignalRepository | 机会信号仓库 | operational-ontology | findActiveByCustomerId, save, updateStatus |
| InteractionRepository | 交互仓库 | operational-ontology | findByCustomerId, save |
| HumanGateRepository | 人工关卡仓库 | operational-ontology | findByOperatingCaseId, save, updateStatus |
| CommitmentRepository | 承诺仓库 | operational-ontology | findByOperatingCaseId, findByCustomerId, save, updateStatus |
| ExternalEventRepository | 外部事件仓库 | operational-ontology | findActiveByCustomerId, save |
| AuditTraceRepository | 审计追踪仓库 | operational-ontology | save, findByEntityTypeAndEntityId, findByActorId |
| BankRelationshipRepository | 银行关系仓库 | operational-ontology | findByCustomerId, save |
| CreditFacilityRepository | 授信额度仓库 | operational-ontology | findByCustomerId, save |
| CustomerRepository | 客户仓库 | operational-ontology | findById, findAll, save |
| FactReconciliationRepository | 事实对账仓库 | operational-ontology | findByCaseId, save, updateStatus |
| GroupRelationshipRepository | 集团关系仓库 | operational-ontology | findByGroupId, findByMemberId, save |
| PolicyRuleRepository | 策略规则仓库 | operational-ontology | findActive, save |
| ProductKnowledgeRepository | 产品知识仓库 | operational-ontology | findActive, findByCode, save |
| RelationshipReportRepository | 关系报告仓库 | operational-ontology | findByCustomerId, save |
| TransactionRepository | 交易仓库 | operational-ontology | findByCustomerId, save |
| ClaimReconciliationPort | 主张对账端口 | operational-ontology | reconcile |
| DomainEventPublisher | 领域事件发布端口 | operational-ontology | publish |
| CustomerJourneyRepository | 客户旅程查询端口 | customer-journey | findByJourneyId, findByCustomerId |
| WritableCustomerJourneyRepository | 客户旅程写入端口 | customer-journey | saveJourney, saveInsight, saveProductCandidate, savePrevisitReport, savePostvisitAnalysis |
| CrmWritebackChannel | CRM回写通道端口 | human-action | send |
| ActionDispatchPort | 动作派发端口 | human-action | dispatch |
| RecordingConsentRepository | 录音授权仓库 | human-action | findByInteractionId, save |
| TaskRepository | 任务仓库 | human-action | findByOperatingCaseId, save, updateStatus |
| EvaluationPort | 评估端口 | evaluation | evaluate |
| ContextAssemblyPort | 上下文组装端口 | context-evidence | assemble |
| SemanticRepositoryPort | 语义仓库端口 | semantic-runtime | loadPackage, validate |
| LlmClient | LLM客户端端口 | scenario-hermes | complete |

---

## 四、旅程阶段（JourneyPhase）

| 英文术语 | 中文术语 | 说明 |
|----------|---------|------|
| SIGNAL_DETECTED | 信号检测 | 系统检测到客户行为变化 |
| INSIGHT_GENERATED | 洞察生成 | AI分析生成客户洞察 |
| PRODUCT_MATCHED | 产品匹配 | AI推荐匹配产品 |
| PREVISIT_PREPARED | 访前准备 | 生成访前报告与话术 |
| IN_VISIT | 面谈中 | 客户经理与客户面谈 |
| POSTVISIT_ANALYZED | 访后分析 | 生成访后分析报告 |
| COMPLETED | 已完成 | 旅程闭环 |

---

## 五、关系报告类型（RelationshipReport.ReportType）

| 英文术语 | 中文术语 | 说明 |
|----------|---------|------|
| INTERNAL_RELATIONSHIP | 内部关系 | 银行内部对客户关系的评估 |
| CRM_CALL | CRM通话 | CRM系统记录的通话 |
| UPDATED_RELATIONSHIP | 更新关系 | 关系变化后的更新记录 |
| NEXT_PREVISIT | 下次访前 | 下次拜访前的关系预判 |

---

## 六、业务流程关键概念

| 英文术语 | 中文术语 | 说明 |
|----------|---------|------|
| Engagement Journey | 经营旅程 | 客户经营的螺旋迭代闭环 |
| Spiral Iteration | 螺旋迭代 | Previsit → Postvisit → Product Recommend 的循环 |
| Claim Lifecycle | 主张生命周期 | Candidate → Conflict → Human Confirmed → Verified Fact |
| Human Gate | 人工关卡 | AI不可自主决策，需人工审批的节点 |
| Controlled Action | 受控动作 | 需人工确认后才可执行的系统动作 |
| Claim Reconciliation | 主张对账 | 结构化事实与交互主张的对账校验 |
| KYC Gap | KYC缺口 | 客户信息中尚未了解的部分 |
| Context Inheritance | 上下文继承 | 新旅程继承历史旅程的上下文 |
| Evidence Bundle | 证据束 | 聚合案件相关事实与证据的数据包 |
| CRM Writeback | CRM回写 | 将经营结果写回CRM系统 |
| Idempotency Key | 幂等键 | 保证动作不重复执行的唯一标识 |
