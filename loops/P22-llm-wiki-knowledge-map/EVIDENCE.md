# P22 证据日志

```text
EVIDENCE_ID=
GATE=
ACTOR=
TIMESTAMP=
BASE_COMMIT=
COMMAND=
EXIT_CODE=
ARTIFACT_PATHS=
CLAIM_SCOPE=
```

---

## EVIDENCE_ID: EV-P22-G1G2-001（G1 知识资产化 + G2 KnowledgeElement 实现）

```text
EVIDENCE_ID=EV-P22-G1G2-001
GATE=contract_registration / knowledge_architecture_check / element_read_gate
ACTOR=feature_pilot
TIMESTAMP=2026-08-19
BASE_COMMIT=e54f236
COMMAND=make generate / make check / mvnw -pl adapters/knowledge-filesystem,modules/knowledge-architecture -am test
EXIT_CODE=0
ARTIFACT_PATHS=
  - specs/knowledge-architecture/schemas/knowledge-element.schema.json
  - specs/knowledge-architecture/elements/KI-*/*.md（39 KE）
  - modules/knowledge-architecture/.../KnowledgeElement.java + KnowledgeElementPort.java
  - adapters/knowledge-filesystem/.../FilesystemKnowledgeElementReader.java + Test + IT
CLAIM_SCOPE=G1: 39 KE 资产化; G2: KnowledgeElementPort + filesystem adapter; 单测 6/6 + IT 4/4; make check 全绿; 不修改 P20 合同
```

## EVIDENCE_ID: EV-P22-E1-001（E1 内存快照 + 运行时装配）

```text
EVIDENCE_ID=EV-P22-E1-001
GATE=llm_read_map_gate 前置（生产可用控制面：内存索引 + 运行时装配）
ACTOR=feature_pilot
TIMESTAMP=2026-08-19
BASE_COMMIT=6247048
COMMAND=
  ./mvnw -pl adapters/knowledge-filesystem -am test
  make check
  ./mvnw -pl apps/api -am compile
EXIT_CODE=0
ARTIFACT_PATHS=
  - modules/knowledge-architecture/.../repository/InMemoryKnowledgeStore.java（不可变快照）
  - adapters/knowledge-filesystem/.../KnowledgeSnapshotLoader.java（启动一次性加载）
  - adapters/knowledge-filesystem/.../InMemoryKnowledgeMapReader/InMemoryAssetCatalogReader/InMemoryActivationContractReader/InMemoryRoutePolicyReader/InMemoryKnowledgeElementReader.java（5 内存 Port）
  - apps/api/.../config/KnowledgeArchitectureConfig.java（装配）
  - apps/api/pom.xml（+knowledge-architecture/knowledge-filesystem 依赖）
  - adapters/knowledge-filesystem/src/test/.../InMemoryReadersTest.java + KnowledgeSnapshotLoaderIT.java
CLAIM_SCOPE=
  - 生产可用控制面：启动时从 Git 权威源一次性加载不可变内存快照，运行时高频读内存（消除 GAP-2 每请求扫盘）
  - 5 类 Port（KnowledgeMap/Asset/ActivationContract/RoutePolicy/KnowledgeElement）内存实现，fail-closed
  - 快照加载失败（目录缺失/为空）抛 IllegalStateException 拒绝启动（fail-closed）
  - apps/api 装配完整（KnowledgeSnapshotLoader → store → 5 Port → DefaultRoutePolicyEvaluator → DefaultActivationPlanner）
  - 测试：knowledge-architecture 19 + knowledge-filesystem 44（含 InMemoryReadersTest 6 + KnowledgeSnapshotLoaderIT 3）
  - make check 全绿（contract/architecture/loop-guard/secret/enum/semantic）
  - apps/api compile PASS
  - 不修改 P20 已 qa_pass 合同；不启用 fusion；不迁移生产；不改变现有业务行为
```

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P22-G3-001（G3 大模型优先读图 + LlmClient 注入）

```text
EVIDENCE_ID=EV-P22-G3-001
GATE=llm_read_map_gate（方案 A：规划器决定加载范围）
ACTOR=feature_pilot (feature-pilot-g3)
TIMESTAMP=2026-08-19
BASE_COMMIT=d158a3f
COMMAND=
  ./mvnw -pl modules/knowledge-architecture,adapters/knowledge-filesystem,apps/api -am test
  make check
EXIT_CODE=0
ARTIFACT_PATHS=
  - modules/knowledge-architecture/.../port/KnowledgeWikiPort.java（新增 Port：renderMap/renderKnowledgeItem/renderElement，fail-closed 空文本）
  - adapters/knowledge-filesystem/.../KnowledgeWikiFilesystemAdapter.java（实现 KnowledgeWikiPort，从 InMemoryKnowledgeStore 渲染 LLM 可读受控地图，含 AUTHORITATIVE 标注）
  - apps/api/.../config/KnowledgeArchitectureConfig.java（装配 KnowledgeWikiPort bean + 知识根路径 CWD 无关解析）
  - apps/api/.../service/KnowledgeWikiService.java（读图→systemPrompt→LlmClient.complete，LLM 失败 fallback 模板）
  - 测试：KnowledgeWikiFilesystemAdapterTest(7) + KnowledgeWikiFilesystemAdapterIT(2) + KnowledgeWikiServiceTest(4)
CLAIM_SCOPE=
  - 大模型优先读图：KnowledgeWikiService 执行任务前先 renderMap(scope) 得到 LLM 可读受控知识地图，作为 systemPrompt 注入 LlmClient 再执行（方案 A）
  - 加载范围 scope 由规划器依据 ActivationPlan.taskType 决定（调用方注入）
  - renderMap 输出含场景→知识域→KI→KE 分层导航 + [AUTHORITATIVE] 权威标注
  - fail-closed：未知 ID / 无匹配返回空字符串，不返回 null、不抛异常
  - 纯增量：不修改 P20 合同，不改变现有业务行为
  - 修复既有 apps/api 上下文测试：知识根目录路径解析改为与 KnowledgeSnapshotLoaderIT 一致的 CWD 无关（walk-up）
  - 测试：knowledge-filesystem 52 + apps/api 321（含新增 13 单测/IT）全通过；make check 全绿
```

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## 验证记录（DEV_SELF_CHECK）

| 检查项 | 结果 |
|---|---|
| `make check` | PASS（7 schemas，架构/合同/防护网全绿） |
| knowledge-architecture 测试 | 19 PASS |
| knowledge-filesystem 测试（含 E1） | 44 PASS（InMemoryReadersTest 6 + KnowledgeSnapshotLoaderIT 3） |
| `apps/api` 编译 | PASS |
| 快照加载 fail-closed | 空/缺失根目录拒绝启动（IT 覆盖） |
| 内存读取 fail-closed | 5 类 Port 未命中返回 empty/空数组（单测覆盖） |
| 合同一致性 | generated 与 specs 哈希一致 |
| G3 renderMap | 输出非空，含 KI/KE 导航 + [AUTHORITATIVE]（单测 + 真实数据 IT） |
| G3 renderKnowledgeItem/renderElement | 按 KI 渲染要素清单、按要素渲染详情；未知 ID 返回空（fail-closed） |
| G3 KnowledgeWikiService | systemPrompt 含知识地图、注入 LlmClient；LLM 失败回退模板（单测覆盖） |
| G3 全量测试 | `-pl modules/knowledge-architecture,adapters/knowledge-filesystem,apps/api -am test` 全通过（apps/api 321） |

## EVIDENCE_ID: EV-P22-QA-001（独立 QA 验收 QA_PASS）

```text
EVIDENCE_ID=EV-P22-QA-001
GATE=independent_qa
ACTOR=independent_qa（独立于 implementation_actor=feature_pilot）
TIMESTAMP=2026-08-19
SESSION=qa-p22-formal-001
COMMAND=make check backend-test frontend-test semantic-rule-gate
EXIT_CODE=0
STATUS=QA_PASS
ARTIFACT=loops/P22-llm-wiki-knowledge-map/evidence/qa-review/qa-formal-attestation-report.md
CLAIM_SCOPE=
  - 9/9 gates pass（contract_registration/generate/check/knowledge_architecture_check/element_read_gate/llm_read_map_gate/shadow_e2e/backend_test/independent_qa）
  - backend 321 + worker 22 测试通过，覆盖率达标；frontend vue-tsc/vitest/build 通过
  - 两场景 shadow E2E 黄金比对一致 + LLM 读图导航，formal_output_changed=false
  - 未修改 P20 已 qa_pass 合同；不启用 fusion；不迁移生产；无 DB 写（shadow 内存态）
  - 环境适配：Python 3.14、dependency-check 离线、npm legacy-peer-deps、nanoid 版本误报豁免；db-check 因仓库外凭据不纳入（P22 shadow 不写 DB）
```

状态：QA_PASS（由独立 QA 角色记录，非实现者自签）。
