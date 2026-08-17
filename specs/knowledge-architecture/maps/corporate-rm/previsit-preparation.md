---
{"schemaVersion":"1.0.0","mapId":"KM-CORP-RM-PREVISIT","name":"访前准备任务地图","version":"0.1.0","status":"VALIDATION","mapType":"TASK","entrypoints":{"roles":["RELATIONSHIP_MANAGER","AGENT"],"tasks":["PRE_VISIT_PREPARATION"]},"domains":[],"assetRefs":["ASSET-DATA-CUSTOMER-PROFILE","ASSET-DATA-CREDIT-FACILITY","ASSET-DATA-TRANSACTION-SUMMARY","ASSET-DATA-INTERACTION-HISTORY","ASSET-DATA-EXTERNAL-EVENT","ASSET-KNOW-CUSTOMER-ONTOLOGY","ASSET-KNOW-EVIDENCE-POLICY","ASSET-KNOW-PRODUCT-CARDS","ASSET-KNOW-VISIT-SOP","ASSET-KNOW-KYC-QUESTION-LIBRARY"],"skillRefs":["SP-02","SP-05","SP-10","SP-15"],"activationContractRefs":["AC-PREVISIT-001"],"routePolicyRef":"RP-CORP-RM-001","defaultPolicy":"DENY","maxInitialTokens":2000}
---

# 访前准备任务地图

## 目标

围绕一次明确拜访任务，生成客户经理可核验的客户概览、KYC缺口、历史未决事项、产品候选、关键问题和风险提醒。

## 推荐路径

`ONTOLOGY_THEN_MAP`

先通过受控语义查询锁定客户、主体、关系、额度、承诺、已知事实和未知项，再按结果定向激活行业知识、产品知识、问题库和拜访SOP。

## 必要输入

- callerId；
- customerId；
- operatingCaseId；
- visitObjective；
- permissionDecisionId；
- asOf。

## 禁止事项

- 不把客户表达直接写成Verified Fact；
- 不把可用额度视为新增融资需求；
- 不把产品候选视为审批结论；
- 不直接创建CRM正式商机；
- 不在权限未决时返回客户敏感上下文。
