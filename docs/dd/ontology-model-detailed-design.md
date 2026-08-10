# GITS 知识工程本体模型详细设计

> 版本：V1.1 | 日期：2026-08-10

---

## 1. 架构概述

采用 **语义-运行双核架构** (ADR-0001)，本体模型分三层：

| 层次 | 职责 | 技术载体 |
|------|------|----------|
| 语义合同层 | 形式化本体定义 | OWL/TTL, LinkML YAML, R2RML |
| 运行控制平面 | 不可变值对象、业务逻辑 | Java Record |
| 场景应用层 | 面向业务场景的视图模型 | Java Record (scenario-hermes) |

核心原则：Java Record 不可变值对象；六边形架构端口；Claim ≠ Fact；Signal ≠ Opportunity；Customer 为领域引用 (ADR-0010)。

### 1.1 定义存储索引

本体模型定义分散在多个层次和载体中，下表为完整的定义存储位置索引：

| 层次 | 文件路径 | 说明 |
|------|----------|------|
| 语义层（OWL本体） | `specs/semantic/gits-core.owl.ttl` | RDF/Turtle 格式的 OWL 本体，定义类、属性、公理 |
| 语义层（LinkML契约） | `specs/semantic/gits-core.linkml.yaml` | LinkML Schema，定义类、槽位、枚举的结构化契约 |
| 数据映射（R2RML） | `specs/data/customer-source-mapping.r2rml.ttl` | 关系数据 → RDF 的映射规则 |
| 决策规则（DMN） | `specs/rules/claim-reconciliation.dmn` | Claim 裁决决策表 |
| 运行层（Java实体） | `modules/operational-ontology/src/main/java/com/gien/gits/ontology/` | Record 领域实体、枚举、端口接口 |
| 场景视图（Java） | `modules/scenario-hermes/src/main/java/com/gien/gits/engagement/` | 场景化视图模型 |
| 持久化（SQL迁移） | `adapters/persistence-relational/src/main/resources/db/migration/h2/` | Flyway 迁移脚本（V001–V013） |
| 详细设计文档 | `docs/dd/ontology-model-detailed-design.md` | 本文档，汇总性本体模型详细设计 |

> **单一真相源**：`specs/semantic/` 下的 OWL + LinkML 为本体的权威定义；Java 实体和 SQL 表结构是其在运行态的落地实现。

---

## 2. 形式化规范层

### 2.1 OWL 本体 (`gits-core.owl.ttl`)

命名空间：`http://gits.gien.com/ontology/core#`

**核心类**：OperatingCase, Interaction, Claim, Evidence, HumanConfirmation, Action, Receipt, Evaluation, Customer

**对象属性**：`belongsToCase`(Interaction→OperatingCase), `producedBy`(Claim→Interaction), `supportedBy`(Claim→Evidence), `authorizedBy`(Action→HumanConfirmation), `producedReceipt`(Action→Receipt)

### 2.2 LinkML 合同 (`gits-core.linkml.yaml`)

8个核心类，标识符及必填属性：

- **OperatingCase**: case_id | case_type, status, purpose, valid_from
- **Interaction**: interaction_id | case_id, occurred_at, channel, source_uri, source_version, source_hash
- **Claim**: claim_id | case_id, claim_type, claim_status, statement_text
- **Evidence**: evidence_id | source_uri, source_version, locator, content_hash, permission_label
- **HumanConfirmation**: confirmation_id | subject_type, subject_id, decision, actor_id, confirmed_at
- **ControlledAction**: action_id | proposal_id, confirmation_id, target_system, target_object_type, target_object_id, operation, payload_json
- **ActionReceipt**: receipt_id | action_id, status, received_at
- **Evaluation**: evaluation_id | run_manifest_id, case_set_version, gate_state, metrics_json

### 2.3 R2RML 映射 (`customer-source-mapping.r2rml.ttl`)

`EDWCRM.A_ZHCX_CUST_BASE` → `gits:Customer`：CUST_ID→customerId, CUST_NAME→customerName, UNIFIED_SOCIAL_CREDIT_CODE→unifiedSocialCreditCode, INDUSTRY→industry, ENTERPRISE_SCALE→enterpriseScale, CUSTOMER_TIER→customerTier, RISK_LEVEL→riskLevel, LISTED_STATUS→listedStatus, RM_ID→rmId

### 2.4 DMN 决策规则 (`claim-reconciliation.dmn`)

输入：conflictDetected, authoritativeMatch, evidenceComplete (boolean)

| conflict | authMatch | evidence | → status |
|:-:|:-:|:-:|---|
| F | T | T | VERIFIED_FACT |
| F | T | F | CANDIDATE |
| T | T | T | CONFLICTING |
| T | F | - | CONFLICTING |
| F | F | T | CANDIDATE |
| F | F | F | INSUFFICIENT |

---

## 3. 核心实体模型

> 包：`com.gien.gits.ontology` | 所有实体为 Java record

### 3.1 Customer — 客户

```
Customer(
  String          customerId,              // 主键，权威来源EDWCRM
  String          customerName,            // 客户名称
  String          customerShortName,       // 客户简称
  String          unifiedSocialCreditCode, // 统一社会信用代码
  LocalDate       establishedDate,         // 成立日期
  Long            registeredCapitalCny,    // 注册资本（元）
  Industry        industry,                // 行业枚举
  String          region,                  // 地区
  EnterpriseScale enterpriseScale,         // 企业规模枚举
  CustomerTier    customerTier,            // 客户层级枚举
  LocalDate       relationshipSince,       // 建立关系日期
  String          rmId,                    // 客户经理ID
  String          rmName,                  // 客户经理姓名
  String          managingBranch,          // 管辖分支行
  boolean         groupFlag,               // 是否集团客户
  ListedStatus    listedStatus,            // 上市状态枚举
  RiskLevel       riskLevel,               // 风险等级枚举
  String          mainProducts,            // 主要产品（JSON）
  String          coreTags,                // 核心标签（JSON）
  String          relationshipSummary      // 关系摘要
)
```

业务语义：对公客户引用实体，非控制平面实体（ADR-0010）。权威数据源为EDWCRM，本系统仅引用不修改。

内嵌枚举：Industry(MANUFACTURING|FINANCE|TECHNOLOGY|REAL_ESTATE|ENERGY|HEALTHCARE|AGRICULTURE|LOGISTICS|RETAIL|OTHER), EnterpriseScale(LARGE|MEDIUM|SMALL|MICRO), CustomerTier(STRATEGIC|KEY|GROWTH|GENERAL), ListedStatus(LISTED|UNLISTED|DELISTED), RiskLevel(HIGH|MEDIUM|LOW)

### 3.2 OperatingCase — 经营案例

```
OperatingCase(
  String      caseId,         // 主键
  CaseType    caseType,       // 案例类型枚举
  CaseStatus  status,         // 案例状态枚举
  String      purpose,        // 案例目的
  LocalDateTime validFrom,    // 生效时间
  LocalDateTime validTo,      // 失效时间（可空）
  LocalDateTime recordedAt,   // 记录时间
  long        recordVersion,  // 记录版本（乐观锁）
  String      createdBy       // 创建人
)
```

业务语义：跨越多次交互的持久业务事项，是所有 Claim/Interaction 的容器。

状态机：`ACTIVE → CLOSED | SUSPENDED`

内嵌枚举：CaseType(CONTINUOUS_ENGAGEMENT|CLAIM_RECONCILIATION), CaseStatus(ACTIVE|CLOSED|SUSPENDED)

### 3.3 Interaction — 交互

```
Interaction(
  String       interactionId,         // 主键
  String       caseId,                // 所属案例ID（FK → OperatingCase）
  String       journeyId,             // 所属旅程ID
  String       interactionType,       // 交互类型
  String       direction,             // 方向（OUTBOUND / INBOUND）
  Channel      channel,               // 渠道枚举
  String       contentSummary,        // 内容摘要
  String       outcome,               // 交互结果
  String       initiatorId,           // 发起人ID
  String       initiatorRole,         // 发起人角色
  String       initiatorDisplayName,  // 发起人显示名
  String       producedClaimIds,      // 产出的Claim ID列表（JSON）
  LocalDateTime occurredAt,           // 发生时间
  LocalDateTime endedAt,              // 结束时间（可空）
  String       sourceUri,             // 来源URI
  String       sourceVersion,         // 来源版本
  String       sourceHash,            // 源数据哈希
  LocalDateTime recordedAt            // 记录时间
)
```

业务语义：案例内的一次交互事件，是Claim的产生来源。AI发起的交互产出只能是CANDIDATE状态。

关键行为方法：`involvesHuman()` — 是否涉及人工参与；`isAiInitiated()` — AI发起的交互产出只能是CANDIDATE

### 3.4 Claim — 声明

```
Claim(
  String       claimId,            // 主键
  String       caseId,             // 所属案例ID（FK → OperatingCase）
  String       interactionId,      // 来源交互ID（FK → Interaction）
  ClaimType    claimType,          // 声明类型枚举
  ClaimStatus  claimStatus,        // 声明状态枚举
  String       statementText,      // 声明文本
  LocalDateTime validFrom,         // 生效时间
  LocalDateTime validTo,           // 失效时间（可空）
  LocalDateTime recordedAt,        // 记录时间
  String       supersedesClaimId,  // 取代的声明ID（版本链）
  String       modelRunId          // 模型运行ID（AI产出时标记）
)
```

业务语义：交互中产生的声明/主张。**Claim ≠ Fact（禁令#6）**：AI产出只能为CANDIDATE，须经DMN裁决+人工确认才可升格为VERIFIED_FACT。

状态机：`CANDIDATE → VERIFIED_FACT | CONFLICTING | INSUFFICIENT | REJECTED`，`VERIFIED_FACT → SUPERSEDED`

内嵌枚举：ClaimType(CUSTOMER_JOURNEY|OPPORTUNITY|PRODUCT_CANDIDATE|CUSTOMER_STATEMENT|SYSTEM_FACT|RISK_SIGNAL|COMMITMENT|FOLLOW_UP), ClaimStatus(CANDIDATE|VERIFIED_FACT|CONFLICTING|INSUFFICIENT|SUPERSEDED|REJECTED)

关键行为方法：`needsReconciliation()` — 是否需要DMN裁决；`isSuperseded()` — 是否已被新声明取代

### 3.5 Evidence — 证据

```
Evidence(
  String       evidenceId,      // 主键
  String       sourceUri,       // 来源URI
  String       sourceVersion,   // 来源版本
  String       locator,         // 定位器（精确位置标识）
  String       contentHash,     // 内容哈希（完整性校验）
  String       permissionLabel, // 权限标签
  String       licenseRef,      // 许可证引用
  LocalDateTime recordedAt      // 记录时间
)
```

业务语义：支撑或反驳Claim的客观证据。通过contentHash保证内容完整性，通过EvidenceVersionLink追踪版本演进。

### 3.6 HumanConfirmation — 人工确认

```
HumanConfirmation(
  String       confirmationId,      // 主键
  String       subjectType,         // 确认对象类型（CLAIM / ACTION / SIGNAL）
  String       subjectId,           // 确认对象ID
  String       decision,            // 决策结果（APPROVED / REJECTED / DEFERRED）
  String       actorId,             // 操作人ID
  String       actorRole,           // 操作人角色
  String       permissionDecisionId,// 权限决策ID
  LocalDateTime confirmedAt,        // 确认时间
  String       commentText          // 评论文本（可空）
)
```

业务语义：人工对声明或操作的确认记录。是受控操作的前置条件，也是Claim升格的必要步骤。

### 3.7 ControlledAction — 受控操作

```
ControlledAction(
  String       actionId,          // 主键
  String       proposalId,        // 提案ID
  String       confirmationId,    // 人工确认ID（FK → HumanConfirmation）
  String       targetSystem,      // 目标系统（CRM / CORE_BANKING）
  String       targetObjectType,  // 目标对象类型
  String       targetObjectId,    // 目标对象ID
  String       expectedVersion,   // 期望版本（乐观锁）
  String       operation,         // 操作类型（CREATE / UPDATE / DELETE）
  String       payloadJson,       // 操作载荷（JSON）
  String       idempotencyKey,    // 幂等键
  String       status,            // 操作状态（PENDING / EXECUTED / FAILED）
  LocalDateTime requestedAt       // 请求时间
)
```

业务语义：经人工确认后方可执行的外部系统操作。**无确认的授信承诺禁止（禁令#3）**。

状态机：`PENDING → EXECUTED | FAILED`

关键行为方法：`isAuthorized()` — 是否已获授权；`isIdempotent()` — 幂等键是否已设置

### 3.8 ActionReceipt — 操作回执

```
ActionReceipt(
  String       receiptId,           // 主键
  String       actionId,            // 关联操作ID（FK → ControlledAction）
  String       status,              // 回执状态（SUCCESS / FAILURE / TIMEOUT）
  String       targetVersionAfter,  // 操作后目标版本
  String       failureCode,         // 失败码（可空）
  String       rawReceiptHash,      // 原始回执哈希
  LocalDateTime receivedAt          // 接收时间
)
```

业务语义：外部系统执行ControlledAction后返回的回执，确认操作结果。

### 3.9 EvaluationResult — 评估结果

```
EvaluationResult(
  String       evaluationId,   // 主键
  String       runManifestId,  // 运行清单ID
  String       caseSetVersion, // 案例集版本
  String       gateState,      // 门控状态（PASS / FAIL / WARNING）
  String       metricsJson,    // 评估指标（JSON）
  LocalDateTime evaluatedAt    // 评估时间
)
```

业务语义：评估运行记录，用于质量门控。独立实体。

### 3.10 OpportunitySignal — 机会信号

```
OpportunitySignal(
  String       signalId,         // 主键
  String       operatingCaseId,  // 所属案例ID（FK → OperatingCase）
  String       journeyId,        // 所属旅程ID
  String       signalType,       // FINANCING_NEED | PRODUCT_OPPORTUNITY | RELATIONSHIP_CHANGE
  String       content,          // 信号内容
  String       sourceType,       // INTERACTION | EXTERNAL_EVENT | ANALYSIS
  String       sourceRef,        // 来源引用
  BigDecimal   confidence,       // 置信度（0.0~1.0）
  String       status,           // DETECTED | CONFIRMED | DISMISSED | CONVERTED
  String       evidenceRef,      // 证据引用
  LocalDateTime detectedAt,      // 检测时间
  LocalDateTime confirmedAt      // 确认时间（可空）
)
```

业务语义：从交互或外部事件中检测到的业务机会信号。**Signal ≠ Opportunity（禁令#7）**。

状态机：`DETECTED → CONFIRMED | DISMISSED`，`CONFIRMED → CONVERTED`

关键行为方法：`needsConfirmation()` — 是否需要人工确认；`isConvertible()` — 是否可转化为Opportunity

### 3.11 KycGapProfile — KYC缺口画像

```
KycGapProfile(
  String       profileId,                    // 主键
  String       customerId,                   // 客户ID（FK → Customer）
  LocalDate    asOf,                         // 截止日期
  String       knownItems,                   // 已知项（JSON）
  String       partialKnownItems,            // 部分已知项（JSON）
  String       staleItems,                   // 过时项（JSON）
  String       conflictingOrAmbiguousItems,  // 冲突或模糊项（JSON）
  String       unknownItems,                 // 未知项（JSON）
  String       priorityQuestions,            // 优先问题（JSON）
  String       overallCompleteness,          // 整体完备度
  String       riskImpact,                   // 风险影响
  String       lastAssessedBy,               // 最后评估人
  LocalDateTime lastAssessedAt               // 最后评估时间
)
```

业务语义：客户KYC信息完备度画像，按5级分类标识信息缺口。驱动拜访前问题准备。

### 3.12 ProductKnowledgeCard — 产品知识卡

```
ProductKnowledgeCard(
  String       productId,         // 主键
  String       name,              // 产品名称
  String       definition,        // 产品定义
  String       keyConditions,     // 关键条件（JSON）
  String       requiredMaterials, // 所需材料（JSON）
  String       riskPoints,        // 风险要点（JSON）
  String       trigger,           // 触发条件
  String       prohibitedPhrases, // 禁止用语（JSON）
  String       evidenceSource     // 证据来源
)
```

业务语义：银行产品知识条目，约束AI生成话术。prohibitedPhrases确保不使用违规用语。

### 3.13 Commitment — 承诺

```
Commitment(
  String       commitmentId,     // 主键
  String       operatingCaseId,  // 所属案例ID（FK → OperatingCase）
  String       journeyId,        // 所属旅程ID
  String       commitmentType,   // CUSTOMER_COMMITMENT | BANK_COMMITMENT
  String       content,          // 承诺内容
  String       owner,            // 承诺方
  LocalDate    dueDate,          // 到期日期
  String       status,           // OPEN | FULFILLED | OVERDUE | CANCELLED
  String       evidenceRef,      // 证据引用
  String       interactionId,    // 关联交互ID
  String       customerId,       // 客户ID
  String       fulfilledDate,    // 履行日期（可空）
  String       assignedTo,       // 负责人
  String       verifiedBy,       // 验证人
  LocalDateTime recordedAt       // 记录时间
)
```

业务语义：交互中产生的双方承诺。**无确认的授信承诺禁止（禁令#3）**。

状态机：`OPEN → FULFILLED | OVERDUE | CANCELLED`

### 3.14 ExternalEvent — 外部事件

```
ExternalEvent(
  String       eventId,               // 主键
  LocalDate    eventDate,             // 事件日期
  String       sourceType,            // 来源类型
  String       sourceName,            // 来源名称
  String       entity,                // 相关实体
  String       title,                 // 标题
  String       content,               // 内容
  String       confidence,            // HIGH | MEDIUM | LOW
  String       reliability,           // VERIFIED | UNVERIFIED | DISPUTED
  boolean      bankUseAllowed,        // 是否允许银行使用
  String       linkedThemes,          // 关联主题（JSON）
  String       possibleBusinessSignal,// 可能的业务信号
  String       noGoStatement,         // 禁止声明
  String       evidenceRef,           // 证据引用
  String       severity,              // 严重程度
  String       affectedIndustries,    // 受影响行业（JSON）
  String       affectedCustomerIds,   // 受影响客户ID（JSON）
  LocalDateTime detectedAt,           // 检测时间
  String       rawPayload             // 原始载荷
)
```

业务语义：外部信息源推送的事件。bankUseAllowed标记数据合规性，noGoStatement标识不可使用场景。

### 3.15 FactReconciliationCase — 事实核查案例

```
FactReconciliationCase(
  String       reconciliationId,    // 主键
  String       caseId,              // 所属案例ID（FK → OperatingCase）
  String       topic,               // 核查主题
  String       structuredFact,      // 结构化事实（JSON）
  String       interactionClaim,    // 交互声明（JSON）
  String       externalFact,        // 外部事实（JSON）
  String       ontologyDistinction, // 本体区分（JSON）
  String       correctJudgment,     // 正确判断
  String       wrongOutputExamples, // 错误输出示例（JSON）
  String       nextAction,          // 下一步操作
  String       status               // OPEN | RESOLVED | ESCALATED
)
```

业务语义：当Claim与外部事实冲突时创建的核查案例。

状态机：`OPEN → RESOLVED | ESCALATED`

### 3.16 PolicyRule — 政策规则

```
PolicyRule(
  String       ruleId,         // 主键
  String       name,           // 规则名称
  String       severity,       // CRITICAL | HIGH | MEDIUM
  String       logic,          // 规则逻辑
  String       requiredOutput  // 必要输出
)
```

业务语义：合规政策规则定义，约束AI产出。

### 3.17 CreditFacility — 授信额度

```
CreditFacility(
  String       facilityId,                    // 主键
  String       customerId,                    // 客户ID（FK → Customer）
  String       borrowerEntity,                // 借款主体
  LocalDate    approvalDate,                  // 批准日期
  LocalDate    maturityDate,                  // 到期日期
  Long         creditTotalCny,                // 授信总额（元）
  Long         usedCreditCny,                 // 已用额度（元）
  Long         availableCreditCny,            // 可用额度（元）
  Long         currentLoanBalanceCny,          // 当前贷款余额（元）
  Long         bankAcceptanceBillBalanceCny,   // 银承余额（元）
  Long         guaranteeBalanceCny,            // 担保余额（元）
  String       collateral,                    // 抵押物（JSON）
  String       purposeAllowed,                // 允许用途（JSON）
  String       purposeRestrictions,           // 用途限制（JSON）
  String       covenants,                     // 契约条款（JSON）
  String       reconciliationNote,            // 核查备注
  String       evidenceRef                    // 证据引用
)
```

业务语义：客户授信额度信息。**未经确认的授信承诺禁止（禁令#3）**。

### 3.18 BankRelationshipSnapshot — 银行关系快照

```
BankRelationshipSnapshot(
  String       id,                            // 主键
  String       customerId,                    // 客户ID（FK → Customer）
  String       snapshotMonth,                 // 快照月份（yyyy-MM）
  Long         avgDailyDepositCny,            // 日均存款（元）
  Long         monthlySettlementCny,          // 月结算量（元）
  Long         loanBalanceCny,                // 贷款余额（元）
  Long         creditTotalCny,                // 授信总额（元）
  Long         usedCreditCny,                 // 已用额度（元）
  Long         availableCreditCny,            // 可用额度（元）
  Long         bankAcceptanceBillBalanceCny,   // 银承余额（元）
  Long         guaranteeBalanceCny,            // 担保余额（元）
  Integer      payrollEmployees,              // 代发工资人数
  boolean      cashManagementOpened,          // 是否开通现金管理
  boolean      supplyChainFinanceOpened,      // 是否开通供应链金融
  Long         crossBorderSettlementCny,      // 跨境结算量（元）
  Integer      productCount,                  // 产品数量
  String       customerContributionLevel,     // 客户贡献等级
  String       anomalyFlags                   // 异常标记
)
```

业务语义：客户与银行关系的月度快照，用于趋势分析和异常检测。

### 3.19 GroupRelationship — 集团关系

```
GroupRelationship(
  String       id,               // 主键
  String       groupId,          // 集团ID
  String       fromEntityId,     // 来源实体ID
  String       toEntityId,       // 目标实体ID
  String       relationshipType, // 关系类型（控股/参股/关联）
  Integer      ownershipRatio    // 持股比例（%）
)
```

业务语义：集团内法人实体间的股权/关联关系。

### 3.20 LegalEntity — 法人实体

```
LegalEntity(
  String       entityId,           // 主键
  String       groupId,            // 集团ID
  String       name,               // 实体名称
  String       role,               // 角色（母公司/子公司/关联公司）
  String       ownership,          // 所有权描述
  String       bankCustomerId,     // 银行客户ID（可空）
  String       relationshipStatus, // 关系状态
  String       evidenceRef         // 证据引用
)
```

业务语义：集团内法人实体，通过groupId与GroupRelationship关联构成集团关系图谱。

### 3.21 Transaction — 交易

```
Transaction(
  String       id,                  // 主键
  String       transactionId,       // 交易ID（唯一）
  String       customerId,          // 客户ID
  String       accountId,           // 账户ID
  String       transactionType,     // DEPOSIT|WITHDRAWAL|TRANSFER_IN|TRANSFER_OUT|LOAN_DISBURSE|LOAN_REPAY|TRADE_SETTLEMENT|FEE
  BigDecimal   amount,              // 金额
  String       currency,            // 币种（默认CNY）
  String       counterparty,        // 交易对手
  String       counterpartyIndustry,// 交易对手行业
  String       description,         // 描述
  LocalDate    transactionDate      // 交易日期
)
```

业务语义：客户交易流水记录，用于经营分析和异常检测。

### 3.22 TransactionRecord — 交易记录

```
TransactionRecord(
  String       id,              // 主键
  String       customerId,      // 客户ID（FK → Customer）
  LocalDate    transactionDate, // 交易日期
  String       transactionType, // 交易类型
  String       counterparty,    // 交易对手
  Long         amountCny,       // 金额（元）
  String       description,     // 描述
  String       evidenceRef      // 证据引用
)
```

业务语义：精简版交易记录，关联证据链。

### 3.23 RelationshipReport — 关系报告

```
RelationshipReport(
  String       reportId,              // 主键
  String       operatingCaseId,       // 所属案例ID（FK → OperatingCase）
  String       journeyId,             // 所属旅程ID
  String       reportType,            // INTERNAL_RELATIONSHIP | CRM_CALL | UPDATED_RELATIONSHIP | NEXT_PREVISIT
  String       content,               // 报告内容
  String       basedOnEvidence,       // 基于证据（JSON）
  String       basedOnReconciliations // 基于核查（JSON）
)
```

业务语义：基于证据和核查结果生成的关系报告，追踪数据来源确保可审计。

### 3.24 Channel — 渠道

```
Channel(
  String       name    // 渠道名称
)
```

业务语义：交互渠道枚举实体。

枚举值：PHONE | IN_PERSON | EMAIL | INSTANT_MESSAGE | VIDEO_CONFERENCE | SYSTEM_PUSH | CRM_PUSH | RISK_SIGNAL_ENGINE | AI_INSIGHT_ENGINE | PRODUCT_MATCH_ENGINE | FACE_TO_FACE | PHONE_CALL

---

## 4. 枚举定义

| 枚举 | 值 | 说明 |
|------|-----|------|
| CaseStatus | ACTIVE, CLOSED, SUSPENDED | 案例状态 |
| CaseType | CONTINUOUS_ENGAGEMENT, CLAIM_RECONCILIATION | 案例类型 |
| ClaimStatus | CANDIDATE, VERIFIED_FACT, CONFLICTING, INSUFFICIENT, SUPERSEDED, REJECTED | 声明状态 |
| ClaimType | CUSTOMER_JOURNEY, OPPORTUNITY, PRODUCT_CANDIDATE, CUSTOMER_STATEMENT, SYSTEM_FACT, RISK_SIGNAL, COMMITMENT, FOLLOW_UP | 声明类型 |
| ReconciliationStatus | VERIFIED_FACT, CANDIDATE, CONFLICTING, INSUFFICIENT | 核查结果状态 |
| Industry | MANUFACTURING, FINANCE, TECHNOLOGY, REAL_ESTATE, ENERGY, HEALTHCARE, AGRICULTURE, LOGISTICS, RETAIL, OTHER | 行业分类 |
| RiskLevel | HIGH, MEDIUM, LOW | 风险等级 |
| CustomerTier | STRATEGIC, KEY, GROWTH, GENERAL | 客户层级 |
| EnterpriseScale | LARGE, MEDIUM, SMALL, MICRO | 企业规模 |
| ListedStatus | LISTED, UNLISTED, DELISTED | 上市状态 |

---

## 5. 领域子包模型

> 包：`com.gien.gits.ontology.domain`

### 5.1 ClaimLifecycleEvent — 声明生命周期事件

```
ClaimLifecycleEvent(
  String       eventId,          // 主键
  String       claimId,          // 声明ID（FK → Claim）
  String       fromStatus,       // 原状态
  String       toStatus,         // 新状态
  String       transitionReason, // 转换原因
  String       actorId,          // 操作人ID
  String       actorRole,        // 操作人角色
  LocalDateTime transitionedAt  // 转换时间
)
```

业务语义：声明状态变更的审计日志，记录每次状态转换的完整上下文。

### 5.2 EvidenceVersionLink — 证据版本链接

```
EvidenceVersionLink(
  String       linkId,            // 主键
  String       evidenceId,        // 证据ID（FK → Evidence）
  String       previousVersionId, // 前一版本ID（可空）
  String       nextVersionId,     // 下一版本ID（可空）
  int          versionNumber,     // 版本号
  String       changeType,        // 变更类型（CREATE / UPDATE / CORRECT）
  String       changeReason,      // 变更原因
  String       changedBy,         // 变更人
  LocalDateTime changedAt         // 变更时间
)
```

业务语义：证据版本演进链，追踪证据从创建到修正的完整历史。

### 5.3 InteractionExtension — 交互扩展

```
InteractionExtension(
  String       extensionId,        // 主键
  String       interactionId,      // 交互ID（UK, FK → Interaction）
  String       recordingConsentId, // 录音录像同意ID
  String       commitmentIds,      // 承诺ID列表（JSON）
  String       taskIds,            // 任务ID列表（JSON）
  String       opportunityIds,     // 机会ID列表（JSON）
  String       kycGapProfileId,    // KYC缺口画像ID
  LocalDateTime createdAt,         // 创建时间
  LocalDateTime updatedAt          // 更新时间
)
```

业务语义：交互的扩展属性，与Interaction一对一关联。

### 5.4 Opportunity — 机会

```
Opportunity(
  String       opportunityId,    // 主键
  String       customerId,       // 客户ID
  String       interactionId,    // 交互ID
  String       operatingCaseId,  // 案例ID
  String       opportunityType,  // 机会类型
  String       productId,        // 产品ID
  String       productName,      // 产品名称
  String       description,      // 描述
  String       status,           // 机会状态
  String       estimatedAmount,  // 预估金额
  String       probability,      // 成功概率
  String       assignedTo,       // 负责人
  String       source,           // 来源
  String       nextSteps,        // 下一步（JSON）
  String       expectedCloseDate,// 预计关闭日期
  LocalDateTime createdAt,       // 创建时间
  LocalDateTime updatedAt        // 更新时间
)
```

业务语义：经确认的业务机会，由OpportunitySignal转化而来。**Signal ≠ Opportunity（禁令#7）**。

### 5.5 ProductKnowledgeVersion — 产品知识版本

```
ProductKnowledgeVersion(
  String       versionId,         // 主键
  String       productId,         // 产品ID（FK → ProductKnowledgeCard）
  int          versionNumber,     // 版本号
  String       productName,       // 产品名称
  String       category,          // 分类
  String       description,       // 描述
  String       keyFeatures,       // 关键特性（JSON）
  String       targetIndustries,  // 目标行业（JSON）
  String       riskLevel,         // 风险等级
  String       requiredMaterials, // 所需材料（JSON）
  String       pricingBasis,      // 定价基础
  String       previousVersionId, // 前一版本ID
  String       changeSummary,     // 变更摘要
  String       changedBy,         // 变更人
  LocalDateTime changedAt         // 变更时间
)
```

业务语义：产品知识的版本化记录，追踪知识条目的变更历史。

---

## 6. 端口接口

> 包：`com.gien.gits.ontology.port`

### 6.1 ClaimReconciliationPort — 声明裁决端口

```java
ReconciliationResult reconcile(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete);
```

业务语义：声明裁决的抽象端口，将DMN决策逻辑与核心业务解耦。

实现：
- `DmnClaimReconciliationAdapter`：KIE DMN运行时加载 `claim-reconciliation.dmn`
- `FallbackClaimReconciliationAdapter`：手写if-else逻辑（降级方案）

### 6.2 DomainEventPublisher — 领域事件发布端口

```java
void publish(CloudEvent event);
```

业务语义：领域事件发布抽象端口，解耦事件发布与业务逻辑。

实现：`SpringEventPublisher`（包装 `ApplicationEventPublisher`）

发布时机：

| 服务 | 触发点 | 事件类型 |
|------|--------|----------|
| EngagementOrchestrator | startJourney | JOURNEY_STARTED |
| EngagementOrchestrator | postvisit | POSTVISIT_COMPLETED |
| EngagementOrchestrator | newEvidence | EVIDENCE_RECEIVED |
| KycInsightService | claimCandidateRecorded | CLAIM_CANDIDATE_RECORDED |
| CrmWritebackService | controlledActionRequested | CONTROLLED_ACTION_REQUESTED |

### 6.3 ScenarioDataProvider — 场景数据提供端口

业务语义：提供场景数据给运行本体，解耦数据获取与业务逻辑。

---

## 7. 场景视图模型

> 包：`com.gien.gits.engagement`

### 7.1 CustomerOperatingView — 客户经营视图

```
CustomerOperatingView(
  String                        customerId,         // 客户ID
  String                        customerName,       // 客户名称
  String                        industry,           // 行业
  String                        enterpriseScale,    // 企业规模
  String                        customerTier,       // 客户层级
  String                        riskLevel,          // 风险等级
  String                        rmId,               // 客户经理ID
  String                        rmName,             // 客户经理姓名
  boolean                       groupFlag,          // 是否集团
  String                        relationshipSummary,// 关系摘要
  List<Interaction>             recentInteractions, // 近期交互
  BankRelationshipSnapshot      bankRelationship,   // 银行关系快照
  List<CreditFacility>          creditFacilities,   // 授信额度
  KycGapProfile                 kycGapProfile,      // KYC缺口画像
  List<OpportunitySignal>       opportunitySignals  // 机会信号
)
```

业务语义：面向客户经理的聚合视图，整合客户基本信息、银行关系、KYC缺口和机会信号。

### 7.2 PrevisitReportContent — 拜访前报告

```
PrevisitReportContent(
  String       id,              // 主键
  String       analysisId,      // 分析ID
  String       journeyId,       // 旅程ID
  String       operatingCaseId, // 案例ID（FK → OperatingCase）
  String       visitObjective,  // 拜访目标
  String       contentJson      // 报告内容（JSON）
)
```

业务语义：AI生成的拜访前准备报告，包含客户洞察、话题建议、风险提示。

### 7.3 QuickBattleCard — 快速作战卡

```
QuickBattleCard(
  String       cardId,           // 主键
  String       customerId,       // 客户ID（FK → Customer）
  String       category,         // 分类
  String       title,            // 标题
  String       summary,          // 摘要
  String       keyPoints,        // 关键要点（JSON）
  String       riskAlerts,       // 风险提示（JSON）
  String       suggestedActions  // 建议行动（JSON）
)
```

业务语义：面向客户经理的快速参考卡片，一页纸呈现关键信息。

### 7.4 PostvisitAnalysisContent — 拜访后分析

```
PostvisitAnalysisContent(
  String       id,                   // 主键
  String       analysisId,           // 分析ID
  String       journeyId,            // 旅程ID
  String       operatingCaseId,      // 案例ID（FK → OperatingCase）
  String       visitSummary,         // 拜访摘要
  String       keyFindingsJson,      // 关键发现（JSON）
  String       opportunitySignalsJson,// 机会信号（JSON）
  String       commitmentsJson,      // 承诺（JSON）
  String       reconciliationItemsJson,// 核查项（JSON）
  String       nextStepsJson         // 下一步（JSON）
)
```

业务语义：拜访后AI自动生成的分析报告，提取关键发现、机会信号和承诺。

### 7.5 InteractionExtraction — 交互提取

```
InteractionExtraction(
  String       extractionId,       // 主键
  String       interactionId,      // 交互ID（FK → Interaction）
  String       extractionType,     // 提取类型
  String       rawContent,         // 原始内容
  String       extractedFieldsJson,// 提取字段（JSON）
  BigDecimal   confidence,         // 置信度
  LocalDateTime extractedAt        // 提取时间
)
```

业务语义：从交互内容中AI提取的结构化信息，confidence标记提取质量。

### 7.6 OutreachScript — 外联话术

```
OutreachScript(
  String       id,                  // 主键
  String       journeyId,           // 旅程ID
  String       operatingCaseId,     // 案例ID（FK → OperatingCase）
  String       scriptType,          // 话术类型
  String       objective,           // 目标
  String       openingLine,         // 开场白
  String       keyTalkingPoints,    // 关键话术点（JSON）
  String       closingLine,         // 结束语
  String       riskConsiderations,  // 风险考量（JSON）
  LocalDateTime generatedAt         // 生成时间
)
```

业务语义：AI生成的客户外联话术，受ProductKnowledgeCard约束。

### 7.7 MeetingScript — 会面话术

```
MeetingScript(
  String       id,                  // 主键
  String       journeyId,           // 旅程ID
  String       operatingCaseId,     // 案例ID（FK → OperatingCase）
  String       meetingType,         // 会面类型
  String       objective,           // 目标
  String       agendaItems,         // 议程项（JSON）
  String       keyQuestions,        // 关键问题（JSON）
  String       talkingPoints,       // 话术要点（JSON）
  String       riskConsiderations,  // 风险考量（JSON）
  LocalDateTime generatedAt         // 生成时间
)
```

业务语义：AI生成的会面话术，包含议程、关键问题和风险考量。

### 7.8 CrmWritebackCommand — CRM回写命令

```
CrmWritebackCommand(
  String       commandId,       // 主键
  String       customerId,      // 客户ID（FK → Customer）
  String       operatingCaseId, // 案例ID
  String       journeyId,       // 旅程ID
  String       actionType,      // 操作类型
  String       payloadJson,     // 操作载荷（JSON）
  String       idempotencyKey,  // 幂等键
  LocalDateTime requestedAt     // 请求时间
)
```

业务语义：向CRM系统回写数据的命令，通过CrmWritebackChannel执行。

### 7.9 LlmClient 端口 (`engagement.port`)

```java
String complete(String systemPrompt, String userPrompt);
```

业务语义：LLM调用抽象端口，支持fallback模式。

实现：`MockLlmClient`（结构化JSON）/ `RealLlmClient`（外部API），通过 `engagement.llm.mode` 配置切换。调用失败时fallback到原有模板/正则逻辑。

---

## 8. 数据库Schema

> H2 Flyway 迁移：V001–V014（V010 为索引优化，V014 为种子数据，此处略）

---

### V001 — 核心运营本体表

#### operating_case — 运营案例

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| case_id | CHAR(36) | PK | 案例唯一标识 |
| case_type | VARCHAR(64) | NOT NULL | 案例类型（CONTINUOUS_ENGAGEMENT / CLAIM_RECONCILIATION） |
| status | VARCHAR(32) | NOT NULL | 案例状态 |
| purpose | VARCHAR(512) | NOT NULL | 案例目的 |
| valid_from | TIMESTAMP(6) | NOT NULL | 生效时间 |
| valid_to | TIMESTAMP(6) | NULL | 失效时间，须 ≥ valid_from |
| recorded_at | TIMESTAMP(6) | NOT NULL | 记录时间 |
| record_version | BIGINT | NOT NULL DEFAULT 0 | 记录版本号 |
| created_by | VARCHAR(128) | NOT NULL | 创建人 |

约束：`ck_case_valid_time CHECK (valid_to IS NULL OR valid_to >= valid_from)`

#### interaction — 交互事件

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| interaction_id | CHAR(36) | PK | 交互唯一标识 |
| case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| occurred_at | TIMESTAMP(6) | NOT NULL | 发生时间 |
| channel | VARCHAR(64) | NOT NULL | 交互渠道 |
| source_uri | VARCHAR(1024) | NOT NULL | 来源 URI |
| source_version | VARCHAR(128) | NOT NULL | 来源版本 |
| source_hash | CHAR(64) | NOT NULL | 来源哈希 |
| recorded_at | TIMESTAMP(6) | NOT NULL | 记录时间 |

> 注：V002 重建此表为完整 14 列模式（见下方 V002）

#### claim — 声明

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| claim_id | CHAR(36) | PK | 声明唯一标识 |
| case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| interaction_id | CHAR(36) | FK → interaction, NULL | 来源交互 |
| claim_type | VARCHAR(64) | NOT NULL | 声明类型 |
| claim_status | VARCHAR(32) | NOT NULL | 声明状态（CANDIDATE / VERIFIED_FACT / CONFLICTING / INSUFFICIENT） |
| statement_text | TEXT | NOT NULL | 声明文本 |
| valid_from | TIMESTAMP(6) | NULL | 生效时间 |
| valid_to | TIMESTAMP(6) | NULL | 失效时间 |
| recorded_at | TIMESTAMP(6) | NOT NULL | 记录时间 |
| supersedes_claim_id | CHAR(36) | FK → claim, NULL | 被替代的声明 ID |
| model_run_id | CHAR(36) | NULL | 产出此声明的模型运行 ID |

约束：`ck_claim_valid_time CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)`

#### evidence — 证据

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| evidence_id | CHAR(36) | PK | 证据唯一标识 |
| source_uri | VARCHAR(1024) | NOT NULL | 来源 URI |
| source_version | VARCHAR(128) | NOT NULL | 来源版本 |
| locator | VARCHAR(1024) | NOT NULL | 定位符 |
| content_hash | CHAR(64) | NOT NULL | 内容哈希（保证完整性） |
| permission_label | VARCHAR(64) | NOT NULL | 权限标签 |
| license_ref | VARCHAR(256) | NULL | 许可证引用 |
| recorded_at | TIMESTAMP(6) | NOT NULL | 记录时间 |

#### claim_evidence — 声明-证据关联（M:N）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| claim_id | CHAR(36) | FK → claim, PK 组成 | 声明 ID |
| evidence_id | CHAR(36) | FK → evidence, PK 组成 | 证据 ID |
| relation_type | VARCHAR(16) | PK 组成 | 关系类型（SUPPORTS / REFUTES / QUALIFIES / EXPLAINS） |

#### human_confirmation — 人工确认

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| confirmation_id | CHAR(36) | PK | 确认唯一标识 |
| subject_type | VARCHAR(32) | NOT NULL | 确认对象类型 |
| subject_id | CHAR(36) | NOT NULL | 确认对象 ID |
| decision | VARCHAR(32) | NOT NULL | 决策（APPROVED / REJECTED / DEFERRED） |
| actor_id | VARCHAR(128) | NOT NULL | 操作人 ID |
| actor_role | VARCHAR(128) | NOT NULL | 操作人角色 |
| permission_decision_id | VARCHAR(128) | NOT NULL | 权限决策 ID |
| confirmed_at | TIMESTAMP(6) | NOT NULL | 确认时间 |
| comment_text | VARCHAR(2000) | NULL | 备注说明 |

#### controlled_action — 受控操作

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| action_id | CHAR(36) | PK | 操作唯一标识 |
| proposal_id | CHAR(36) | NOT NULL | 提案 ID |
| confirmation_id | CHAR(36) | FK → human_confirmation, NOT NULL | 人工确认 ID |
| target_system | VARCHAR(128) | NOT NULL | 目标系统 |
| target_object_type | VARCHAR(128) | NOT NULL | 目标对象类型 |
| target_object_id | VARCHAR(256) | NOT NULL | 目标对象 ID |
| expected_version | VARCHAR(128) | NOT NULL | 期望版本 |
| operation | VARCHAR(64) | NOT NULL | 操作类型 |
| payload_json | JSON | NOT NULL | 操作载荷 |
| idempotency_key | VARCHAR(128) | NOT NULL | 幂等键 |
| status | VARCHAR(32) | NOT NULL | 操作状态 |
| requested_at | TIMESTAMP(6) | NOT NULL | 请求时间 |

唯一约束：`uk_action_idempotency (target_system, idempotency_key)`

#### action_receipt — 操作回执

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| receipt_id | CHAR(36) | PK | 回执唯一标识 |
| action_id | CHAR(36) | FK → controlled_action, NOT NULL | 关联操作 ID |
| status | VARCHAR(32) | NOT NULL | 回执状态 |
| target_version_after | VARCHAR(128) | NULL | 操作后目标版本 |
| failure_code | VARCHAR(128) | NULL | 失败码 |
| raw_receipt_hash | CHAR(64) | NULL | 原始回执哈希 |
| received_at | TIMESTAMP(6) | NOT NULL | 接收时间 |

#### evaluation_run — 评估运行

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| evaluation_id | CHAR(36) | PK | 评估唯一标识 |
| run_manifest_id | CHAR(36) | NOT NULL | 运行清单 ID |
| case_set_version | VARCHAR(128) | NOT NULL | 案例集版本 |
| gate_state | VARCHAR(64) | NOT NULL | 门控状态 |
| metrics_json | JSON | NOT NULL | 评估指标 |
| evaluated_at | TIMESTAMP(6) | NOT NULL | 评估时间 |

#### outbox_event — 领域事件发件箱

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| event_id | CHAR(36) | PK | 事件唯一标识 |
| aggregate_type | VARCHAR(128) | NOT NULL | 聚合类型 |
| aggregate_id | VARCHAR(256) | NOT NULL | 聚合 ID |
| event_type | VARCHAR(256) | NOT NULL | 事件类型 |
| event_version | VARCHAR(32) | NOT NULL | 事件版本 |
| payload_json | JSON | NOT NULL | 事件载荷 |
| occurred_at | TIMESTAMP(6) | NOT NULL | 发生时间 |
| published_at | TIMESTAMP(6) | NULL | 发布时间 |
| publish_attempts | INT | NOT NULL DEFAULT 0 | 发布尝试次数 |
| last_error_code | VARCHAR(128) | NULL | 最近错误码 |

### V002 — 交互增强 + 客户旅程场景

> 重建 interaction 表为完整 14 列模式，新增客户旅程场景表

#### interaction（重建） — 交互事件

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| interaction_id | CHAR(36) | PK | 交互唯一标识 |
| case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| journey_id | CHAR(36) | NULL | 所属旅程 |
| interaction_type | VARCHAR(64) | NOT NULL | 交互类型 |
| direction | VARCHAR(16) | NOT NULL | 方向（INBOUND / OUTBOUND） |
| channel | VARCHAR(64) | NOT NULL | 交互渠道 |
| content_summary | VARCHAR(2000) | NULL | 内容摘要 |
| outcome | VARCHAR(32) | NOT NULL | 交互结果 |
| initiator_id | VARCHAR(128) | NOT NULL | 发起人 ID |
| initiator_role | VARCHAR(64) | NOT NULL | 发起人角色 |
| initiator_display_name | VARCHAR(256) | NOT NULL | 发起人显示名 |
| produced_claim_ids | VARCHAR(4000) | NULL | 产出的声明 ID 列表 |
| occurred_at | TIMESTAMP(6) | NOT NULL | 发生时间 |
| ended_at | TIMESTAMP(6) | NULL | 结束时间 |
| source_uri | VARCHAR(1024) | NOT NULL | 来源 URI |
| source_version | VARCHAR(128) | NOT NULL | 来源版本 |
| source_hash | CHAR(64) | NOT NULL | 来源哈希 |
| recorded_at | TIMESTAMP(6) | NOT NULL | 记录时间 |

#### interaction_participant — 交互参与者（M:N）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| interaction_id | CHAR(36) | FK → interaction, PK 组成 | 交互 ID |
| participant_id | VARCHAR(128) | PK 组成 | 参与者 ID |
| participant_role | VARCHAR(64) | NOT NULL | 角色（AI_AGENT / RELATIONSHIP_MANAGER / COMPLIANCE_OFFICER / PRODUCT_SPECIALIST / CUSTOMER / SYSTEM） |
| display_name | VARCHAR(256) | NOT NULL | 显示名称 |

#### customer_journey — 客户旅程

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| journey_id | CHAR(36) | PK | 旅程唯一标识 |
| case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| customer_id | VARCHAR(128) | NOT NULL | 客户 ID |
| customer_name | VARCHAR(256) | NOT NULL | 客户名称 |
| phase | VARCHAR(32) | NOT NULL | 旅程阶段 |
| started_at | TIMESTAMP(6) | NOT NULL | 开始时间 |
| updated_at | TIMESTAMP(6) | NULL | 更新时间 |

#### insight_claim — 洞察声明

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| insight_id | CHAR(36) | PK | 洞察唯一标识 |
| claim_id | CHAR(36) | FK → claim, NOT NULL | 关联声明 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| insight_category | VARCHAR(64) | NOT NULL | 洞察类别 |
| insight_summary | VARCHAR(2000) | NOT NULL | 洞察摘要 |
| generated_at | TIMESTAMP(6) | NOT NULL | 生成时间 |

#### product_candidate_claim — 产品候选声明

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| product_id | CHAR(36) | PK | 产品候选唯一标识 |
| claim_id | CHAR(36) | FK → claim, NOT NULL | 关联声明 |
| insight_claim_id | CHAR(36) | FK → insight_claim, NOT NULL | 关联洞察 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| product_code | VARCHAR(64) | NOT NULL | 产品代码 |
| product_name | VARCHAR(256) | NOT NULL | 产品名称 |
| match_reason | VARCHAR(2000) | NOT NULL | 匹配原因 |
| proposed_at | TIMESTAMP(6) | NOT NULL | 提议时间 |

#### previsit_report — 拜访前报告

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| report_id | CHAR(36) | PK | 报告唯一标识 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| journey_id | CHAR(36) | FK → customer_journey, NOT NULL | 所属旅程 |
| insight_ids | VARCHAR(4000) | NOT NULL | 关联洞察 ID 列表 |
| product_candidate_ids | VARCHAR(4000) | NOT NULL | 关联产品候选 ID 列表 |
| summary | VARCHAR(2000) | NOT NULL | 报告摘要 |
| generated_at | TIMESTAMP(6) | NOT NULL | 生成时间 |

#### postvisit_analysis — 拜访后分析

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| analysis_id | CHAR(36) | PK | 分析唯一标识 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| journey_id | CHAR(36) | NOT NULL | 所属旅程 |
| previsit_report_id | CHAR(36) | FK → previsit_report, NOT NULL | 关联拜访前报告 |
| outcome | VARCHAR(2000) | NOT NULL | 分析结果 |
| follow_up_action | VARCHAR(2000) | NULL | 后续行动 |
| analyzed_at | TIMESTAMP(6) | NOT NULL | 分析时间 |

### V003 — 客户上下文

#### customer — 客户主档

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| customer_id | VARCHAR(36) | PK | 客户唯一标识（CRM 权威源） |
| customer_name | VARCHAR(200) | NOT NULL | 客户全称 |
| customer_short_name | VARCHAR(100) | NULL | 客户简称 |
| unified_social_credit_code | VARCHAR(50) | NULL | 统一社会信用代码 |
| established_date | DATE | NULL | 成立日期 |
| registered_capital_cny | BIGINT | NULL | 注册资本（人民币） |
| industry | VARCHAR(200) | NULL | 行业（MANUFACTURING / FINANCE / TECHNOLOGY / REAL_ESTATE / ENERGY / HEALTHCARE / AGRICULTURE / LOGISTICS / RETAIL / OTHER） |
| region | VARCHAR(100) | NULL | 地区 |
| enterprise_scale | VARCHAR(100) | NULL | 企业规模（LARGE / MEDIUM / SMALL / MICRO） |
| customer_tier | VARCHAR(50) | NULL | 客户层级（STRATEGIC / KEY / GROWTH / GENERAL） |
| relationship_since | DATE | NULL | 建立关系日期 |
| rm_id | VARCHAR(36) | NOT NULL | 客户经理 ID |
| rm_name | VARCHAR(100) | NULL | 客户经理姓名 |
| managing_branch | VARCHAR(200) | NULL | 管辖分支 |
| group_flag | BOOLEAN | DEFAULT FALSE | 是否集团客户 |
| listed_status | VARCHAR(50) | NULL | 上市状态（LISTED / UNLISTED / DELISTED） |
| risk_level | VARCHAR(50) | NULL | 风险等级（HIGH / MEDIUM / LOW） |
| main_products | CLOB | NULL | 主要产品（JSON） |
| core_tags | CLOB | NULL | 核心标签（JSON） |
| relationship_summary | CLOB | NULL | 关系摘要 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NULL | 更新时间 |

#### legal_entity — 法人实体

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| entity_id | VARCHAR(36) | PK | 法人实体唯一标识 |
| group_id | VARCHAR(36) | NOT NULL | 所属集团 ID |
| name | VARCHAR(200) | NOT NULL | 法人名称 |
| role | VARCHAR(200) | NULL | 角色（母公司 / 子公司 / 关联方） |
| ownership | VARCHAR(100) | NULL | 持股比例 |
| bank_customer_id | VARCHAR(36) | NULL | 对应银行客户 ID |
| relationship_status | VARCHAR(100) | NULL | 关系状态 |
| evidence_ref | VARCHAR(100) | NULL | 证据引用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### group_relationship — 集团关系

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | VARCHAR(36) | PK | 关系唯一标识 |
| group_id | VARCHAR(36) | NOT NULL | 集团 ID |
| from_entity_id | VARCHAR(36) | NOT NULL | 出资方实体 ID |
| to_entity_id | VARCHAR(36) | NOT NULL | 被投资方实体 ID |
| relationship_type | VARCHAR(50) | NOT NULL | 关系类型（控股 / 参股 / 关联交易） |
| ownership_ratio | INTEGER | NULL | 持股比例（%） |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### bank_relationship_snapshot — 银行关系快照

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | VARCHAR(36) | PK | 快照唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| snapshot_month | VARCHAR(7) | NOT NULL | 快照月份（yyyy-MM） |
| avg_daily_deposit_cny | BIGINT | NULL | 日均存款（人民币） |
| monthly_settlement_cny | BIGINT | NULL | 月结算量（人民币） |
| loan_balance_cny | BIGINT | NULL | 贷款余额（人民币） |
| credit_total_cny | BIGINT | NULL | 授信总额（人民币） |
| used_credit_cny | BIGINT | NULL | 已用授信（人民币） |
| available_credit_cny | BIGINT | NULL | 可用授信（人民币） |
| bank_acceptance_bill_balance_cny | BIGINT | NULL | 银承余额（人民币） |
| guarantee_balance_cny | BIGINT | NULL | 担保余额（人民币） |
| payroll_employees | INTEGER | NULL | 代发薪人数 |
| cash_management_opened | BOOLEAN | DEFAULT FALSE | 是否开通现金管理 |
| supply_chain_finance_opened | BOOLEAN | DEFAULT FALSE | 是否开通供应链金融 |
| cross_border_settlement_cny | BIGINT | NULL | 跨境结算量（人民币） |
| product_count | INTEGER | NULL | 产品数量 |
| customer_contribution_level | VARCHAR(10) | NULL | 客户贡献等级 |
| anomaly_flags | VARCHAR(200) | NULL | 异常标记 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### credit_facility — 授信额度

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| facility_id | VARCHAR(36) | PK | 授信唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| borrower_entity | VARCHAR(200) | NOT NULL | 借款主体 |
| approval_date | DATE | NULL | 审批日期 |
| maturity_date | DATE | NULL | 到期日期 |
| credit_total_cny | BIGINT | NULL | 授信总额（人民币） |
| used_credit_cny | BIGINT | NULL | 已用授信（人民币） |
| available_credit_cny | BIGINT | NULL | 可用授信（人民币） |
| current_loan_balance_cny | BIGINT | NULL | 当前贷款余额（人民币） |
| bank_acceptance_bill_balance_cny | BIGINT | NULL | 银承余额（人民币） |
| guarantee_balance_cny | BIGINT | NULL | 担保余额（人民币） |
| collateral | CLOB | NULL | 抵押物 |
| purpose_allowed | CLOB | NULL | 允许用途 |
| purpose_restrictions | CLOB | NULL | 用途限制 |
| covenants | CLOB | NULL | 契约条款 |
| reconciliation_note | CLOB | NULL | 对账说明 |
| evidence_ref | VARCHAR(100) | NULL | 证据引用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NULL | 更新时间 |

#### transaction_ledger — 交易流水

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | VARCHAR(36) | PK | 记录唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| transaction_date | DATE | NOT NULL | 交易日期 |
| transaction_type | VARCHAR(50) | NOT NULL | 交易类型 |
| counterparty | VARCHAR(200) | NULL | 交易对手 |
| amount_cny | BIGINT | NOT NULL | 金额（人民币） |
| description | CLOB | NULL | 描述 |
| evidence_ref | VARCHAR(100) | NULL | 证据引用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### V004 — 知识规则

#### product_catalog — 产品目录

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| product_id | VARCHAR(36) | PK | 产品唯一标识 |
| name | VARCHAR(200) | NOT NULL | 产品名称 |
| definition | CLOB | NULL | 产品定义 |
| key_conditions | CLOB | NULL | 关键条件 |
| required_materials | CLOB | NULL | 所需材料 |
| risk_points | CLOB | NULL | 风险要点 |
| trigger | VARCHAR(500) | NULL | 触发条件 |
| prohibited_phrases | CLOB | NULL | 禁止用语 |
| evidence_source | VARCHAR(100) | NULL | 证据来源 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### policy_rule — 政策规则

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| rule_id | VARCHAR(36) | PK | 规则唯一标识 |
| name | VARCHAR(200) | NOT NULL | 规则名称 |
| severity | VARCHAR(20) | NOT NULL | 严重程度（CRITICAL / HIGH / MEDIUM） |
| logic | CLOB | NOT NULL | 规则逻辑 |
| required_output | CLOB | NOT NULL | 要求输出 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### external_event — 外部事件

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| event_id | VARCHAR(36) | PK | 事件唯一标识 |
| event_date | DATE | NOT NULL | 事件日期 |
| source_type | VARCHAR(100) | NULL | 来源类型 |
| source_name | VARCHAR(200) | NULL | 来源名称 |
| entity | VARCHAR(200) | NULL | 涉及实体 |
| title | VARCHAR(500) | NOT NULL | 事件标题 |
| content | CLOB | NULL | 事件内容 |
| confidence | VARCHAR(20) | NULL | 置信度（HIGH / MEDIUM / LOW） |
| reliability | VARCHAR(20) | NULL | 可靠性（VERIFIED / UNVERIFIED / DISPUTED） |
| bank_use_allowed | BOOLEAN | DEFAULT TRUE | 是否允许银行使用 |
| linked_themes | CLOB | NULL | 关联主题 |
| possible_business_signal | VARCHAR(500) | NULL | 可能的业务信号 |
| no_go_statement | CLOB | NULL | 禁止声明 |
| evidence_ref | VARCHAR(100) | NULL | 证据引用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

> V011 扩展列：severity, affected_industries(JSON), affected_customer_ids(JSON), detected_at, raw_payload(TEXT)

### V005 — 经营增强

#### kyc_gap_profile — KYC 缺口画像

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| profile_id | VARCHAR(36) | PK | 画像唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| as_of | DATE | NOT NULL | 评估基准日 |
| known_items | CLOB | NULL | 已知项 |
| partial_known_items | CLOB | NULL | 部分已知项 |
| stale_items | CLOB | NULL | 过期项 |
| conflicting_or_ambiguous_items | CLOB | NULL | 冲突/模糊项 |
| unknown_items | CLOB | NULL | 未知项 |
| priority_questions | CLOB | NULL | 优先问题 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NULL | 更新时间 |

> V011 扩展列：overall_completeness, risk_impact, last_assessed_by, last_assessed_at

#### fact_reconciliation_case — 事实核查案例

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| reconciliation_id | VARCHAR(36) | PK | 核查唯一标识 |
| case_id | VARCHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| topic | VARCHAR(200) | NOT NULL | 核查主题 |
| structured_fact | CLOB | NULL | 结构化事实 |
| interaction_claim | CLOB | NULL | 交互声明 |
| external_fact | CLOB | NULL | 外部事实 |
| ontology_distinction | CLOB | NULL | 本体区分说明 |
| correct_judgment | VARCHAR(500) | NULL | 正确判断 |
| wrong_output_examples | CLOB | NULL | 错误输出示例 |
| next_action | VARCHAR(200) | NULL | 下一步行动 |
| status | VARCHAR(32) | NOT NULL | 核查状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### opportunity_signal — 机会信号

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| signal_id | CHAR(36) | PK | 信号唯一标识 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| journey_id | CHAR(36) | NULL | 所属旅程 |
| signal_type | VARCHAR(64) | NOT NULL | 信号类型 |
| content | VARCHAR(2000) | NOT NULL | 信号内容 |
| source_type | VARCHAR(64) | NOT NULL | 来源类型 |
| source_ref | VARCHAR(512) | NOT NULL | 来源引用 |
| confidence | VARCHAR(16) | NOT NULL | 置信度（HIGH / MEDIUM / LOW） |
| status | VARCHAR(32) | NOT NULL | 信号状态（DETECTED / CONFIRMED / DISMISSED） |
| evidence_ref | VARCHAR(512) | NULL | 证据引用 |
| detected_at | TIMESTAMP(6) | NOT NULL | 检测时间 |
| confirmed_at | TIMESTAMP(6) | NULL | 确认时间 |

### V006 — 枚举检查约束

为以下列添加 CHECK 约束，确保枚举值域一致：

| 表 | 列 | 允许值 |
|----|-----|--------|
| operating_case | case_type | CONTINUOUS_ENGAGEMENT, CLAIM_RECONCILIATION |
| operating_case | status | DRAFT, ACTIVE, COMPLETED, ARCHIVED |
| claim | claim_type | CUSTOMER_FACT, MARKET_INTELLIGENCE, RISK_INDICATOR, COMPLIANCE_OBSERVATION |
| claim | claim_status | CANDIDATE, VERIFIED_FACT, CONFLICTING, INSUFFICIENT |
| interaction | channel | EMAIL, PHONE, IN_PERSON, WECHAT, VIDEO, SYSTEM |
| interaction | direction | INBOUND, OUTBOUND |

### V007 — 拜访后分析和拜访前报告

#### postvisit_analysis_content — 拜访后分析内容

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| analysis_id | CHAR(36) | NOT NULL | 分析 ID |
| journey_id | CHAR(36) | NULL | 所属旅程 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| visit_summary | CLOB | NULL | 拜访摘要 |
| key_findings_json | CLOB | NULL | 关键发现（JSON） |
| opportunity_signals_json | CLOB | NULL | 机会信号（JSON） |
| commitments_json | CLOB | NULL | 承诺事项（JSON） |
| reconciliation_items_json | CLOB | NULL | 对账项目（JSON） |
| next_steps_json | CLOB | NULL | 后续步骤（JSON） |

索引：`idx_pac_case (operating_case_id)`

#### previsit_report_content — 拜访前报告内容

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| analysis_id | CHAR(36) | NOT NULL | 分析 ID |
| journey_id | CHAR(36) | NULL | 所属旅程 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| visit_objective | VARCHAR(500) | NULL | 拜访目标 |
| content_json | CLOB | NULL | 报告内容（JSON） |

索引：`idx_prc_case (operating_case_id)`

### V008 — 交易

#### transaction — 交易记录

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| transaction_id | VARCHAR(64) | UK | 交易流水号 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| account_id | VARCHAR(64) | NULL | 账户 ID |
| transaction_type | VARCHAR(32) | NOT NULL | 交易类型（DEPOSIT / WITHDRAWAL / TRANSFER / PAYMENT / LOAN_DISBURSEMENT / LOAN_REPAYMENT） |
| amount | DECIMAL(18,2) | NOT NULL | 金额 |
| currency | VARCHAR(3) | NOT NULL DEFAULT 'CNY' | 币种 |
| counterparty | VARCHAR(200) | NULL | 交易对手 |
| counterparty_industry | VARCHAR(100) | NULL | 交易对手行业 |
| description | CLOB | NULL | 描述 |
| transaction_date | DATE | NOT NULL | 交易日期 |

索引：`idx_txn_customer (customer_id)`, `idx_txn_date (transaction_date)`

### V009 — 外联和会面话术

#### outreach_script — 外联话术

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| journey_id | CHAR(36) | NOT NULL | 所属旅程 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| script_type | VARCHAR(32) | NOT NULL | 话术类型（COLD_OUTREACH / WARM_INTRO / FOLLOW_UP） |
| objective | VARCHAR(500) | NOT NULL | 目标 |
| opening_line | VARCHAR(1000) | NULL | 开场白 |
| key_talking_points | CLOB | NULL | 关键谈话要点 |
| closing_line | VARCHAR(1000) | NULL | 结束语 |
| risk_considerations | CLOB | NULL | 风险考量 |
| generated_at | TIMESTAMP(6) | NOT NULL | 生成时间 |

索引：`idx_outreach_customer (customer_id)`

#### meeting_script — 会面话术

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| journey_id | CHAR(36) | NOT NULL | 所属旅程 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| meeting_type | VARCHAR(32) | NOT NULL | 会面类型（INITIAL / FOLLOW_UP / REVIEW / NEGOTIATION） |
| objective | VARCHAR(500) | NOT NULL | 目标 |
| agenda_items | CLOB | NULL | 议程项 |
| key_questions | CLOB | NULL | 关键问题 |
| talking_points | CLOB | NULL | 谈话要点 |
| risk_considerations | CLOB | NULL | 风险考量 |
| generated_at | TIMESTAMP(6) | NOT NULL | 生成时间 |

索引：`idx_meeting_customer (customer_id)`

### V011 — V1.1 运营本体扩展

> 扩展已有表列 + 新增实体

**扩展列清单：**

| 表 | 新增列 | 说明 |
|----|--------|------|
| external_event | severity VARCHAR(20) | 严重程度 |
| external_event | affected_industries CLOB | 受影响行业（JSON） |
| external_event | affected_customer_ids CLOB | 受影响客户 ID（JSON） |
| external_event | detected_at TIMESTAMP(6) | 检测时间 |
| external_event | raw_payload TEXT | 原始载荷 |
| kyc_gap_profile | overall_completeness DECIMAL(5,4) | 整体完整度 |
| kyc_gap_profile | risk_impact VARCHAR(20) | 风险影响 |
| kyc_gap_profile | last_assessed_by VARCHAR(128) | 最近评估人 |
| kyc_gap_profile | last_assessed_at TIMESTAMP(6) | 最近评估时间 |

#### opportunity — 机会

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| opportunity_id | CHAR(36) | PK | 机会唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| interaction_id | CHAR(36) | NULL | 来源交互 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| opportunity_type | VARCHAR(64) | NOT NULL | 机会类型 |
| product_id | VARCHAR(36) | NULL | 关联产品 ID |
| product_name | VARCHAR(256) | NULL | 产品名称 |
| description | VARCHAR(2000) | NOT NULL | 描述 |
| status | VARCHAR(32) | NOT NULL | 机会状态 |
| estimated_amount | BIGINT | NULL | 预估金额（人民币） |
| probability | VARCHAR(16) | NULL | 成功概率（HIGH / MEDIUM / LOW） |
| assigned_to | VARCHAR(128) | NULL | 负责人 |
| source | VARCHAR(64) | NOT NULL | 来源 |
| next_steps | CLOB | NULL | 后续步骤 |
| expected_close_date | DATE | NULL | 预计关闭日期 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP(6) | NULL | 更新时间 |

#### product_knowledge_version — 产品知识版本

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| version_id | CHAR(36) | PK | 版本唯一标识 |
| product_id | VARCHAR(36) | FK → product_catalog, NOT NULL | 关联产品 |
| version_number | INT | NOT NULL | 版本号 |
| product_name | VARCHAR(256) | NOT NULL | 产品名称 |
| category | VARCHAR(128) | NULL | 分类 |
| description | CLOB | NULL | 描述 |
| key_features | CLOB | NULL | 核心特性 |
| target_industries | CLOB | NULL | 目标行业 |
| risk_level | VARCHAR(20) | NULL | 风险等级 |
| required_materials | CLOB | NULL | 所需材料 |
| pricing_basis | VARCHAR(200) | NULL | 定价基准 |
| previous_version_id | CHAR(36) | FK → product_knowledge_version, NULL | 上一版本 |
| change_summary | VARCHAR(500) | NULL | 变更摘要 |
| changed_by | VARCHAR(128) | NOT NULL | 变更人 |
| changed_at | TIMESTAMP(6) | NOT NULL | 变更时间 |

### V012 — V1.1 人工操作实体

#### recording_consent — 录音同意

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| consent_id | CHAR(36) | PK | 同意唯一标识 |
| interaction_id | CHAR(36) | FK → interaction, NOT NULL | 关联交互 |
| consent_type | VARCHAR(64) | NOT NULL | 同意类型（RECORDING / SCREEN_CAPTURE / AI_PROCESSING） |
| consent_given | BOOLEAN | NOT NULL | 是否同意 |
| consent_method | VARCHAR(64) | NOT NULL | 同意方式（VERBAL / WRITTEN / ELECTRONIC） |
| consent_given_by | VARCHAR(128) | NOT NULL | 同意人 |
| consent_given_at | TIMESTAMP(6) | NOT NULL | 同意时间 |
| evidence_ref | VARCHAR(512) | NULL | 证据引用 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |

#### task — 任务

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| task_id | CHAR(36) | PK | 任务唯一标识 |
| case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| interaction_id | CHAR(36) | FK → interaction, NULL | 来源交互 |
| task_type | VARCHAR(64) | NOT NULL | 任务类型 |
| title | VARCHAR(500) | NOT NULL | 任务标题 |
| description | CLOB | NULL | 任务描述 |
| priority | VARCHAR(16) | NOT NULL | 优先级（URGENT / HIGH / MEDIUM / LOW） |
| status | VARCHAR(32) | NOT NULL | 任务状态（PENDING / IN_PROGRESS / COMPLETED / CANCELLED） |
| assigned_to | VARCHAR(128) | NULL | 负责人 |
| due_date | DATE | NULL | 截止日期 |
| completed_at | TIMESTAMP(6) | NULL | 完成时间 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP(6) | NULL | 更新时间 |

### V013 — V1.1 扩展实体

#### claim_lifecycle_event — 声明生命周期事件

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| event_id | CHAR(36) | PK | 事件唯一标识 |
| claim_id | CHAR(36) | FK → claim, NOT NULL | 关联声明 |
| from_status | VARCHAR(32) | NOT NULL | 原状态 |
| to_status | VARCHAR(32) | NOT NULL | 目标状态 |
| transition_reason | VARCHAR(500) | NOT NULL | 转换原因 |
| actor_id | VARCHAR(128) | NOT NULL | 操作人 ID |
| actor_role | VARCHAR(64) | NOT NULL | 操作人角色 |
| transitioned_at | TIMESTAMP(6) | NOT NULL | 转换时间 |

#### evidence_version_link — 证据版本链接

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| link_id | CHAR(36) | PK | 链接唯一标识 |
| evidence_id | CHAR(36) | FK → evidence, NOT NULL | 关联证据 |
| previous_version_id | CHAR(36) | FK → evidence, NULL | 上一版本 |
| next_version_id | CHAR(36) | FK → evidence, NULL | 下一版本 |
| version_number | INT | NOT NULL | 版本号 |
| change_type | VARCHAR(32) | NOT NULL | 变更类型（CREATED / UPDATED / CORRECTED / SUPERSEDED） |
| change_reason | VARCHAR(500) | NULL | 变更原因 |
| changed_by | VARCHAR(128) | NOT NULL | 变更人 |
| changed_at | TIMESTAMP(6) | NOT NULL | 变更时间 |

#### interaction_extension — 交互扩展

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| extension_id | CHAR(36) | PK | 扩展唯一标识 |
| interaction_id | CHAR(36) | FK → interaction, UK | 关联交互（1:1） |
| recording_consent_id | CHAR(36) | FK → recording_consent, NULL | 录音同意 |
| commitment_ids | VARCHAR(4000) | NULL | 关联承诺 ID 列表 |
| task_ids | VARCHAR(4000) | NULL | 关联任务 ID 列表 |
| opportunity_ids | VARCHAR(4000) | NULL | 关联机会 ID 列表 |
| kyc_gap_profile_id | VARCHAR(36) | NULL | 关联 KYC 缺口画像 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP(6) | NULL | 更新时间 |

#### relationship_report — 关系报告

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| report_id | CHAR(36) | PK | 报告唯一标识 |
| operating_case_id | CHAR(36) | FK → operating_case, NOT NULL | 所属案例 |
| journey_id | CHAR(36) | NULL | 所属旅程 |
| report_type | VARCHAR(64) | NOT NULL | 报告类型 |
| content | CLOB | NOT NULL | 报告内容 |
| based_on_evidence | VARCHAR(4000) | NULL | 基于证据 ID 列表 |
| based_on_reconciliations | VARCHAR(4000) | NULL | 基于核查 ID 列表 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |

#### transaction_record — 交易记录

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | CHAR(36) | PK | 记录唯一标识 |
| customer_id | VARCHAR(36) | NOT NULL | 客户 ID |
| transaction_date | DATE | NOT NULL | 交易日期 |
| transaction_type | VARCHAR(50) | NOT NULL | 交易类型 |
| counterparty | VARCHAR(200) | NULL | 交易对手 |
| amount_cny | BIGINT | NOT NULL | 金额（人民币） |
| description | CLOB | NULL | 描述 |
| evidence_ref | VARCHAR(100) | NULL | 证据引用 |
| created_at | TIMESTAMP(6) | NOT NULL | 创建时间 |

---

## 9. 实体关系图

```
Customer ─────────────────────────────────────────────────┐
  │ 1                                                     │
  ├─ KycGapProfile (1:1)                                  │
  ├─ CreditFacility (1:N)                                 │
  ├─ BankRelationshipSnapshot (1:N)                       │
  ├─ Commitment (1:N)                                     │
  ├─ Transaction (1:N)                                    │
  ├─ TransactionRecord (1:N)                              │
  ├─ Opportunity (1:N)                                    │
  └─ CrmWritebackCommand (1:N)                            │
                                                          │
OperatingCase ────────────────────────────────────────────┤
  │ 1                                                     │
  ├─ Interaction (1:N)                                    │
  │    ├─ Claim (1:N) ── Evidence (M:N via supportedBy)   │
  │    ├─ InteractionExtension (1:1)                      │
  │    ├─ InteractionExtraction (1:N)                     │
  │    └─ OpportunitySignal (via journeyId)               │
  ├─ OpportunitySignal (1:N)                              │
  ├─ Commitment (1:N)                                     │
  ├─ FactReconciliationCase (1:N)                         │
  ├─ RelationshipReport (1:N)                             │
  ├─ PrevisitReportContent (1:N)                          │
  ├─ PostvisitAnalysisContent (1:N)                       │
  ├─ OutreachScript (1:N)                                 │
  ├─ MeetingScript (1:N)                                  │
  └─ CrmWritebackCommand (1:N)                            │
                                                          │
HumanConfirmation ── ControlledAction (1:1) ── ActionReceipt (1:1)
                                                          │
Claim ── ClaimLifecycleEvent (1:N)                        │
Evidence ── EvidenceVersionLink (1:N)                     │
ProductKnowledgeCard ── ProductKnowledgeVersion (1:N)     │
GroupRelationship ── LegalEntity (via groupId)            │
PolicyRule (独立)                                         │
EvaluationResult (独立)                                   │
                                                          │
Customer ◄── OperatingCase (via customerId in context) ───┘
```

---

## 10. 业务规则与禁令

### 10.1 核心禁令 (AI_GUIDE.md)

| # | 禁令 | 含义 |
|---|------|------|
| 3 | 无确认的授信承诺禁止 | Commitment.status≠FULFILLED 未经 HumanConfirmation 不得执行 |
| 6 | Claim ≠ Fact | AI 产出只能为 CANDIDATE，经 DMN 裁决+人工确认才为 VERIFIED_FACT |
| 7 | Signal ≠ Opportunity | OpportunitySignal 须经确认才转化为 Opportunity |

### 10.2 声明裁决流程

```
AI产出 → Claim(CANDIDATE) → DMN裁决(conflictDetected/authoritativeMatch/evidenceComplete)
  → VERIFIED_FACT / CONFLICTING / INSUFFICIENT / CANDIDATE
  → 人工确认(HumanConfirmation) → 最终状态
```

### 10.3 受控操作流程

```
提案 → HumanConfirmation(decision=APPROVED) → ControlledAction → ActionReceipt
```

### 10.4 领域事件发布

| 服务                   | 触发点                    | 事件类型                    |
|------------------------|---------------------------|-----------------------------|
| EngagementOrchestrator | startJourney              | JOURNEY_STARTED             |
| EngagementOrchestrator | postvisit                 | POSTVISIT_COMPLETED         |
| EngagementOrchestrator | newEvidence               | EVIDENCE_RECEIVED           |
| KycInsightService      | claimCandidateRecorded    | CLAIM_CANDIDATE_RECORDED    |
| CrmWritebackService    | controlledActionRequested | CONTROLLED_ACTION_REQUESTED |

### 10.5 证据链完整性

- Evidence 通过 contentHash 保证内容完整性
- EvidenceVersionLink 追踪证据版本演进
- Claim 通过 supportedBy 关联 Evidence
- 所有 AI 产出必须关联 evidenceRef

### 10.6 客户数据权威源

- Customer 权威来源为 EDWCRM (ADR-0010)
- 通过 R2RML 映射将 CRM 数据转为 gits:Customer 三元组
- 本系统不修改客户主数据，仅引用
