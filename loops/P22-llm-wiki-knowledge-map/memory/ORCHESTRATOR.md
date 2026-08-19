# P22 LLM-WIKI 知识地图｜Orchestrator

## 当前阶段

`completed_qa_pass`：9/9 Gate 通过，独立 QA 已记录 QA_PASS（session=qa-p22-formal-001），可转 Tech Lead 关闭/归档。

## 已完成能力

- 合同登记：`KnowledgeElement`（CTR-KELEM-001）
- 知识要素资产化：39 个 KE（`specs/knowledge-architecture/elements/`）
- 控制面：`InMemoryKnowledgeStore` + `KnowledgeSnapshotLoader` + 5 个 `InMemory*Reader`（apps/api 装配）
- LLM 读图：`KnowledgeWikiPort` + `KnowledgeWikiFilesystemAdapter` + `KnowledgeWikiService`（方案 A）
- Shadow E2E：`run_p22_shadow_e2e.py`（两场景黄金比对 + LLM 读图导航）

## 允许动作

- 运行 `make evidence-check` / `memory-check` / `check` / `backend-test` 复核证据。
- 更新 Loop 归档/交接文档。
- 若 Owner 授权，评估真实平台接入或生产 cutover。

## 禁止动作

- 未授权实现真实平台接入（RAG/GraphDB/MetadataCatalog 选型留空）。
- 未授权生产 cutover/fusion/写回。
- 引入 OpenWiki（已放弃）。
- 修改 P20 已 qa_pass 合同。
- 声称生产就绪（PRODUCTION_READY=NO）。
