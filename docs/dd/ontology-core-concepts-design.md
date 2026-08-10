# 运营本体核心概念设计文档

> 本文档以人类易于理解的方式，逐一阐述 GITS 知识工程系统中运营本体的每个核心概念。

---

## 1. 禁令体系 — 系统不可逾越的红线

| # | 标识 | 含义 | 执行机制 |
|---|------|------|---------|
| 1 | `no_direct_crm_writeback` | AI 不可直接写入 CRM | `CrmWritebackCommand.requiresHumanConfirm == true` |
| 2 | `no_auto_send` | 不可自动发送客户沟通 | `ControlledAction` 需 `HumanConfirmation` |
| 3 | `no_approval_commitment` | 不可承诺审批结果 | `Commitment.commitmentType` 不含 APPROVAL_GUARANTEE |
| 4 | `no_truth_score` | 不可给客户信息打"真实度评分" | 不存在 truthScore 字段 |
| 5 | `no_infer_full_cashflow` | 不可从部分数据推断完整现金流 | `KycGapProfile` 标记缺失维度 |
| 6 | `Claim ≠ Fact` | 客户说的 ≠ 事实 | `Claim.status` 经 DMN 对账才能变为 `VERIFIED_FACT` |
| 7 | `OpportunitySignal ≠ Opportunity` | 信号 ≠ 商机 | `OpportunitySignal` 经评估才产生 `Opportunity` |
| 8 | `Bankability ≠ Approval` | 可贷性 ≠ 审批通过 | `EvaluationResult` ≠ 授信审批 |

---

## 2. Claim — 主张/声明

> **本质：** "待验证的声明"——客户说的、系统推断的、外部传入的信息，未经事实对账前只是"主张"，不是"事实"。
> **禁令 #6：Claim ≠ Fact**

源码：`modules/operational-ontology/.../Claim.java`

| 字段 | 含义 |
|------|------|
| `claimId` | 唯一标识 |
| `caseId` | 所属经营案例 |
| `claimType` | 主张类型 |
| `status` | 当前状态 |
| `statement` | 主张内容描述 |
| `validFrom/validTo` | 有效期 |
| `recordedAt` | 记录时间 |
| `supersedesClaimId` | 取代的上一条 Claim（版本链） |

**ClaimType：** `CUSTOMER_JOURNEY`(客户旅程) | `OPPORTUNITY`(机会信号) | `PRODUCT_CANDIDATE`(产品候选) | `CUSTOMER_STATEMENT`(客户陈述) | `SYSTEM_FACT`(系统事实) | `RISK_SIGNAL`(风险信号) | `COMMITMENT`(承诺) | `FOLLOW_UP`(跟进)

**ClaimStatus 状态流转：**
```
CANDIDATE ──→ CONFLICT ──→ HUMAN_CONFIRMED ──→ VERIFIED_FACT
    │              └──→ REJECTED
    └──→ VERIFIED_FACT（DMN直接确认）
```
- `CANDIDATE` — 初始候选，尚未校验
- `CONFLICT` — 四维校验发现冲突，需人工审核
- `REJECTED` — 被拒绝
- `HUMAN_CONFIRMED` — 人工确认通过（中间态）
- `VERIFIED_FACT` — 经 DMN 对账确认为事实（唯一权威状态）

**事实对账流程：** Claim(CANDIDATE) → `ClaimReconciliationPort.reconcile(conflictDetected, authoritativeMatch, evidenceComplete)` → DMN 决策 → VERIFIED_FACT / CONFLICT / CANDIDATE

---

## 3. OperatingCase — 经营案例

> **本质：** "经营案件卷宗"——围绕一个客户的一段持续经营周期，所有 Claim/Interaction/Evidence 都归属于一个 Case。

源码：`modules/operational-ontology/.../OperatingCase.java`

| 字段 | 含义 |
|------|------|
| `caseId` | 案例唯一标识 |
| `customerId` | 关联客户 |
| `caseType` | 案例类型 |
| `status` | 当前状态 |
| `title/description` | 标题/描述 |
| `rmId/branchId` | 负责RM/所属支行 |
| `createdAt/updatedAt/closedAt` | 时间戳 |

**CaseType：** `KYC_REVIEW` | `CREDIT_ORIGINATION` | `CROSS_SELL` | `RETENTION` | `COMPLIANCE_REVIEW`

**CaseStatus：** `OPEN` → `IN_PROGRESS` → `PENDING_REVIEW` → `CLOSED` → `ARCHIVED`

---

## 4. Customer — 客户

> **本质：** "银行服务的对公客户"——拥有行业、规模、上市状态等属性，是所有经营活动的中心。

源码：`modules/operational-ontology/.../Customer.java`

| 字段 | 含义 |
|------|------|
| `customerId/customerName` | 标识/名称 |
| `industry` | 所属行业 |
| `tier` | 客户层级 |
| `enterpriseScale` | 企业规模 |
| `listedStatus` | 上市状态 |
| `rmId/branchId/region` | RM/支行/区域 |
| `registeredCapital/mainBusiness` | 注册资本/主营业务 |

**Industry：** `MANUFACTURING` | `TRADE` | `TECHNOLOGY` | `REAL_ESTATE` | `FINANCE` | `HEALTHCARE` | `ENERGY` | `INFRASTRUCTURE` | `OTHER`

**CustomerTier：** `STRATEGIC`(战略) | `KEY`(重点) | `GENERAL`(一般) | `WATCH`(观察)

**EnterpriseScale：** `LARGE` | `MEDIUM` | `SMALL` | `MICRO`

**ListedStatus：** `LISTED_A` | `LISTED_HK` | `LISTED_US` | `UNLISTED` | `PRE_IPO`

---

## 5. Interaction — 交互/触达

> **本质：** "一次交互事件的完整记录"——客户经理与客户之间的每次接触都是一个 Interaction，它是连接 M17(开户)→M18(洞察)→M20(产品匹配)→M21(访前)→M22(访后) 的纽带。
>
> **设计原则：**
> - 交互可由人发起（客户经理拜访），也可由系统发起（AI信号推送）
> - 交互产出可追溯：每次交互可能产生新的 Claim 或触发状态转换
> - 交互有方向：出站（主动接触客户）和入站（客户主动联系）
> - 交互有上下文：关联到具体的 OperatingCase 和 CustomerJourney

源码：`modules/operational-ontology/.../Interaction.java`

| 字段 | 含义 |
|------|------|
| `interactionId` | 交互唯一标识 |
| `caseId` | 所属经营案例 |
| `journeyId` | 所属客户旅程 |
| `type` | 交互类型（InteractionType） |
| `direction` | 交互方向（Direction） |
| `channel` | 交互渠道（Channel） |
| `initiator` | 发起人（Participant） |
| `participants` | 参与者列表（List\<Participant\>） |
| `contentSummary` | 内容摘要 |
| `producedClaimIds` | 产出的 Claim 列表 |
| `outcome` | 交互结果（InteractionOutcome） |
| `occurredAt/endedAt` | 起止时间 |
| `sourceHash` | 来源哈希（防重复） |

**InteractionType：** `SIGNAL_TRIGGER`(信号触发) | `AI_INSIGHT_PUSH`(AI洞察推送) | `PHONE_CALL`(电话) | `FACE_TO_FACE_VISIT`(面访) | `VIDEO_CONFERENCE`(视频) | `INSTANT_MESSAGE`(即时消息) | `EMAIL`(邮件) | `PRODUCT_PRESENTATION`(产品推介) | `CUSTOMER_COMPLAINT`(投诉) | `FOLLOW_UP`(回访跟进)

**Direction：** `OUTBOUND`(出站-主动触达) | `INBOUND`(入站-客户主动)

**Participant：** `participantId` + `role` + `displayName`
- **Role：** `RELATIONSHIP_MANAGER`(客户经理) | `CUSTOMER`(客户) | `AI_AGENT`(AI智能体) | `COMPLIANCE_OFFICER`(合规审核员) | `PRODUCT_SPECIALIST`(产品专家)

**InteractionOutcome：** `COMPLETED`(完成) | `CUSTOMER_AGREED`(客户同意) | `CUSTOMER_DECLINED`(客户拒绝) | `CUSTOMER_DEFERRED`(客户需考虑) | `FOLLOW_UP_REQUIRED`(需跟进) | `INTERRUPTED`(中断) | `INFORMATION_GATHERED`(仅收集信息)

**Channel：** `IN_PERSON` | `PHONE` | `VIDEO` | `EMAIL` | `WECHAT` | `OTHER`

### 概念澄清：Interaction 的粒度

> **核心规则：Interaction 的粒度是"一次接触事件"，不是"一次对话"或"一个对话人"。**

**典型场景：** 客户经理拜访"华东精工"，一次拜访中分别见了3个关键人（CEO/CFO/采购总监）聊了不同内容——这算 **1 次 Interaction，1 条记录**。

**理由：**
1. `participants` 是 `List<Participant>` — 一个 Interaction 可包含多个参与者，3个关键人都是 `Participant(role=CUSTOMER)`，挂在同一条记录的 `participants` 列表中
2. `occurredAt/endedAt` 只有一组时间 — 对应一次拜访的起止时间，而非每人各一组
3. `contentSummary` 是单个字符串 — 整体摘要，不是按人分段
4. `producedClaimIds` 是 `List<UUID>` — 这次拜访中与3人聊出的所有 Claim 都关联到同一个 Interaction

**建模示例：**
```
Interaction {
  interactionId:  uuid-001,
  caseId:         华东精工的OperatingCase,
  type:           FACE_TO_FACE_VISIT,
  direction:      OUTBOUND,
  channel:        IN_PERSON,
  initiator:      Participant("P-RM-001", RELATIONSHIP_MANAGER, "张伟"),
  participants: [
    Participant("KEY-001", CUSTOMER, "王总(CEO)"),
    Participant("KEY-002", CUSTOMER, "李总(CFO)"),
    Participant("KEY-003", CUSTOMER, "赵总(采购总监)")
  ],
  contentSummary: "拜访华东精工，与王总讨论战略规划，与李总确认融资需求，与赵总了解设备采购节奏",
  producedClaimIds: [claim-1, claim-2, claim-3],
  outcome:        FOLLOW_UP_REQUIRED,
  occurredAt:     2026-07-08T10:00:00Z,
  endedAt:        2026-07-08T11:30:00Z
}
```

**粒度判断速查：**

| 场景 | Interaction 数量 |
|---|---|
| 一次拜访见3个关键人 | **1** |
| 同一天上午拜访 + 下午电话跟进 | **2**（不同 type/channel/时间） |
| 拜访后第二天发邮件补充材料 | **1**（新的 Interaction，type=EMAIL） |
| 系统自动推送 AI 洞察给客户经理 | **1**（type=AI_INSIGHT_PUSH，initiator=AI_AGENT） |

**关键方法：**
- `involvesHuman()` — 判断是否涉及人工参与（决定是否需要 HumanConfirmation）
- `isAiInitiated()` — 判断是否由 AI 发起（AI 发起的交互产出只能是 CANDIDATE Claim，不能直接作为 VERIFIED_FACT）

---

## 6. Evidence — 证据

> **本质：** "信息来源的凭证"——每条 Claim 都需要 Evidence 支撑，证据的完整性和权威性决定 Claim 能否升级为 Fact。
> **禁令 #5：no_infer_full_cashflow**

源码：`modules/operational-ontology/.../Evidence.java`

| 字段 | 含义 |
|------|------|
| `evidenceId` | 证据唯一标识 |
| `caseId/claimId` | 所属案例/关联Claim |
| `sourceType/sourceReference` | 来源类型/引用 |
| `content` | 证据内容 |
| `confidence` | 置信度（0.0-1.0） |
| `collectedAt/collectedBy` | 采集时间/人 |
| `verifiedAt/verifiedBy` | 验证时间/人 |

版本链通过 `EvidenceVersionLink` 实现，支持 UPDATE/CORRECT/AMEND。

---

## 7. Commitment — 承诺

> **本质：** "客户经理对客户做出的承诺"——如"下周出方案"，但**绝不能承诺审批结果**。
> **禁令 #3：no_approval_commitment**

源码：`modules/operational-ontology/.../Commitment.java`

| 字段 | 含义 |
|------|------|
| `commitmentId` | 承诺唯一标识 |
| `caseId/interactionId/customerId` | 关联案例/交互/客户 |
| `commitmentType` | 承诺类型 |
| `description` | 承诺描述 |
| `committedBy/committedAt` | 承诺人/时间 |
| `dueDate` | 截止日期 |
| `status/fulfilledAt` | 状态/履约时间 |

合规类型：`DELIVERY`(交付) | `FOLLOW_UP`(跟进) | `DOCUMENT_SUBMISSION`(材料提交)
**违规类型：** `APPROVAL_GUARANTEE`(审批保证) — 禁令 #3 禁止

---

## 8. OpportunitySignal — 机会信号

> **本质：** "市场/客户发出的信号"——如"客户提到有融资需求"，但**信号 ≠ 商机**。
> **禁令 #7：OpportunitySignal ≠ Opportunity**

源码：`modules/operational-ontology/.../OpportunitySignal.java`

| 字段 | 含义 |
|------|------|
| `signalId` | 信号唯一标识 |
| `caseId/customerId` | 所属案例/客户 |
| `signalType/source` | 信号类型/来源 |
| `description` | 信号描述 |
| `strength` | 信号强度 |
| `detectedAt` | 检测时间 |
| `evaluated/evaluationResult` | 是否已评估/评估结果 |

转化流程：`OpportunitySignal(detected)` → 评估(bankability) → `Opportunity(identified)`

---

## 9. Opportunity — 商机

> **本质：** "经过评估确认的销售机会"——从信号转化而来，有明确类型、金额和概率。

源码：`modules/operational-ontology/.../domain/Opportunity.java`

| 字段 | 含义 |
|------|------|
| `opportunityId` | 商机唯一标识 |
| `customerId/interactionId/operatingCaseId` | 关联客户/交互/案例 |
| `opportunityType` | 商机类型 |
| `productId/productName` | 关联产品 |
| `description` | 商机描述 |
| `status` | 商机状态 |
| `estimatedAmount/probability` | 预估金额/概率 |
| `assignedTo/source` | 负责人/来源 |
| `nextSteps/expectedCloseDate` | 下一步/预计关闭日期 |

**OpportunityType：** `CROSS_SELL` | `UP_SELL` | `NEW_PRODUCT` | `RENEWAL` | `REFERRAL`

**OpportunityStatus：** `IDENTIFIED` → `QUALIFIED` → `PROPOSAL` → `NEGOTIATION` → `WON`/`LOST`/`STALE`

---

## 10. HumanConfirmation — 人工确认

> **本质：** "人工审核的记录"——任何受控行动都必须有人工确认，AI 不能自主执行。
> **禁令 #1、#2**

源码：`modules/operational-ontology/.../HumanConfirmation.java`

| 字段 | 含义 |
|------|------|
| `confirmationId` | 确认唯一标识 |
| `actionType/actionReference` | 行动类型/引用 |
| `confirmedBy/confirmedAt` | 确认人/时间 |
| `decision` | 决策（APPROVED/REJECTED/MODIFIED） |
| `comments` | 审核意见 |
| `riskAcknowledged` | 风险是否已确认 |

---

## 11. ControlledAction — 受控行动

> **本质：** "需要人工审批才能执行的操作"——CRM 写回、客户邮件等高风险操作，AI 不能自主执行。
> **禁令 #1、#2**

源码：`modules/operational-ontology/.../ControlledAction.java`

| 字段 | 含义 |
|------|------|
| `actionId` | 行动唯一标识 |
| `caseId` | 所属经营案例 |
| `actionType` | 行动类型 |
| `description` | 行动描述 |
| `proposedBy/proposedAt` | 提议人/时间 |
| `confirmationId` | 关联人工确认 |
| `status` | 状态 |
| `executedAt/executionResult` | 执行时间/结果 |

受控类型：`CRM_WRITEBACK`(CRM写回) | `CUSTOMER_COMMUNICATION`(客户沟通) | `COMMITMENT_RECORDING`(承诺记录) | `RISK_ACKNOWLEDGMENT`(风险确认)

---

## 12. FactReconciliationCase — 事实对账案例

> **本质：** "一次事实校验的完整记录"——记录对某条 Claim 进行四维校验的输入、过程和结果。

源码：`modules/operational-ontology/.../FactReconciliationCase.java`

| 字段 | 含义 |
|------|------|
| `reconciliationId` | 对账唯一标识 |
| `claimId/caseId` | 关联Claim/案例 |
| `status` | 对账状态 |
| `conflictDetected/authoritativeMatch/evidenceComplete` | DMN三维度输入 |
| `reconciliationResult` | 对账结果 |
| `reconciledAt/reconciledBy` | 对账时间/执行者 |

**ReconciliationStatus：** `OPEN` | `IN_PROGRESS` | `RESOLVED` | `ESCALATED`

---

## 13. ExternalEvent — 外部事件

> **本质：** "来自银行外部的事件信号"——工商变更、司法涉诉、行业政策变化等。

源码：`modules/operational-ontology/.../ExternalEvent.java`

| 字段 | 含义 |
|------|------|
| `eventId/eventType` | 事件标识/类型 |
| `source` | 事件来源 |
| `customerId` | 关联客户 |
| `description` | 事件描述 |
| `severity` | 严重程度 |
| `occurredAt/processedAt` | 发生/处理时间 |
| `processingResult` | 处理结果 |

---

## 14. KycGapProfile — KYC缺口画像

> **本质：** "客户信息完整度的体检报告"——标记哪些维度信息缺失。
> **禁令 #5：no_infer_full_cashflow**

源码：`modules/operational-ontology/.../KycGapProfile.java`

| 字段 | 含义 |
|------|------|
| `profileId` | 画像唯一标识 |
| `customerId/caseId` | 关联客户/案例 |
| `financialsComplete` | 财务信息是否完整 |
| `ownershipComplete` | 股权信息是否完整 |
| `operationsComplete` | 经营信息是否完整 |
| `complianceComplete` | 合规信息是否完整 |
| `overallCompleteness` | 整体完整度（0.0-1.0） |
| `gapSummary` | 缺口摘要 |

---

## 15. BankRelationshipSnapshot — 银企关系快照

> **本质：** "某一时点客户与本行关系的全貌"——存款、贷款、结算等业务快照。

源码：`modules/operational-ontology/.../BankRelationshipSnapshot.java`

| 字段 | 含义 |
|------|------|
| `snapshotId/customerId/snapshotDate` | 标识/客户/日期 |
| `totalDepositBalance/totalLoanBalance` | 存款/贷款余额 |
| `totalCreditLimit/creditUtilization` | 授信总额/使用率 |
| `transactionVolume/productCount` | 交易量/产品数 |
| `relationshipYears` | 合作年限 |
| `riskLevel` | 风险等级 |

**RiskLevel：** `LOW` | `MEDIUM` | `HIGH`

---

## 16. CreditFacility — 授信额度

> **本质：** "银行给客户批复的授信额度"——**可贷性评估 ≠ 审批通过**。
> **禁令 #8：Bankability ≠ Approval**

源码：`modules/operational-ontology/.../CreditFacility.java`

| 字段 | 含义 |
|------|------|
| `facilityId/customerId` | 授信标识/客户 |
| `facilityType` | 授信类型 |
| `approvedLimit/utilizedAmount/availableAmount` | 批准/已用/可用额度 |
| `currency` | 币种 |
| `startDate/expiryDate` | 起止日期 |
| `status/riskRating` | 状态/风险评级 |

---

## 17. GroupRelationship — 集团关系

> **本质：** "企业集团的股权/控制关系"——母子公司、关联公司之间的关系。

源码：`modules/operational-ontology/.../GroupRelationship.java`

| 字段 | 含义 |
|------|------|
| `relationshipId` | 关系唯一标识 |
| `parentEntityId/childEntityId` | 母/子公司标识 |
| `relationshipType` | 关系类型 |
| `ownershipPercentage` | 持股比例 |
| `effectiveDate/source` | 生效日期/数据来源 |

---

## 18. LegalEntity — 法律实体

> **本质：** "具有独立法人资格的实体"——一个客户可能对应多个法律实体。

源码：`modules/operational-ontology/.../LegalEntity.java`

| 字段 | 含义 |
|------|------|
| `entityId/customerId` | 实体标识/关联客户 |
| `entityName/entityType` | 名称/类型 |
| `registrationNumber` | 注册号 |
| `legalRepresentative` | 法定代表人 |
| `registeredAddress/businessScope` | 注册地址/经营范围 |

---

## 19. ProductKnowledgeCard — 产品知识卡

> **本质：** "银行产品的结构化知识"——产品特性、适用行业、所需材料等。

源码：`modules/operational-ontology/.../ProductKnowledgeCard.java`

| 字段 | 含义 |
|------|------|
| `productId/productName/category` | 产品标识/名称/类别 |
| `description` | 产品描述 |
| `targetIndustries/keyFeatures` | 目标行业/关键特性 |
| `requiredMaterials` | 所需材料 |
| `riskLevel/pricingBasis` | 风险等级/定价基础 |
| `effectiveFrom/effectiveTo` | 生效/失效日期 |

---

## 20. PolicyRule — 政策规则

> **本质：** "银行内部的合规/业务规则"——授信限额、行业准入、反洗钱要求等。

源码：`modules/operational-ontology/.../PolicyRule.java`

| 字段 | 含义 |
|------|------|
| `ruleId/ruleName` | 规则标识/名称 |
| `category` | 规则类别 |
| `description` | 规则描述 |
| `severity` | 严重程度 |
| `isActive` | 是否生效 |
| `effectiveFrom/effectiveTo` | 生效/失效日期 |

---

## 21. RelationshipReport — 关系报告

> **本质：** "客户关系的综合分析报告"——基于快照和交互数据生成的洞察报告。

源码：`modules/operational-ontology/.../RelationshipReport.java`

| 字段 | 含义 |
|------|------|
| `reportId/customerId` | 报告标识/客户 |
| `reportType` | 报告类型 |
| `period` | 报告周期 |
| `summary` | 摘要 |
| `recommendations` | 建议 |
| `generatedAt/generatedBy` | 生成时间/人 |

---

## 22. EvaluationResult — 评估结果

> **本质：** "对客户/商机的评估结论"——**评估 ≠ 审批**（禁令 #8）。
> **禁令 #8：Bankability ≠ Approval**

源码：`modules/operational-ontology/.../EvaluationResult.java`

| 字段 | 含义 |
|------|------|
| `evaluationId` | 评估唯一标识 |
| `caseId/customerId` | 关联案例/客户 |
| `evaluationType` | 评估类型 |
| `result` | 评估结果 |
| `score` | 评分 |
| `evaluatedAt/evaluatedBy` | 评估时间/人 |
| `validUntil` | 有效期至 |

---

## 23. ActionReceipt — 行动回执

> **本质：** "受控行动执行后的回执记录"——证明行动已被执行，记录执行结果。

源码：`modules/operational-ontology/.../ActionReceipt.java`

| 字段 | 含义 |
|------|------|
| `receiptId` | 回执唯一标识 |
| `actionId` | 关联受控行动 |
| `confirmationId` | 关联人工确认 |
| `executedBy/executedAt` | 执行人/时间 |
| `executionStatus` | 执行状态 |
| `executionResult` | 执行结果 |
| `auditTrail` | 审计轨迹 |

---

## 24. Transaction — 交易流水

> **本质：** "客户账户的资金进出明细"——存款、贷款、结算等交易记录。

源码：`modules/operational-ontology/.../Transaction.java`

| 字段 | 含义 |
|------|------|
| `id/transactionId` | 主键/流水号 |
| `customerId/accountId` | 客户/账户 |
| `transactionType` | 交易类型 |
| `amount/currency` | 金额/币种 |
| `counterparty/counterpartyIndustry` | 交易对手/行业 |
| `description/transactionDate` | 描述/日期 |

**TransactionType：** `DEPOSIT` | `WITHDRAWAL` | `TRANSFER_IN` | `TRANSFER_OUT` | `LOAN_DISBURSE` | `LOAN_REPAY` | `TRADE_SETTLEMENT` | `FEE`

---

## 25. TransactionRecord — 交易记录

> **本质：** "简化的交易流水"——用于场景数据中的交易快照。

源码：`modules/operational-ontology/.../TransactionRecord.java`

| 字段 | 含义 |
|------|------|
| `id/customerId` | 主键/客户 |
| `transactionDate/transactionType` | 日期/类型 |
| `counterparty/amountCny` | 对手方/金额(CNY) |
| `description/evidenceRef` | 描述/证据引用 |

---

## 26. EvidenceVersionLink — 证据版本链接

> **本质：** "证据版本链的跟踪"——记录证据的每次变更，形成可追溯的版本历史。

源码：`modules/operational-ontology/.../domain/EvidenceVersionLink.java`

| 字段 | 含义 |
|------|------|
| `linkId/evidenceId` | 链接标识/证据标识 |
| `previousVersionId/nextVersionId` | 前/后版本 |
| `versionNumber` | 版本号 |
| `changeType` | 变更类型 |
| `changeReason/changedBy/changedAt` | 变更原因/人/时间 |

**changeType：** `CREATE` | `UPDATE` | `CORRECT` | `AMEND`

---

## 27. ClaimLifecycleEvent — 声明生命周期事件

> **本质：** "Claim 状态变更的审计记录"——每次 Claim 状态流转都留下不可篡改的事件。

源码：`modules/operational-ontology/.../domain/ClaimLifecycleEvent.java`

| 字段 | 含义 |
|------|------|
| `eventId/claimId` | 事件标识/Claim标识 |
| `fromStatus/toStatus` | 原状态/新状态 |
| `transitionReason` | 转换原因 |
| `actorId/actorRole` | 操作人/角色 |
| `transitionedAt` | 转换时间 |

---

## 28. ProductKnowledgeVersion — 产品知识版本

> **本质：** "产品信息的版本化管理"——产品知识卡的变更历史。

源码：`modules/operational-ontology/.../domain/ProductKnowledgeVersion.java`

| 字段 | 含义 |
|------|------|
| `versionId/productId/versionNumber` | 版本标识/产品/版本号 |
| `productName/category/description` | 名称/类别/描述 |
| `keyFeatures/targetIndustries` | 关键特性/目标行业 |
| `riskLevel/requiredMaterials/pricingBasis` | 风险/材料/定价 |
| `previousVersionId/changeSummary/changedBy` | 前版本/变更摘要/变更人 |

---

## 29. InteractionExtension — 交互扩展

> **本质：** "Interaction 与新增实体的关联"——将 Interaction 与 Commitment/Task/Opportunity 等关联。

源码：`modules/operational-ontology/.../domain/InteractionExtension.java`

| 字段 | 含义 |
|------|------|
| `extensionId/interactionId` | 扩展标识/交互标识 |
| `recordingConsentId` | 关联录音同意 |
| `commitmentIds/taskIds/opportunityIds` | 关联承诺/任务/商机列表 |
| `kycGapProfileId` | 关联KYC缺口画像 |

---

## 30. RecordingConsent — 录音录像同意

> **本质：** "交互记录的合规授权"——录音、录像、截屏等需获得客户同意。

源码：`modules/human-action/.../domain/RecordingConsent.java`

| 字段 | 含义 |
|------|------|
| `consentId/interactionId/customerId` | 同意标识/交互/客户 |
| `consentType` | 同意类型 |
| `status` | 状态 |
| `grantedBy/grantedRole/grantedAt` | 授权人/角色/时间 |
| `withdrawalReason/expiresAt/legalBasis` | 撤回原因/过期时间/法律依据 |

**consentType：** `AUDIO_RECORDING` | `VIDEO_RECORDING` | `SCREEN_CAPTURE` | `TRANSCRIPT`

**status：** `GRANTED` | `DENIED` | `WITHDRAWN` | `PENDING`

---

## 31. Task — 任务

> **本质：** "跟进任务/行动项的跟踪"——拜访后的待办事项。

源码：`modules/human-action/.../domain/Task.java`

| 字段 | 含义 |
|------|------|
| `taskId/interactionId/customerId/operatingCaseId` | 标识/交互/客户/案例 |
| `taskType/title/description` | 任务类型/标题/描述 |
| `status/priority` | 状态/优先级 |
| `assignedTo/assignedRole` | 负责人/角色 |
| `dueDate/completedDate` | 截止/完成日期 |
| `tags/parentTaskId` | 标签/父任务 |

**taskType：** `FOLLOW_UP` | `DOCUMENT_COLLECTION` | `COMPLIANCE_CHECK` | `CREDIT_REVIEW` | `CUSTOMER_VISIT`

**status：** `TODO` | `IN_PROGRESS` | `DONE` | `CANCELLED` | `OVERDUE`

**priority：** `URGENT` | `HIGH` | `MEDIUM` | `LOW`

---

## 32. CrmWritebackCommand — CRM回写命令

> **本质：** "向 CRM 系统写回数据的命令"——**AI 不可直接写入 CRM**（禁令 #1）。
> **禁令 #1：no_direct_crm_writeback** — `requiresHumanConfirm` 构造器强制为 true

源码：`modules/scenario-hermes/.../engagement/CrmWritebackCommand.java`

| 字段 | 含义 |
|------|------|
| `commandId` | 命令唯一标识 |
| `objectType` | 回写对象类型 |
| `operation` | 操作类型 |
| `beforeValue/proposedValue` | 变更前/后值 |
| `riskLevel` | 风险等级 |
| `requiresHumanConfirm` | 必须人工确认（强制true） |
| `rmAction/auditRef` | RM操作/审计引用 |
| `idempotencyKey` | 幂等键（≥16字符） |

**ObjectType：** `INTERACTION` | `CUSTOMER` | `CREDIT_FACILITY` | `COMMITMENT`

**Operation：** `CREATE` | `UPDATE` | `DELETE`

---

## 33. CloudEvent — 云事件信封

> **本质：** "领域事件的标准信封"——遵循 CloudEvents v1.0 规范，所有领域事件都包装在此信封中。

源码：`modules/operational-ontology/.../event/CloudEvent.java`

| 字段 | 含义 |
|------|------|
| `specversion` | 规范版本（固定"1.0"） |
| `id` | 事件唯一标识 |
| `source` | 事件来源 |
| `type` | 事件类型（见 DomainEventType） |
| `time` | 事件时间 |
| `subject` | 事件主题 |
| `datacontenttype` | 数据内容类型（固定"application/json"） |
| `data` | 事件数据 |

---

## 34. DomainEventType — 领域事件类型

> **本质：** "系统内定义的事件类型常量"——每种事件类型对应一个 CloudEvent type 值。

源码：`modules/operational-ontology/.../event/DomainEventType.java`

| 常量 | 值 | 含义 |
|------|---|------|
| `CLAIM_CANDIDATE_RECORDED` | `gits.kno.claim-candidate-recorded.v1` | 声明候选已记录 |
| `CONTROLLED_ACTION_REQUESTED` | `gits.kno.controlled-action-requested.v1` | 受控行动已请求 |

---

## 附录A：枚举值速查表

| 枚举类 | 值 | 含义 |
|--------|---|------|
| **CaseStatus** | `OPEN` | 打开 |
| | `IN_PROGRESS` | 进行中 |
| | `PENDING_REVIEW` | 待审核 |
| | `CLOSED` | 已关闭 |
| | `ARCHIVED` | 已归档 |
| **CaseType** | `KYC_REVIEW` | KYC审查 |
| | `CREDIT_ORIGINATION` | 授信发起 |
| | `CROSS_SELL` | 交叉营销 |
| | `RETENTION` | 客户挽留 |
| | `COMPLIANCE_REVIEW` | 合规审查 |
| **Channel** | `IN_PERSON` | 面对面 |
| | `PHONE` | 电话 |
| | `VIDEO` | 视频会议 |
| | `EMAIL` | 邮件 |
| | `WECHAT` | 微信 |
| | `OTHER` | 其他 |
| **ClaimStatus** | `CANDIDATE` | 候选 |
| | `CONFLICT` | 冲突 |
| | `REJECTED` | 拒绝 |
| | `HUMAN_CONFIRMED` | 人工确认 |
| | `VERIFIED_FACT` | 验证事实 |
| **ClaimType** | `CUSTOMER_JOURNEY` | 客户旅程 |
| | `OPPORTUNITY` | 机会信号 |
| | `PRODUCT_CANDIDATE` | 产品候选 |
| | `CUSTOMER_STATEMENT` | 客户陈述 |
| | `SYSTEM_FACT` | 系统事实 |
| | `RISK_SIGNAL` | 风险信号 |
| | `COMMITMENT` | 承诺 |
| | `FOLLOW_UP` | 跟进 |
| **CustomerTier** | `STRATEGIC` | 战略客户 |
| | `KEY` | 重点客户 |
| | `GENERAL` | 一般客户 |
| | `WATCH` | 观察客户 |
| **EnterpriseScale** | `LARGE` | 大型 |
| | `MEDIUM` | 中型 |
| | `SMALL` | 小型 |
| | `MICRO` | 微型 |
| **Industry** | `MANUFACTURING` | 制造业 |
| | `TRADE` | 贸易 |
| | `TECHNOLOGY` | 科技 |
| | `REAL_ESTATE` | 房地产 |
| | `FINANCE` | 金融 |
| | `HEALTHCARE` | 医疗 |
| | `ENERGY` | 能源 |
| | `INFRASTRUCTURE` | 基建 |
| | `OTHER` | 其他 |
| **ListedStatus** | `LISTED_A` | A股上市 |
| | `LISTED_HK` | 港股上市 |
| | `LISTED_US` | 美股上市 |
| | `UNLISTED` | 未上市 |
| | `PRE_IPO` | 准上市 |
| **ReconciliationStatus** | `OPEN` | 待对账 |
| | `IN_PROGRESS` | 对账中 |
| | `RESOLVED` | 已解决 |
| | `ESCALATED` | 已升级 |
| **RiskLevel** | `LOW` | 低风险 |
| | `MEDIUM` | 中风险 |
| | `HIGH` | 高风险 |

---

## 附录B：概念关系图谱

```
Customer ──┬── OperatingCase ──┬── Claim ────── FactReconciliationCase
           │                  ├── Interaction ──┬── InteractionExtension
           │                  │                  ├── RecordingConsent
           │                  │                  ├── Commitment
           │                  │                  └── Task
           │                  ├── Evidence ────── EvidenceVersionLink
           │                  ├── OpportunitySignal ── Opportunity
           │                  ├── HumanConfirmation
           │                  ├── ControlledAction ── ActionReceipt
           │                  ├── KycGapProfile
           │                  ├── ExternalEvent
           │                  ├── EvaluationResult
           │                  └── PolicyRule
           ├── BankRelationshipSnapshot
           ├── CreditFacility
           ├── GroupRelationship ── LegalEntity
           ├── ProductKnowledgeCard ── ProductKnowledgeVersion
           ├── Transaction / TransactionRecord
           ├── RelationshipReport
           └── CrmWritebackCommand ── CloudEvent(DomainEventType)

禁令执行链：
  CrmWritebackCommand(requiresHumanConfirm=true) ── 禁令#1
  ControlledAction → HumanConfirmation ──────────── 禁令#1,#2
  Commitment(无APPROVAL_GUARANTEE) ──────────────── 禁令#3
  Claim(CANDIDATE → VERIFIED_FACT via DMN) ──────── 禁令#6
  OpportunitySignal → Opportunity(需评估) ────────── 禁令#7
  EvaluationResult ≠ CreditFacility(审批) ────────── 禁令#8
```
