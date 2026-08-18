---
{"schemaVersion":"1.0.0","mapId":"KM-GITS-ROOT","name":"GITS企业知识工程根知识地图","version":"0.1.0","status":"VALIDATION","mapType":"ROOT","entrypoints":{"roles":["RELATIONSHIP_MANAGER","PRODUCT_MANAGER","RISK_MANAGER","AGENT"],"tasks":["PRE_VISIT_PREPARATION","FACT_RECONCILIATION_30M","MARKET_SIGNAL_DISCOVERY","REPORT_GENERATION"]},"domains":[{"domainId":"KD-CORP-RM","name":"对公客户持续经营","purpose":"支持经营触发、访前、互动、访后和持续经营闭环","mapRef":"specs/knowledge-architecture/maps/corporate-rm/DOMAIN_MAP.md"}],"assetRefs":[],"skillRefs":[],"activationContractRefs":[],"routePolicyRef":"RP-CORP-RM-001","defaultPolicy":"DENY","maxInitialTokens":1200}
---

# GITS根知识地图

本文件是LLM/Agent进入企业知识工程的唯一根入口。它只提供知识域、任务入口和路由策略，不直接加载客户数据、完整知识包或运行轨迹。

## 使用规则

1. 先识别调用人、任务类型和客户范围；
2. 未映射任务默认拒绝，不允许扫描整个仓库寻找信息；
3. 进入对公持续经营域后读取该域地图；
4. 由Route Policy决定Wiki-first、本体优先或混合路径；
5. 任何数据、知识、Skill和运行反馈都必须通过Asset Manifest激活；
6. SENSITIVE和RESTRICTED资产必须继承调用人权限；
7. Agent只能产生候选Claim、Proposal或受控Action请求。

## 当前验证域

- [对公客户持续经营](corporate-rm/DOMAIN_MAP.md)

## P20限制

本地图仅用于文件系统和Mock接口架构验证，不表示真实RAG、Graph、IAM、CRM或Oracle已经接通。
