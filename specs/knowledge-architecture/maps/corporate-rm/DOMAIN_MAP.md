---
{"schemaVersion":"1.0.0","mapId":"KM-CORP-RM","name":"对公客户持续经营知识域地图","version":"0.1.0","status":"VALIDATION","mapType":"DOMAIN","entrypoints":{"roles":["RELATIONSHIP_MANAGER","PRODUCT_MANAGER","RISK_MANAGER","AGENT"],"tasks":["PRE_VISIT_PREPARATION","FACT_RECONCILIATION_30M","MARKET_SIGNAL_DISCOVERY","REPORT_GENERATION"]},"domains":[{"domainId":"KD-CORP-RM-PREVISIT","name":"访前准备","purpose":"装配客户事实、KYC缺口、历史互动、产品知识和风险规则","mapRef":"specs/knowledge-architecture/maps/corporate-rm/previsit-preparation.md"},{"domainId":"KD-CORP-RM-RECON","name":"事实对账","purpose":"区分客户表达、额度、提款、项目投资与融资需求","mapRef":"specs/knowledge-architecture/maps/corporate-rm/fact-reconciliation.md"}],"assetRefs":["ASSET-DATA-CUSTOMER-PROFILE","ASSET-DATA-CREDIT-FACILITY","ASSET-DATA-INTERACTION-HISTORY","ASSET-KNOW-CUSTOMER-ONTOLOGY","ASSET-KNOW-EVIDENCE-POLICY"],"skillRefs":["SP-02","SP-05","SP-07","SP-10","SP-15"],"activationContractRefs":["AC-PREVISIT-001","AC-FACT-RECONCILIATION-001"],"routePolicyRef":"RP-CORP-RM-001","defaultPolicy":"DENY","maxInitialTokens":1800}
---

# 对公客户持续经营知识域

## 业务主链

```text
经营触发 → 访前准备 → 互动记录 → 访后分析 → 持续经营 → 反馈评测
```

## 路径选择原则

- 开放研究、行业材料和报告组织优先使用知识地图及文档资产；
- 对象身份、关系、状态、额度、Claim/Fact边界和规则判断优先使用本体及DMN；
- 非结构化材料抽取后必须进入本体类型校验；
- 需要产品说明时先用本体约束候选范围，再加载对应产品知识卡；
- 所有结果统一形成ActivationPlan和EvidenceBundle。

## 当前任务入口

- [访前准备](previsit-preparation.md)
- [3000万语义事实对账](fact-reconciliation.md)
