# Oracle客户数据 -> GITS知识工程映射方案

## 1. 场景背景

**Hermes场景**: 华东精工客户经理持续经营闭环场景
- 客户: 华东精工制造有限公司
- 客户经理: 李明 (EMP003)
- 核心业务: 授信管理、存款产品交叉销售、供应链金融探索
- 数据源: Hermes演示数据包 (13个文件)

**目标**: 将Oracle数据集市中的真实客户数据加工为GITS知识工程的基础分析数据

---

## 2. 数据源映射总览

### 2.1 Hermes演示数据 -> GITS领域模型

| Hermes数据文件 | GITS领域实体 | 映射关系 |
|---------------|-------------|---------|
| `03_CUSTOMER_MASTER.json` | `OperatingCase.subject` | 客户基础信息 → 案例主题 |
| `05_BANK_RELATIONSHIP_SNAPSHOT.csv` | `OperatingCase.evidence` | 银行关系 → 案例证据 |
| `07_CREDIT_FACILITY.json` | `OperatingCase.evidence` | 授信额度 → 案例证据 |
| `11_HISTORICAL_INTERACTIONS.jsonl` | `Interaction` | 历史交互 → 交互记录 |
| `10_EXTERNAL_EVENTS.jsonl` | `Interaction` | 外部事件 → 交互记录 |
| `12_TIMELINE.csv` | `CustomerJourney` | 时间线 → 客户旅程 |
| `14_CUSTOMER_OPERATING_VIEW_SNAPSHOT.json` | `OperatingCase` | 经营视图 → 经营案例 |
| `15_KYC_GAP_PROFILE.json` | `Claim(RISK_SIGNAL)` | KYC缺口 → 风险信号 |
| `04_GROUP_RELATIONSHIP.json` | `Interaction` | 集团关系 → 关联交互 |
| `23_FACT_RECONCILIATION_CASE.json` | `Claim(SYSTEM_FACT)` | 事实对账 → 系统事实 |

### 2.2 Oracle数据集市 -> GITS领域模型

| Oracle表 | GITS领域实体 | 映射关系 |
|----------|-------------|---------|
| `F_CUST_LOAN_IFO` (55列) | `OperatingCase.evidence` | 贷款指标 → 经营证据 |
| `F_CUST_DEPR_IFO` (61列) | `OperatingCase.evidence` | 存款指标 → 经营证据 |
| `F_CUST_OWNED_PRODUCT` (24列) | `OperatingCase.evidence` | 持有产品 → 经营证据 |
| `F_CUST_ZCAMT_IFO` (75列) | `OperatingCase.evidence` | 资产指标 → 经营证据 |
| `F_CUST_SIGN_IFO` (26列) | `OperatingCase.evidence` | 签约信息 → 经营证据 |
| `F_CUST_CRC_IFO` (44列) | `OperatingCase.evidence` | 信用卡指标 → 经营证据 |
| `A_ZHCX_CUST_BASE` | `OperatingCase.subject` | 客户基础 → 案例主题 |
| `A_ZHCX_CUST_EXP` | `OperatingCase.evidence` | 客户扩展 → 经营证据 |
| `field_lineage` (910条) | `Claim(SYSTEM_FACT)` | 字段血缘 → 系统事实 |
| `derived_field` (1182条) | `Claim(SYSTEM_FACT)` | 衍生字段 → 系统事实 |
| `analysis_issue` (4条) | `Claim(RISK_SIGNAL)` | 分析议题 → 风险信号 |

---

## 3. 领域模型详细映射

### 3.1 OperatingCase (经营案例)

```
Hermes: CustomerOperatingView
  ├── customerProfile → subject
  ├── relationshipSummary → evidence.bankRelationship
  ├── riskOverview → evidence.riskProfile
  ├── crossSellOpportunities → evidence.crossSellOpportunities
  ├── actionItems → evidence.actionItems
  └── lastUpdated → recordedAt

Oracle: F_CUST_* 系列表
  ├── F_CUST_LOAN_IFO → evidence.loanMetrics
  ├── F_CUST_DEPR_IFO → evidence.depositMetrics
  ├── F_CUST_OWNED_PRODUCT → evidence.productHolding
  ├── F_CUST_ZCAMT_IFO → evidence.assetMetrics
  ├── F_CUST_SIGN_IFO → evidence.channelSigning
  ├── F_CUST_CRC_IFO → evidence.creditCardMetrics
  └── DATA_DT → recordedAt
```

### 3.2 Interaction (交互记录)

```
Hermes: HistoricalInteractions + ExternalEvents
  ├── interactionId → interactionId
  ├── timestamp → timestamp
  ├── type → type (FACE_TO_FACE, PHONE_CALL, EMAIL, SYSTEM_ALERT)
  ├── channel → channel
  ├── participants → participants
  ├── content → content.summary
  ├── followUpActions → evidence.followUpActions
  └── relatedCaseIds → relatedCaseIds

Oracle: 无直接交互表 (需从业务日志表提取)
```

### 3.3 CustomerJourney (客户旅程)

```
Hermes: Timeline
  ├── phase → phase (ONBOARDING, ACTIVE_MANAGEMENT, RISK_MONITORING, RENEWAL, EXIT)
  ├── milestone → milestone
  ├── timestamp → timestamp
  ├── status → status
  └── nextActions → nextActions
```

### 3.4 Claim (知识声明)

```
Hermes: FactReconciliation + KYCGap
  ├── SYSTEM_FACT: 事实对账差异
  ├── RISK_SIGNAL: KYC缺口风险

Oracle: MetricDefinition + AnalysisIssue
  ├── SYSTEM_FACT: 175个指标口径
  ├── RISK_SIGNAL: 4个分析议题
```

---

## 4. 数据加工流程

```
Oracle数据集市 (EDWCRM)
    │
    ├─ 客户指标表 (F_CUST_*)
    │   └─→ 聚合为 OperatingCase.evidence
    │
    ├─ 字段血缘 (field_lineage)
    │   └─→ 转化为 Claim(SYSTEM_FACT)
    │
    ├─ 衍生字段 (derived_field)
    │   └─→ 转化为 Claim(SYSTEM_FACT)
    │
    └─ 分析议题 (analysis_issue)
        └─→ 转化为 Claim(RISK_SIGNAL)

Hermes演示数据
    │
    ├─ 客户主数据
    │   └─→ OperatingCase.subject
    │
    ├─ 历史交互
    │   └─→ Interaction
    │
    └─ 时间线
        └─→ CustomerJourney
```

---

## 5. 数据质量映射

| Hermes质量维度 | Oracle对应维度 | GITS映射 |
|---------------|---------------|---------|
| `dataQuality.score` (0.92) | `confidence_level` | `Claim.confidence` |
| `completeness` (0.85) | 字段覆盖率 | `Evidence.completeness` |
| `lastVerified` | `last_analyzed` | `Evidence.lastVerified` |
| `riskRating` | `analysis_issue.severity` | `Claim.severity` |

---

## 6. 下一步行动

1. 编写Oracle客户数据提取脚本
2. 生成Hermes场景的Oracle数据映射种子文件
3. 创建数据血缘追踪Claim
4. 建立持续数据同步管道

