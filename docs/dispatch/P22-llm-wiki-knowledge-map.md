# P22 Dispatch｜LLM-WIKI 知识工程地图/目录（对齐规范 + 大模型优先读图）

```text
DISPATCH_ID=P22-LLM-WIKI-KNOWLEDGE-MAP
STATUS=APPROVED_CONTRACT_REGISTRATION
OWNER_DECISION=OWNER_APPROVED_P22_CONTRACT_REGISTRATION_LIMITED
BASE_COMMIT=33a93c4
LOOP=loops/P22-llm-wiki-knowledge-map
DESIGN_DRAFT=docs/design/LLM-WIKI-KNOWLEDGE-MAP.md
AUTHORITY_SPEC=docs/dd/银行知识工程规范打样_fixed.xlsx（最高权威）
MAP_REF=docs/design/knowledge-engineering-map-ref.json
CONTRACT_CANDIDATE_HOLDER=tech_lead
IMPLEMENTATION_ACTOR=feature_pilot
IMPLEMENTATION_SCOPE=shadow_only
PRODUCTION_READY=NO
FROZEN=NO
QA_ACTOR=MUST_BE_INDEPENDENT
```

## 目标

以《银行知识工程规范打样_fixed.xlsx》为最高权威，实现"LLM-WIKI 知识工程地图/目录"能力：业务场景 → 知识域 → 知识条目(KI) → 知识要素(KE) 的分层受控地图，并支持**大模型优先读图再执行任务**（读图机制：方案 A，由规划器决定加载范围）。

## 背景

- P20 已实现 wiki-ontology 融合控制面（KnowledgeMap/AssetManifest/ActivationContract/RoutePolicy/ActivationPlan/受控语义查询/上下文装配），qa_pass。
- P20 模型是**运行时控制面**（技术资产层）；规范（xlsx）定义**知识语义层**（KD/KI/KE）。两者分层互补，需对齐。
- 草案 v0.2（docs/design/LLM-WIKI-KNOWLEDGE-MAP.md）已明确差距与合同候选。

## Gap 分解

| Gap | 范围 | 退出条件 |
|---|---|---|
| G0 | 合同登记与生成 | KnowledgeElement 等新合同登记 + AssetManifest/ActivationContract/KnowledgeMap 扩展（explicit_migration）登记，generate/check 通过 |
| G1 | 知识地图/目录资产化 | 规范 KI/KE/RUL/SK/T/DS 全量录入知识地图与目录，可校验（基于 map-ref.json） |
| G2 | KnowledgeElement 读取 | KnowledgeElementPort + filesystem adapter，fail-closed |
| G3 | LLM 读图（方案 A） | 规划器决定注入范围 → KnowledgeWikiPort.render → LlmClient 注入，LLM 先读图再执行 |
| G4 | 两场景 Shadow | 访前准备/事实对账 两场景 shadow E2E，不改变正式输出 |
| G5 | 回归与 QA | 全量回归 + 独立 QA attestation |

## 明确排除

- 真实 RAG / GraphDB / OpenMetadata 接通（选型未定，留待 Owner 指定）；
- 生产切换 / fusion 切换；
- 自动 CRM 写回；
- Oracle 写回（quarantine 隔离资产，需单独授权）；
- 开发自签 QA。

## 开发规则

1. 规范（xlsx）是最高权威；KI/KE/RUL 定义以规范为准，不发明结构。
2. 先合同后实现；`generated/` 只读。
3. 合同变更（新增 KE + 扩展 AssetManifest 等）**需 Owner/CCB 审批**后方可登记 CONTRACT_INDEX。
4. 失败先记录到 FAILURES.md。
5. 未经 Owner 批准不得从 SHADOW 切到 FUSION。
6. 开发只记录 DEV_SELF_CHECK；独立 QA 单独签署 QA_PASS。
7. 真实平台接入选型（RagEmbedding/GraphDB/MetadataCatalog）留空，不臆测。
