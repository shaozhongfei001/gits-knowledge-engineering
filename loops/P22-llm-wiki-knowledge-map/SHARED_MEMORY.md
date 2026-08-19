# P22 Shared Memory

## Owner 决策（2026-08-19）

```text
OWNER_DECISION=APPROVE_P22_CONTRACT_REGISTRATION_LIMITED + 增强 Git 控制面至生产可用
OPENWIKI=放弃引入（代码级评估三大硬伤：权限/内网CDN/非结构化）
HUMAN_MACHINE_READ=保留目标（人机共读知识地图/目录/契约），当前测试过渡期人操作不急
P22_SCOPE=LLM-WIKI 知识地图 shadow 实现（对齐《银行知识工程规范打样_fixed.xlsx》）
PRODUCTION_CUTOVER=NOT_AUTHORIZED
FUSION_CUTOVER=NOT_AUTHORIZED
PRODUCTION_WRITEBACK=NOT_AUTHORIZED
REAL_PLATFORM_SELECTION=留空，待 Owner 指定（RAG/GraphDB/MetadataCatalog）
INDEPENDENT_QA=PASS（independent_qa，actor≠feature_pilot，session=qa-p22-formal-001）
PRODUCTION_READY=NO
FROZEN=NO
decision_source=Human Owner directive supplied through the execution prompt
```

## 不变量

- Knowledge Map 是 Agent 根入口；LLM 优先读图（方案 A：规划器决定加载范围）再执行任务。
- 权威源永远是 `specs/`（SSOT）；OpenWiki 永不成为权威源/权限引擎/本体运行时。
- 知识要素（KE）以《银行知识工程规范打样_fixed.xlsx》4.4 为准（K-Type-F/R/P/E/M），不发明结构。
- 控制面读取为内存快照（启动加载），运行时高频读内存，不每请求扫盘。
- fail-closed：加载失败拒绝启动；未命中返回 empty/空数组；权限未决拒绝。

## 关键资源

- 权威规范：`docs/dd/银行知识工程规范打样_fixed.xlsx`
- 设计草案：`docs/design/LLM-WIKI-KNOWLEDGE-MAP.md`
- 生产就绪差距/增强：`docs/design/P22-GIT-CONTROL-PLANE-PRODUCTION.md`
- 数据参考：`docs/design/knowledge-engineering-map-ref.json`
- 知识要素资产：`specs/knowledge-architecture/elements/`
- 代码级 OpenWiki 评估：`docs/architecture/OPENWIKI_CODE_LEVEL_ASSESSMENT.md`

## 已完成 Gate（9/9 pass）

contract_registration / contract_generate / contract_check / knowledge_architecture_check / element_read_gate / llm_read_map_gate / shadow_e2e / backend_test / independent_qa
