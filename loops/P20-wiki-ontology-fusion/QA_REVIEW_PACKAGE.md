# P20 独立 QA 评审包（供独立 QA Actor 使用）

```text
PACKAGE_ID=QA-PKG-P20-001
PREPARED_BY=feature_pilot（仅准备，非 QA 签署）
PREPARED_AT=2026-08-19
FINAL_CANDIDATE_HEAD=52fa6d5644f9c01257ceef64d8a93adf35493a11
FORMAL_GATES=6/6_PASS
INDEPENDENT_QA=PENDING
PRODUCTION_READY=NO
FROZEN=NO
```

> 本包由开发角色整理，供独立 QA Actor 对最终候选 HEAD 进行正式评审。**独立 QA 才可执行 `qa_attest.py` 并记录 `QA_PASS`**；开发角色不得自签。

## 1. 待评审的最终候选 HEAD

- 分支：`feature/p20-wiki-ontology-fusion`
- 最终 HEAD：`52fa6d5644f9c01257ceef64d8a93adf35493a11`
- 工作区：干净（仅本包与证据文件为新增未提交项）
- P19 原工作区（`/home/szf/dev/gits-knowledge-engineering`）：**未修改**

## 2. 6/6 正式 Gate 证据

| Gate | 状态 | 证据文件（hash-attested） |
|---|---|---|
| contract_generate | PASS | `evidence/contract-gates/contract-gates-evidence.txt` |
| contract_check | PASS | 同上 |
| knowledge_architecture_check | PASS | 同上 |
| security_check | PASS | `evidence/security-check/security-check-evidence.txt` |
| shadow_e2e | PASS | `evidence/shadow-e2e/shadow-e2e-evidence.json` |
| backend_test | PASS | `evidence/backend-test/backend-test-evidence.txt` |

`make backend-test` 在最终 HEAD 上 EXIT=0：`mvn verify` BUILD SUCCESS + dependency-check-guard 全部 15 份报告 PASS + apps/api 覆盖率 0.804。

## 3. P1 安全治理结果

| 项 | 结果 |
|---|---|
| P1A nanoid | RESOLVED（`overrides nanoid ^5.1.16`；GHSA-28wg 消除） |
| P1C Connector/J | 临时缓解（9.7.0→9.6.0，9.6.0 不在受影响区间；上游 patch 待发布） |
| P1C Log4j | 组件不匹配（无 log4j-core，仅 log4j-api + to-slf4j 桥） |
| P1D Tomcat | 10.1.55→10.1.57（统一，5/6 CVE 清除；CVE-2026-66299 组件不匹配窄抑制） |
| P1D Micrometer | 组件不匹配（无 Prometheus server/remote-read） |
| P1B apps/api 覆盖率 | 0.804（≥0.80，JaCoCo gate met） |

## 4. P3/P4 结果

- **P3 Shadow E2E**：两场景可重放（planHash 稳定 16 位 hex）、重复运行证据一致、`formal_output_changed=False`；production/writeback/第三场景/空/重复/不支持模式 fail-closed。tooling-test 32 OK。
- **P4 合同索引**：KnowledgeMap/AssetManifest/ActivationContract/RoutePolicy/ActivationPlan/EvidenceBundle 均注册于 CONTRACT_INDEX；Semantic Query 合同基础经激活合同 semanticQueries ∪ 技能 semanticDependencies 并集（9 SQ ID）精确匹配；ADR-0015/16/17 注册；`make generate` 无 diff、`make check` PASS。

## 5. 未关闭 MINOR（独立 QA 需核对）

- MINOR-1（planHash 跨实现规范化不一致）：记录为 refinement，未阻断。
- MINOR-3（reader 必需字段校验未完全对齐 schema.required）：真实数据均合法。
- MINOR-4（Context 装配使用随机 UUID/now/hashCode）：context 层可重放非本 Loop 硬性要求。
- MINOR-5（Java IT 以字面量比对黄金而非读 JSON）：Python E2E 已承担读文件比对。
- 62 项 secret-scan advisory（如实报告，未写成零风险）。

## 6. 独立 QA 执行步骤（由独立 QA Actor 完成）

```bash
# 前置：确认 6/6 gate pass 且 loop_guard 通过
python3 scripts/loop_guard.py --loop P20-wiki-ontology-fusion --memory-only
python3 scripts/loop_guard.py --loop P20-wiki-ontology-fusion --evidence-only

# 若需将状态转 ready_for_independent_qa，由 QA 流程确认后执行 qa_attest
python3 scripts/qa_attest.py --loop P20-wiki-ontology-fusion --decision pass --actor <independent_qa_id>
```

独立 QA 核对清单：最终 HEAD；6/6 全量 Gate；P1 安全与覆盖率治理；P3 Shadow E2E；P4 合同索引；未关闭 MINOR；证据散列；工作区状态；P19 工作区未修改。

> 注意：`qa_attest.py` 需 `STATE.json.status == "ready_for_independent_qa"`。该状态转换应由独立 QA 流程在确认全部条件满足后触发，开发角色不擅自转。
