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

初始状态：合同已登记，G1/G2 实现中。

---

## EVIDENCE_ID: EV-P22-G1G2-001（G1 知识资产化 + G2 KnowledgeElement 实现）

```text
EVIDENCE_ID=EV-P22-G1G2-001
GATE=contract_registration / knowledge_architecture_check / element_read_gate
ACTOR=feature_pilot
TIMESTAMP=2026-08-19
BASE_COMMIT=e54f236
COMMAND=
  make generate
  make check
  ./mvnw -pl adapters/knowledge-filesystem,modules/knowledge-architecture -am test
  ./mvnw -pl adapters/knowledge-filesystem -am test -Dtest=FilesystemKnowledgeElementReaderIT -Dsurefire.failIfNoSpecifiedTests=false
EXIT_CODE=0
ARTIFACT_PATHS=
  - specs/knowledge-architecture/schemas/knowledge-element.schema.json（合同源，CTR-KELEM-001）
  - specs/knowledge-architecture/elements/KI-*/*.md（39 个权威 KE 资产，按 KI 分组）
  - modules/knowledge-architecture/.../KnowledgeElement.java + KnowledgeElementPort.java
  - adapters/knowledge-filesystem/.../FilesystemKnowledgeElementReader.java
  - adapters/knowledge-filesystem/src/test/.../FilesystemKnowledgeElementReaderTest.java + IT
CLAIM_SCOPE=
  - G1：规范《银行知识工程规范打样_fixed.xlsx》4.4 的 39 个 KE 资产化到 elements/，按 KI 分组（KI-009×8, KI-FRONT-001×3, 002×9, 003×6, 004×4, 005×4, 006×5），全部符合 knowledge-element 合同（K-Type-F/R/P/E/M，source.authority 映射）
  - G2：KnowledgeElement record + KnowledgeElementPort（find/listByKnowledgeItem）+ FilesystemKnowledgeElementReader（fail-closed，PathSafety 路径防护）
  - 单元测试 FilesystemKnowledgeElementReaderTest 6/6 + 集成测试 IT 4/4（真实 39 个 KE 全加载，KI-009 8 要素，KI-FRONT-002 9 维度含 K-Type-M/E）
  - 全模块测试：knowledge-architecture 19 + knowledge-filesystem 38 = 57 通过，BUILD SUCCESS
  - make check 全绿（contract/architecture/loop-guard/secret/enum/semantic）
  - 不修改 P20 已 qa_pass 的 AssetManifest/ActivationContract/KnowledgeMap 合同
  - 不启用 fusion、不迁移生产、不修改现有业务行为
```

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## 验证记录（DEV_SELF_CHECK）

| 检查项 | 结果 |
|---|---|
| `make check` | PASS（7 schemas，架构/合同/防护网全绿） |
| 单元测试 FilesystemKnowledgeElementReaderTest | 6/6 PASS |
| 集成测试 FilesystemKnowledgeElementReaderIT | 4/4 PASS（39 KE 真实数据） |
| 全模块测试 | knowledge-architecture 19 + knowledge-filesystem 38，BUILD SUCCESS |
| 路径穿越防护 | 拒绝 `../secret`、空、null |
| 合同一致性 | generated 与 specs 哈希一致 |
