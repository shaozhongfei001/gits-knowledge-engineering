# P8 Shared Memory — Hermes场景全链路落地

## 项目架构约束
- Java 21, Spring Boot 3.5.16, Spring Modulith 1.4.12, Apache Jena 6.2.0
- 合同驱动: specs/CONTRACT_INDEX.yaml 是唯一合同注册表
- 8条禁令: no direct_crm_writeback, no auto_send_customer_message, no approval_commitment, no truth_score, no infer_full_cashflow, Claim≠Fact, OpportunitySignal≠Opportunity, Bankability≠Approval
- 不手动编辑generated/

## 已实现的核心骨架
- OperatingCase (caseId, caseType, status, purpose, validFrom, validTo, recordedAt, createdBy)
- Claim (claimId, caseId, claimType, status, statement, validFrom, validTo, supersedesClaimId)
- Evidence (evidenceId, caseId, sourceType, sourceRef, contentHash, capturedAt)
- Interaction (14字段, 含InteractionType/Direction/Participant/Outcome)
- CustomerJourney (journeyId, caseId, customerId, phase, contextBundle, currentPrevisitReport, currentPostvisitAnalysis, openedAt)
- JourneyPhase: KYC_COLLECT→INSIGHT_ANALYSIS→PRODUCT_MATCHING→PREVISIT_PREP→POSTVISIT_REVIEW→COMPLETED
- PrevisitReport, PostvisitAnalysis, InsightClaim, ProductCandidateClaim
- ControlledAction + ActionReceipt (人工确认机制)
- EvaluationRun + EvaluationResult
- Outbox事件机制 (CloudEvent + WorkerEventHandler)
- JDBC Repositories: OperatingCase, Claim, Interaction, CustomerJourney

## 缺失的关键能力 (Gap Analysis)
### 数据层
- customer, legal_entity, group_relationship 表
- bank_relationship_snapshot, credit_facility, transaction_ledger 表
- product_catalog, policy_rule, external_event 表
- kyc_gap, fact_reconciliation, opportunity_signal, commitment, relationship_report 表

### 领域对象
- Customer, LegalEntity, GroupRelationship
- BankRelationshipSnapshot, CreditFacility, TransactionRecord
- ProductKnowledgeCard, PolicyRule, ExternalEvent
- KYCGapProfile, FactReconciliationCase, OpportunitySignal
- Commitment, RelationshipReport

### 服务层
- CustomerContextService (Customer Operating View组装)
- ProductKnowledgeService, PolicyRuleService
- KYCGapAnalysisService, FactReconciliationService
- PrevisitReportGenerator (R1/R2/R3)
- MeetingTranscriptProcessor, InteractionExtractionService
- ReportGenerationService (R4/R5A/R5B/R7/R8)
- CRMWritebackService (全部require_human_confirm)
- AgentOrchestrator, ContextInheritanceService

### API层
- /api/customer/**, /api/context/**, /api/kyc/**
- /api/visit/pre-visit/**, /api/visit/post-visit/**
- /api/report/**, /api/crm/writeback/**

## Hermes场景关键数据
- 客户: 华东精工集团 (3法人实体: 集团本部, 智能制造, 自动化科技)
- RM: 张伟, 决策人: 王强, 财务总监: 李明, 采购VP: 赵芳
- 授信: 1.5亿 (集团本部8000万, 智能制造5000万, 自动化科技2000万)
- 存款: 8200万 (下降趋势), 流水: 2.8亿
- 关键事件: 设备付款+32%, 智能制造二期备案, 3000万模糊表达
- 8阶段时间线: 2026-06-15 ~ 2026-07-15

## 当前迭代焦点
- Phase P1: 数据基础层 — 创建V003/V004/V005 SQL迁移 + 领域对象

## 决策日志
| 日期 | 决策 | 理由 |
|------|------|------|
| 2026-08-04 | 采用增量SQL迁移而非重建 | 保留已有V001/V002数据结构 |
| 2026-08-04 | 领域对象放在各Modulith模块中 | 遵循Spring Modulith模块化设计 |
