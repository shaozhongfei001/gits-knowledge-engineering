---
{"schemaVersion":"1.0.0","mapId":"KM-CORP-RM-FACT-RECON","name":"3000万语义事实对账任务地图","version":"0.1.0","status":"VALIDATION","mapType":"TASK","entrypoints":{"roles":["RELATIONSHIP_MANAGER","RISK_MANAGER","AGENT"],"tasks":["FACT_RECONCILIATION_30M"]},"domains":[],"assetRefs":["ASSET-DATA-CUSTOMER-PROFILE","ASSET-DATA-CREDIT-FACILITY","ASSET-DATA-INTERACTION-HISTORY","ASSET-KNOW-CUSTOMER-ONTOLOGY","ASSET-KNOW-EVIDENCE-POLICY","ASSET-KNOW-CLAIM-RECONCILIATION"],"skillRefs":["SP-02","SP-07"],"activationContractRefs":["AC-FACT-RECONCILIATION-001"],"routePolicyRef":"RP-CORP-RM-001","defaultPolicy":"DENY","maxInitialTokens":1600}
---

# 3000万语义事实对账

## 目标

保留客户原话，将“希望银行增加3000万左右支持”与现有额度、已提款、项目投资、设备金额、融资主体和用途信息分层对账。

## 推荐路径

`ONTOLOGY_FIRST`

本任务不是普通文档问答。必须查询并区分：

- 客户关系与法律主体；
- 项目主体与借款主体；
- 已批额度、可用额度、提款金额；
- 项目总投资、设备及配套金额；
- 客户原话、AI解释和人工判断；
- Signal、Need、Opportunity和Approval。

## 输出边界

允许输出多个待确认解释路径；不得未经Human Gate写成新增授信需求、正式商机或审批结论。
