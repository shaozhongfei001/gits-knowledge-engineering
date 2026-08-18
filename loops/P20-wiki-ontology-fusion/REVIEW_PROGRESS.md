# P20 Wiki-first × Ontology 融合｜评审结论后进展与多阶段开发计划（REVIEW PROGRESS）

```text
DOC_ID=REVIEW-P20-PROGRESS-003
STATUS=REVIEW_PENDING（Owner 已授权有限基线治理；正式 QA Attestation 待 P1/P3/P4 完成后）
AUTHOR=tech_lead（orchestrator）
CREATED_AT=2026-08-18
UPDATED_AT=2026-08-18
REVIEW_HEAD=47169aed00d6c010afd1c0eb056e7b010e2c3749
PRE_ATTESTATION_REVIEW_VERDICT=PASS_WITH_ISSUES（独立 QA 交叉预审，无 BLOCKER/MAJOR）
FORMAL_QA_ATTESTATION=PENDING
SUPERSEDES=loops/P20-wiki-ontology-fusion/REVIEW_PENDING.md（v1）与 REVIEW_PROGRESS v2（评审前阶段顺序）
LOOP=loops/P20-wiki-ontology-fusion
DISPATCH=docs/dispatch/P20-wiki-ontology-fusion.md
EVIDENCE=loops/P20-wiki-ontology-fusion/EVIDENCE.md
QA_REVIEW_REPORT=loops/P20-wiki-ontology-fusion/evidence/qa-review/qa-review-report.md
BASELINE_GOVERNANCE=loops/P20-wiki-ontology-fusion/BASELINE_GOVERNANCE_PLAN.md
```

> 本文档用于提交下一轮交叉评审（Tech Lead / Feature Pilot / E2E Owner / Independent QA）。
> 请评审人按文末「评审提示词」逐项核查；结论回写本文档或 `HANDOFF.md`。

---

## 1. 评审结论（已下达，预审性质）

```text
PRE_ATTESTATION_REVIEW_VERDICT=PASS_WITH_ISSUES（独立 QA 交叉预审，BLOCKERS=（无）、MAJOR=（无））
FORMAL_QA_ATTESTATION=PENDING
```

- **独立 QA 交叉预审结论**：`PASS_WITH_ISSUES`，`BLOCKERS=（无）`、`MAJOR=（无）`。
- **合同合规 / 分层 / fail-closed / 测试充分 / 越界审计 / 证据纪律 / 路径安全**：均 PASS。
- **MINOR-2（路由歧义 fail-open）**：已修复（commit `78e5372`，`RoutePolicy.findRule` 对同优先级并列返回空，fail-closed）。
- **其余 MINOR（1/3/4/5）**：记录为 refinement，不阻断授权。
- 详见 `evidence/qa-review/qa-review-report.md`。
- **性质说明**：此为 **独立 QA 交叉预审（PRE_ATTESTATION）**，不构成正式 QA Attestation。正式 attestation 必须针对 P1/P3/P4 完成、6/6 Gate 通过后的最终候选 HEAD 由独立 QA Actor 单独执行。

## 2. 评审结论下达后的工作进展（As-Is）

> 评审前已完成：工具链可复现（EV-P20-ENV-003）、Route & Activation shadow slice（EV-P20-ROUTE-004）、两场景 Shadow E2E + 安全门禁（EV-P20-E2E-005）。

| 工作包 | 证据 | 提交 | 验证 |
|---|---|---|---|
| **Gap G3 受控语义查询**（评审后） | EV-P20-SQ-006 | `461f7c8` | semantic-runtime 16 测试；任意 SPARQL/未注册 → `DENY_ONLY_REGISTERED_QUERY_ID`（SEC-005） |
| **Gap G4 上下文装配**（评审后） | EV-P20-CTX-007 | `ac5472c` | context-evidence 19 测试；计划驱动装配生成非空 EvidenceBundle（权限+来源完整） |
| **QA MINOR-2 修复**（评审后） | EV-P20-QA-008 | `78e5372` | knowledge-architecture 19 测试；路由歧义 fail-closed |
| **独立 QA 评审**（评审后） | EV-P20-QA-008 | `eb5ed51` | PASS_WITH_ISSUES |
| **基线治理计划**（评审后，待授权） | BASELINE_GOVERNANCE_PLAN.md | `1444cfd` | 两阻断处置选项待 Owner 决策 |

**当前 Loop 状态**（保守，未声明最终 READY_FOR_INDEPENDENT_QA）：
```text
status=in_progress
implementation_state=DEV_SELF_CHECK_PASS
formal_qa_attestation=pending
gate_readiness=pending_baseline_governance
```
Baton=`feature_pilot`、`implementation_actor=feature_pilot`、`qa_actor=null`。

**正式 Gate 通过（5/6）**：`contract_generate` / `contract_check` / `knowledge_architecture_check` / `security_check` / `shadow_e2e`（hash-attested）。

**正式 Gate 未通过（1/6）**：`backend_test`（因基线 OWASP npm nanoid + apps/api JaCoCo 覆盖率 0.69<0.80）。

> 在 `backend_test` 通过、P3/P4 完成之前，**不得**声明最终 `READY_FOR_INDEPENDENT_QA`。

### 关键协调阻断

```text
loop_guard 强制 ready_for_independent_qa 需 all gates pass（line 116）。
→ backend_test（基线治理）是独立 QA attestation 的前置。
→ 基线治理超出 P20 shadow 授权范围，需 Owner 明确授权。
```

## 3. 下一步多阶段详细开发计划（Next Plan）

> 全部为 SHADOW；不切换 FUSION、不迁移生产、不执行生产写回、不改变现有业务行为。

### 阶段 P1：基线治理（Owner 已授权 APPROVE_P20_BASELINE_GOVERNANCE_LIMITED）

| 子项 | 处置（Owner 决定） | 影响面 |
|---|---|---|
| P1a OWASP npm nanoid | 优先升级父依赖；无兼容升级再评估最小 overrides；不豁免 | 根 `package-lock.json` / `package.json` |
| P1b apps/api JaCoCo 覆盖率 | `ADD_TESTS`（`COVERAGE_THRESHOLD_REDUCTION=NOT_AUTHORIZED`），目标 ≥0.80 | `apps/api` 测试 |

- 通过标准：`make backend-test` → PASS（全反应堆 Java 测试 + dependency-check 无阻断）。

### 阶段 P3：两场景 Shadow E2E 深化（Gap G5 收尾）

- 在 `run_p20_shadow_e2e.py` 基础上补充：重复运行结果确定且一致（可重放）；production mode 拒绝；production writeback 拒绝；第三场景拒绝；非法/空值/空白/不支持枚举 fail-closed；正式输出未变化。
- 通过标准：两场景 shadow 证据可重放；`formal_output_changed=False`、`production_side_effect=False`、`production_writeback=False`。

### 阶段 P4：合同索引与生成物最终核对

- 核对新增 Port/record 对应合同（CTR-KMAP/ASSET/ACTIVATION/ROUTE/PLAN/SEMANTIC/CTX）登记到 `specs/CONTRACT_INDEX.yaml`。
- 运行 `make generate` → 确认 `generated/` 无手工改动；实现偏离必须先修实现，不得反向编造合同。
- 通过标准：`make check` 全绿；合同哈希一致。

### 阶段 R：全量回归（P1/P3/P4 完成后，同一候选 HEAD）

- 运行 `make generate` / `make check` / `make framework-test` / `make tooling-test` / `make security-check` / `make backend-test` / loop memory+evidence guard / shadow e2e。
- 通过条件：`FORMAL_GATES=6/6_PASS`、`backend_test=PASS`、`shadow_e2e=PASS`、`formal_output_changed=False`、loop guards PASS。
- 62 项 secret-scan advisory 如仍存在如实报告。

### 阶段 P2：正式独立 QA Attestation

- **前置**：P1、P3、P4、阶段 R 全部完成；6/6 Gate 通过；工作区干净；证据哈希与最终候选 HEAD 一致；loop guards 通过；无 BLOCKER/MAJOR；未执行生产切换。
- 随后将 `STATE.json` 转 `ready_for_independent_qa`，由**独立 QA Actor** 对最终 HEAD 评审并执行 `python3 scripts/qa_attest.py --loop P20-wiki-ontology-fusion --decision pass --actor <independent_qa>`。
- 开发角色不得自签。
- **原因**：P3/P4 含测试/代码/合同/生成物变更；若在 QA Attestation 后修改，QA 签署的 HEAD 与证据哈希将失效。

### 阶段 P5：Owner 审批与受控合并

- Owner 审查正式 QA Attestation + 全量回归报告。
- Owner 批准仅表示"允许将 P20 shadow implementation 受控合并到指定目标分支"；不表示 production fusion / cutover / writeback / 生产就绪 / 冻结。

### 阶段依赖

```text
P1（基线治理，Owner 已授权）
  → P3、P4（可并行）
  → 阶段 R（全量回归，6/6 Gate）
  → P2（正式独立 QA Attestation，针对最终候选 HEAD）
  → P5（Owner 审批与受控合并）
```

## 4. 通过标准（Acceptance Criteria，评审后更新）

| # | 标准 | 验证命令 | 达标要求 |
|---|---|---|---|
| AC-1 | 合同与生成物一致 | `make check` | contract-check、enum-consistency、semantic-rule-gate 全 PASS |
| AC-2 | 知识架构可校验 | `validate_knowledge_architecture.py` | 6 schemas、4 maps、20 assets、5 skills、2 activations、1 route、2 plans fail-closed |
| AC-3 | 路由黄金用例 | 同上路由用例 | ROUTE-001~006 期望决策全部命中；歧义 route fail-closed |
| AC-4 | 计划确定性与交叉引用 | planner 集成测试 | 两场景 ActivationPlan 确定性字段与黄金一致；planHash 可重放 |
| AC-5 | 语义查询受控 | semantic-runtime 测试 | 注册 CQ 可执行；任意 SPARQL → `DENY_ONLY_REGISTERED_QUERY_ID` |
| AC-6 | 上下文非空 | context-evidence 测试 | EvidenceBundle 非空；权限与来源完整；权限未决不返回 |
| AC-7 | 后端回归 | `make backend-test` | 全量 Java 测试通过（P1 后）；行覆盖率 ≥80% |
| AC-8 | 安全回归 | `make security-check` | secret-scan、sensitive-permissions、oracle guard 通过（62 advisories 如实报告） |
| AC-9 | 两场景 Shadow E2E | `run_p20_shadow_e2e.py` | 差异报告产出；正式输出 diff 为空 |
| AC-10 | 独立 QA | `qa_attest.py` | 仅独立 QA 记录 `qa_pass`；开发仅 `DEV_SELF_CHECK_PASS` |

## 5. 已明确排除（Out of Scope）

- 真实 RAG / OpenSPG / GraphDB / OpenMetadata / Oracle 接通；
- 全量业务模块迁移、删除 Legacy、生产切换；
- 自动 CRM 写回；
- 开发自签独立 QA、复用 P19 证据或签署；
- 未经 Owner 批准从 SHADOW 切到 FUSION；
- 未经 Owner 授权修改 `frontend/`、根 `package-lock.json`、`apps/api`（P1 基线治理）。

---

## 6. 汇报 + 申请评审提示词（请评审 Agent 使用）

### 6.1 进展汇报（可直接转述给 Owner/上级）

```text
P20 独立 QA 交叉预审结论（PRE_ATTESTATION_REVIEW_VERDICT=PASS_WITH_ISSUES）已下达，无 BLOCKER/MAJOR；
FORMAL_QA_ATTESTATION=PENDING。评审后的 Gap G3/G4、路由歧义修复、shadow e2e、security gate 均已
DEV_SELF_CHECK 通过并提交。当前 5/6 正式 Loop Gate 通过；唯一未通过为 backend_test，源于预存基线
（OWASP npm nanoid + apps/api 覆盖率）。Owner 已授权有限基线治理（P1）。执行顺序：P1 基线治理 →
P3/P4（可并行）→ 全量回归 6/6 → P2 正式独立 QA Attestation（针对最终候选 HEAD）→ P5 Owner 受控合并。
正式 QA Attestation 前不声明最终 READY_FOR_INDEPENDENT_QA。
```

### 6.2 申请评审提示词（请评审 Agent 使用）

```text
你是 P20 独立评审 Agent（只读，禁止修改代码）。请交叉评审
loops/P20-wiki-ontology-fusion/REVIEW_PROGRESS.md 所述的评审后进展、P1 基线治理与 P3/P4 结果
（模块：modules/knowledge-architecture、adapters/knowledge-filesystem、modules/semantic-runtime、
modules/context-evidence、apps/api；脚本：scripts/run_p20_shadow_e2e.py；根 package-lock.json）。

请按以下清单输出结构化评审结论（此为预审，非正式 QA Attestation）：

1. 评审后变更核验：
   - 独立 QA 交叉预审结论 PASS_WITH_ISSUES 是否如实采纳？MINOR-2 修复是否真实有效？
   - 其余 MINOR（1/3/4/5）是否恰当记录而非掩盖？
2. 新增工作包：
   - Gap G3 语义查询：注册 ID 可执行、任意 SPARQL 拒绝，是否满足 SEC-005？
   - Gap G4 上下文装配：EvidenceBundle 是否非空、权限与来源完整、权限未决 fail-closed？
   - 路由歧义修复：同优先级并列 fail-closed？
3. P1 基线治理：
   - P1a nanoid：是否最小范围升级父依赖/overrides？是否未经授权豁免或关闭 OWASP？
   - P1b apps/api：是否仅补有效断言测试？是否未调低覆盖率门槛/未排除核心类/未删失败测试？
4. P3 Shadow E2E：重复运行一致、planHash 可重放、production/写回/第三场景/空值/非法枚举 fail-closed、
   formal_output_changed=False？
5. P4 合同索引：新增 Port/record/schema 是否均有受控合同依据？是否反向编造合同？generated/ 是否仅由生成器产生？
6. 合同合规与分层越界：record/Port 字段是否与 schema 一致？是否未启用 fusion/迁移生产/写回/改现有行为？
7. 证据纪律：是否把 DEV_SELF_CHECK 误标为 QA_PASS？EVIDENCE.json 是否 hash-attested？FAILURES.md append-only？
8. 协调与风险：backend_test 基线阻断是否已如实治理并 6/6？正式 QA Attestation 前置（P2 针对最终候选 HEAD）是否成立？

结论输出格式：
REVIEW_VERDICT=PASS / PASS_WITH_ISSUES / BLOCKED
BLOCKERS=...
MAJOR=...
MINOR=...
NOTE=...
```

---

## 7. 变更记录

| 版本 | 时间 | 说明 |
|---|---|---|
| v1 | 2026-08-18 | 评审前状态（REVIEW_PENDING.md） |
| v2 | 2026-08-18 | 评审结论后进展 + 多阶段开发计划（原 P1→P2→P3/P4→P5）+ 汇报/评审提示词 |
| v3 | 2026-08-18 | Owner 授权有限基线治理后：修订阶段顺序为 P1→P3/P4→阶段R→P2→P5；修正状态语义为 PRE_ATTESTATION/PENDING；明确独立 QA 交叉预审性质 |
