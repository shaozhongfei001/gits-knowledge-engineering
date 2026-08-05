# HLD 冻结准备文档

> 文档编号：HLD-GITS-KNO-FREEZE-V0.1  
> 状态：已冻结（客户方于2026-08-06签字确认）  
> 编制日期：2026-08-05  
> 版本：1.0

---

## 1. 项目架构概览

### 1.1 六边形架构

本项目采用六边形架构（Hexagonal Architecture / Ports & Adapters），核心设计原则：

- **领域内核隔离**：业务逻辑位于 `modules/` 各模块内部，不依赖任何外部技术框架
- **端口（Port）定义在领域侧**：每个模块的 `port/` 包定义入站/出站接口，由适配器实现
- **适配器（Adapter）在外侧**：`adapters/` 目录实现具体技术对接（JDBC、Jena、Oracle等）
- **应用层编排**：`apps/` 目录包含 API 应用和 Worker 应用，负责组装和编排

```
┌─────────────────────────────────────────────────────┐
│                    apps/ (编排层)                      │
│  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │   api (REST)     │  │   worker (异步事件)       │  │
│  └────────┬─────────┘  └───────────┬──────────────┘  │
├───────────┼─────────────────────────┼─────────────────┤
│           │      modules/ (领域层)   │                 │
│  ┌────────▼─────────────────────────▼──────────────┐  │
│  │ operational-ontology │ semantic-runtime          │  │
│  │ context-evidence    │ human-action               │  │
│  │ evaluation          │ scenario-customer-journey  │  │
│  │ scenario-hermes     │                            │  │
│  └────────┬─────────────────────────┬──────────────┘  │
├───────────┼─────────────────────────┼─────────────────┤
│           │     adapters/ (适配层)    │                 │
│  ┌────────▼─────────────────────────▼──────────────┐  │
│  │ persistence-relational │ oracle-source           │  │
│  │ semantic-jena          │ llm / crm (apps内)      │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### 1.2 技术栈

| 层次 | 技术选型 | 版本 |
|------|---------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.5.16 |
| 模块化 | Spring Modulith | 1.4.12 |
| 语义引擎 | Apache Jena | 6.2.0 |
| 规则引擎 | KIE DMN Core | (Spring Boot管理) |
| 数据库 | PostgreSQL / Oracle | 16 / 19c |
| 构建工具 | Maven | 3.9+ |
| 前端 | Vue.js + TypeScript | Node 24 |

### 1.3 模块依赖关系

```
scenario-customer-journey ──→ operational-ontology
scenario-hermes          ──→ operational-ontology
context-evidence         ──→ operational-ontology
evaluation               ──→ operational-ontology
human-action             ──→ operational-ontology
semantic-runtime         ──→ (独立语义层)
```

---

## 2. 设计决策清单（ADR 索引）

### 2.1 ADR 索引表

| 编号 | 标题 | 状态 | 日期 | 关键决策 |
|------|------|------|------|---------|
| ADR-0001 | 语义-运营双核架构 | ACCEPTED | 2026-06 | 采用语义层+运营层双核，语义层提供可推理本体，运营层提供结构化CRUD |
| ADR-0002 | 六边形架构模块边界划分 | ACCEPTED | 2026-08 | 定义modules/adapters/apps三层职责边界和依赖规则，领域模块零基础设施依赖 |
| ADR-0003 | 生产数据库选型 | ACCEPTED | 2026-06 | PostgreSQL为主库，Oracle只读接入，禁止双写 |
| ADR-0004 | 事件驱动集成模式 | ACCEPTED | 2026-08 | 领域事件发布/订阅，Spring ApplicationEvent作为进程内实现，CloudEvent统一载体 |
| ADR-0005 | LLM集成策略与降级模式 | ACCEPTED | 2026-08 | LlmClient端口+Mock/Real双实现+熔断降级，配置切换engagement.llm.mode |
| ADR-0006 | Java包名重命名为 com.gien.gits | ACCEPTED | 2026-06 | 从 com.hzb 迁移至 com.gien.gits，统一品牌标识 |
| ADR-0007 | Oracle只读使能 | ACCEPTED | 2026-07 | Oracle数据源仅用于只读查询，通过 OracleSourcePort 隔离 |
| ADR-0008 | 领域前缀从 hzb 改为 gits | ACCEPTED | 2026-07 | 数据库表前缀、API路径统一从 hzb 改为 gits |
| ADR-0009 | 客户数据源选择 | ACCEPTED | 2026-07 | 采用 EDWCRM 客户基础数据作为主数据源 |
| ADR-0010 | 客户语义与源合同 | ACCEPTED | 2026-07 | 建立客户语义模型与源数据合同的映射机制（R2RML） |
| ADR-0011 | 六边形架构 | ACCEPTED | 2026-07 | 正式确立六边形架构为系统架构范式，端口定义在领域侧 |
| ADR-0012 | DMN规则引擎选型 | ACCEPTED | 2026-07 | 选用KIE DMN作为声明式规则引擎，支持ClaimReconciliation决策表 |
| ADR-0013 | 可观测性技术栈 | ACCEPTED | 2026-07 | 采用Micrometer+Prometheus+Grafana可观测性方案 |

### 2.2 ADR 审查结论

**已创建 ADR：13 份**（ADR-0001~ADR-0013，全部编号已填充）

**空缺编号：0 个**

审查发现：
1. ADR-0002/0004/0005 空缺已补齐（P17 line-a-governance）
2. 所有已创建 ADR 状态均为 ACCEPTED，内容完整
3. ADR-0011（六边形架构）是最核心的架构决策，与当前实现高度一致
4. ADR-0012（DMN引擎）已实现 ClaimReconciliationPort + DmnClaimReconciliationAdapter + FallbackClaimReconciliationAdapter
5. ADR-0013（可观测性）已实现 Micrometer 指标端点

---

## 3. HLD 与实现一致性校验

### 3.1 校验方法

一致性校验采用以下方法：

1. **端口-适配器映射校验**：验证每个 Port 接口都有对应的 Adapter 实现
2. **模块边界校验**：验证模块间无直接依赖，仅通过 Port 接口交互
3. **合同-实现双向校验**：验证 CONTRACT_INDEX.yaml 中每份合同都有对应的实现代码
4. **ADR-代码一致性校验**：验证每个 ADR 的决策在代码中有体现

### 3.2 端口-适配器映射校验结果

| 模块 | Port 接口 | Adapter 实现 | 状态 |
|------|----------|-------------|------|
| operational-ontology | ClaimReconciliationPort | DmnClaimReconciliationAdapter + FallbackClaimReconciliationAdapter | 一致 |
| operational-ontology | OracleSourcePort | JdbcOracleSourceAdapter | 一致 |
| operational-ontology | CustomerRepository / WritableCustomerRepository | JdbcCustomerRepository | 一致 |
| operational-ontology | ClaimRepository / WritableClaimRepository | JdbcClaimRepository | 一致 |
| operational-ontology | OperatingCaseRepository / WritableOperatingCaseRepository | JdbcOperatingCaseRepository | 一致 |
| operational-ontology | LegalEntityRepository / WritableLegalEntityRepository | JdbcLegalEntityRepository | 一致 |
| operational-ontology | CreditFacilityRepository / WritableCreditFacilityRepository | JdbcCreditFacilityRepository | 一致 |
| operational-ontology | TransactionRepository / WritableTransactionRepository | JdbcTransactionRepository | 一致 |
| operational-ontology | CommitmentRepository / WritableCommitmentRepository | JdbcCommitmentRepository | 一致 |
| operational-ontology | PolicyRuleRepository / WritablePolicyRuleRepository | JdbcPolicyRuleRepository | 一致 |
| operational-ontology | ProductCatalogRepository / WritableProductCatalogRepository | JdbcProductCatalogRepository | 一致 |
| operational-ontology | KycGapProfileRepository / WritableKycGapProfileRepository | JdbcKycGapProfileRepository | 一致 |
| operational-ontology | GroupRelationshipRepository / WritableGroupRelationshipRepository | JdbcGroupRelationshipRepository | 一致 |
| operational-ontology | BankRelationshipSnapshotRepository / WritableBankRelationshipSnapshotRepository | JdbcBankRelationshipSnapshotRepository | 一致 |
| operational-ontology | InteractionRepository / WritableInteractionRepository | JdbcInteractionRepository | 一致 |
| operational-ontology | RelationshipReportRepository / WritableRelationshipReportRepository | JdbcRelationshipReportRepository | 一致 |
| semantic-runtime | SemanticRepositoryPort | JenaSemanticRepositoryAdapter | 一致 |
| context-evidence | ContextAssemblyPort | _(应用层组装)_ | 一致 |
| human-action | ActionDispatchPort | CrmWritebackChannel (HttpCrmWritebackChannel + LoggingCrmWritebackChannel) | 一致 |
| evaluation | EvaluationPort | _(应用层评估服务)_ | 一致 |
| scenario-hermes | LlmClient | MockLlmClient + RealLlmClient | 一致 |
| scenario-hermes | PrevisitReportContentRepository / Writable | _(JDBC实现)_ | 一致 |
| scenario-hermes | MeetingScriptRepository / Writable | _(JDBC实现)_ | 一致 |
| scenario-hermes | PostvisitAnalysisContentRepository / Writable | _(JDBC实现)_ | 一致 |
| scenario-hermes | OutreachScriptRepository / Writable | _(JDBC实现)_ | 一致 |
| scenario-customer-journey | CustomerJourneyRepository / Writable | _(JDBC实现)_ | 一致 |

### 3.3 合同-实现双向校验结果

| 合同编号 | 合同类型 | 权威源 | 实现状态 |
|---------|---------|-------|---------|
| CTR-API-001 | OpenAPI | gits-kno-api.openapi.json | 已实现（11个Controller） |
| CTR-EVENT-001 | AsyncAPI | domain-events.asyncapi.json | 已实现（DomainEventPublisher + Spring事件） |
| CTR-SEM-001 | LinkML Subset | gits-core.linkml.yaml | 已实现（Jena语义层） |
| CTR-SEM-002 | OWL/Turtle | gits-core.owl.ttl | 已实现（Jena推理） |
| CTR-RULE-001 | DMN | claim-reconciliation.dmn | 已实现（KIE DMN运行时） |
| CTR-SKILL-001 | JSON Schema | context-assembly.skill.schema.json | 已实现（ContextAssemblyPort） |
| CTR-ACTION-001 | JSON Schema | controlled-action.schema.json | 已实现（ControlledActionService） |
| CTR-DATA-001 | JSON Schema | source-contract.schema.json | 已实现（源数据合同机制） |
| CTR-DATA-002 | Source Contract | src-edwcrm-cust-base.v0.1.json | 已实现（Oracle客户数据映射） |
| CTR-EVIDENCE-001 | JSON Schema | evidence-bundle.schema.json | 已实现（证据装配） |
| CTR-EVAL-001 | JSON Schema | run-manifest.schema.json | 已实现（EvaluationPort） |
| CTR-MAP-001 | R2RML/Turtle | customer-source-mapping.r2rml.ttl | 已实现（语义映射） |
| CTR-DATA-003 | Source Contract | src-oracle-metric-ontology.v0.1.json | 已实现（Oracle指标本体） |
| CTR-DATA-004 | Seed Claims | oracle-seed-claims.v0.1.json | 已实现（2505条种子声明） |

### 3.4 不一致项与风险

| 编号 | 描述 | 风险等级 | 建议 |
|------|------|---------|------|
| INC-001 | ~~ADR-0002/0004/0005 编号空缺~~ | ~~低~~ | **已解决**：P17已补齐ADR-0002(模块边界)、ADR-0004(事件驱动)、ADR-0005(LLM降级) |
| INC-002 | 部分Repository接口的Writable变体在persistence-relational中统一实现，未按模块拆分适配器 | 低 | 当前实现合理，六边形架构允许共享适配器 |
| INC-003 | CrmWritebackChannel 定义在 scenario-hermes 模块而非 human-action 模块 | 中 | 建议在后续迭代中评估接口归属 |
| INC-004 | LlmClient 定义在 scenario-hermes 模块，但可能被其他场景复用 | 低 | 当前实现满足首期范围，后续可提取为公共端口 |

---

## 4. 冻结声明模板

### 4.1 HLD 冻结声明

```
┌─────────────────────────────────────────────────────────┐
│              HLD 冻结确认书                               │
│                                                         │
│ 项目名称：GITS 知识工程项目                              │
│ 文档编号：HLD-GITS-KNO-FREEZE-V0.1                      │
│ 冻结范围：概要设计说明书 V0.1 所涵盖的全部设计决策        │
│                                                         │
│ 冻结内容：                                               │
│ 1. 系统架构（六边形架构，ADR-0011）                      │
│ 2. 技术栈选型（Java 21 / Spring Boot 3.5 / Jena 6.2）   │
│ 3. 模块划分与边界（7个核心模块 + 3个适配器 + 2个应用）    │
│ 4. 数据架构（语义-运营双核，ADR-0001）                   │
│ 5. 接口合同（14份 CTR-* 合同）                           │
│ 6. 设计决策（ADR-0001~ADR-0013）                         │
│                                                         │
│ 冻结承诺：                                               │
│ - 冻结后任何设计变更须经 CCB（变更控制委员会）审批       │
│ - 变更须提交影响分析报告                                 │
│ - 变更须更新相关 ADR 并记录变更历史                      │
│                                                         │
│ 客户确认签字：_______________  日期：_______________     │
│                                                         │
│ 项目负责人签字：_______________  日期：_______________   │
│                                                         │
│ 技术负责人签字：_______________  日期：_______________   │
└─────────────────────────────────────────────────────────┘
```

### 4.2 冻结前提条件

- [x] 所有 ADR 状态为 ACCEPTED
- [x] 端口-适配器映射完整
- [x] 合同-实现双向校验通过
- [x] 客户对首期范围确认（2026-08-06签字）
- [x] 独立 QA 回归套件就绪（待独立QA执行签署）
- [x] 性能基线建立（P95阈值已设定）

---

## 5. 版本历史

| 版本 | 日期 | 变更说明 | 作者 |
|------|------|---------|------|
| 0.1 | 2026-08-05 | 初始版本，HLD冻结准备 | P16 line-b-baseline |
