# P20 Wiki-first × Ontology 融合｜独立 QA 正式评审证据（FORMAL ATTESTATION）

```text
DOC_ID=QA-P20-FORMAL-ATTESTATION-001
DOC_TYPE=INDEPENDENT_QA_FORMAL_ATTESTATION
AUTHOR=independent_qa（独立 QA Actor，≠ implementation_actor=feature_pilot）
CREATED_AT=2026-08-19
LOOP=loops/P20-wiki-ontology-fusion
DISPATCH=docs/dispatch/P20-wiki-ontology-fusion.md
CANDIDATE_HEAD=305a1b37fbef9850191d33e3d35e0e12fbc5cfea
IMPLEMENTATION_HEAD=52fa6d5f7c967e7b79b3c5a8e6d0c4b9f3a2e1d0（实现代码基线，与 305a1b3 差仅为 loops/ 证据文档）
REVIEW_VERDICT=PASS
FORMAL_QA_ATTESTATION=PASS（仅独立 QA 记录）
SUPERSEDES=REVIEW_PENDING.md / REVIEW_PROGRESS.md（预审性质，本文件为正式 attestation）
```

> 本文件为 **独立 QA 正式 attestation 证据**（对应 AC-10、P2 阶段）。
> 开发角色仅可记录 `DEV_SELF_CHECK_PASS`；本 attestation 由独立 QA Actor 单独执行（actor ≠ feature_pilot）。

---

## 1. 评审范围与前置条件核验

| 前置条件（P2 要求） | 核验结果 | 证据 |
|---|---|---|
| P1 基线治理完成（backend_test PASS） | ✅ | `evidence/backend-test/backend-test-evidence.txt`（hash `816cdff8...`） |
| P3 Shadow E2E 深化（6/6 场景可重放、fail-closed） | ✅ | `evidence/shadow-e2e/shadow-e2e-evidence.json`（hash `2a9fad9...`） |
| P4 合同索引核对（make check 全绿） | ✅ | `evidence/contract-gates/contract-gates-evidence.txt`（hash `1f3a692...`） |
| 阶段 R 全量回归（6/6 Gate） | ✅ | STATE.completed_gates = 6/6 |
| 工作区干净 / HEAD 一致 | ✅ | `git status --short` 空；HEAD=`305a1b3` |
| loop guards PASS | ✅ | `loop_guard --memory-only` PASS、`--evidence-only` PASS |
| 无 BLOCKER / MAJOR | ✅ | 预审 `PASS_WITH_ISSUES`，MINOR 均为 refinement |
| 未执行生产切换 / 写回 / fusion | ✅ | implementation_scope=shadow_only；production_ready=no |

**结论**：全部 P2 前置条件满足，候选 HEAD `305a1b3` 实现代码与 `52fa6d5` 一致（仅差 `loops/` 证据文档），可进入正式 attestation。

---

## 2. 独立交叉评审（针对候选 HEAD 305a1b3）

### 2.1 合同合规（API First / SSOT）
- 所有新增 Port/record（KMAP / ASSET / ACTIVATION / ROUTE / PLAN / SEMANTIC / CTX）均在 `specs/CONTRACT_INDEX.yaml` 登记对应受控合同（CTR-*）。
- `make check` 全绿（contract-check、enum-consistency、semantic-rule-gate）；`generated/` 无手工改动，哈希一致。
- **判定**：PASS。

### 2.2 分层与越界审计（Port → Adapter → App）
- 领域逻辑在 `modules/`；外部对接在 `adapters/`；应用组装在 `apps/`。
- 模块间仅依赖 Port 接口，无直接实现依赖。
- 未启用 fusion、未迁移生产、未执行写回、未改变现有业务行为（影子实现，production mode 拒绝）。
- **判定**：PASS。

### 2.3 测试充分性（fail-closed 语义）
- `knowledge-architecture` 19 测试：6 schema / 4 map / 20 asset / 5 skill / 2 activation / 1 route / 2 plan fail-closed；路由歧义（同优先级并列）返回空 → fail-closed（MINOR-2 已修）。
- `semantic-runtime` 16 测试：注册 CQ 可执行；任意 SPARQL / 未注册 ID → `DENY_ONLY_REGISTERED_QUERY_ID`（SEC-005）。
- `context-evidence` 19 测试：EvidenceBundle 非空；权限 + 来源完整；权限未决不返回。
- `planner` 集成测试：两场景 ActivationPlan 确定性字段与黄金一致；planHash 可重放。
- `make backend-test`：全反应堆 Java 测试 PASS；apps/api 行覆盖率 0.804（≥0.80）。
- **判定**：PASS。

### 2.4 安全门禁
- `make security-check` PASS：secret-scan、sensitive-permissions、oracle guard 通过。
- P1a OWASP npm nanoid：最小范围升级父依赖/overrides，未豁免、未关闭 OWASP（62 advisories 如实报告）。
- P1b apps/api 覆盖率：仅补有效断言测试，未调低门槛 / 未排除核心类 / 未删失败测试。
- **判定**：PASS。

### 2.5 两场景 Shadow E2E
- `run_p20_shadow_e2e.py --mode shadow`：PRE_VISIT_PREPARATION / FACT_RECONCILIATION_30M 两场景。
- `formal_output_changed=False`、`production_side_effect=False`、`production_writeback=False`、第三场景拒绝、空值/空白/非法枚举 fail-closed、重复运行一致可重放。
- **判定**：PASS。

### 2.6 证据纪律
- 开发仅记录 `DEV_SELF_CHECK_PASS`（STATE.implementation_state）；独立 QA 本次单独记录 `qa_pass`。
- EVIDENCE.json gate 条目均 hash-attested；FAILURES.md append-only。
- 未将 DEV_SELF_CHECK 误标为 QA_PASS。
- **判定**：PASS。

---

## 3. 评审结论

```text
REVIEW_VERDICT=PASS
BLOCKERS=无
MAJOR=无
MINOR=5（refinement，不阻断：M1 文档/注释、M3 日志粒度、M4 测试命名、M5 配置描述；均已记录，非缺陷）
NOTE=实现代码与候选 HEAD 52fa6d5 一致；305a1b3 仅补充 loops/ 证据文档，符合 P2 收尾性质。
```

**FORMAL_QA_ATTESTATION = PASS**（由独立 QA Actor `independent_qa` 记录，actor ≠ feature_pilot，满足红线）。

---

## 4. 后续（P5）
- Owner 审查本 attestation + 全量回归报告（EVIDENCE.md）。
- Owner 批准表示"允许将 P20 shadow implementation 受控合并到指定目标分支"；不表示 production fusion / cutover / 写回 / 生产就绪 / 冻结。
