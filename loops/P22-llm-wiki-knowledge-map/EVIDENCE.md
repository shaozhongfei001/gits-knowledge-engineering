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
