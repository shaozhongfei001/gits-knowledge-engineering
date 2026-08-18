# P20 Wiki-first × Ontology 融合｜评审结论后进展与多阶段开发计划（REVIEW PROGRESS）

```text
DOC_ID=REVIEW-P20-PROGRESS-002
STATUS=REVIEW_PENDING（评审结论已下达，待下一轮评审）
AUTHOR=tech_lead（orchestrator）
CREATED_AT=2026-08-18
REVIEW_HEAD=1444cfd71826dd46ee3ce5254197df76c3dbe56b
REVIEW_VERDICT=PASS_WITH_ISSUES（独立 QA 交叉评审，无 BLOCKER/MAJOR）
SUPERSEDES=loops/P20-wiki-ontology-fusion/REVIEW_PENDING.md（v1，评审前状态）
LOOP=loops/P20-wiki-ontology-fusion
DISPATCH=docs/dispatch/P20-wiki-ontology-fusion.md
EVIDENCE=loops/P20-wiki-ontology-fusion/EVIDENCE.md
QA_REVIEW_REPORT=loops/P20-wiki-ontology-fusion/evidence/qa-review/qa-review-report.md
BASELINE_GOVERNANCE=loops/P20-wiki-ontology-fusion/BASELINE_GOVERNANCE_PLAN.md
```

> 本文档用于提交下一轮交叉评审（Tech Lead / Feature Pilot / E2E Owner / Independent QA）。
> 请评审人按文末「评审提示词」逐项核查；结论回写本文档或 `HANDOFF.md`。

---

## 1. 评审结论（已下达）

**独立 QA 交叉评审结论**：`PASS_WITH_ISSUES`，`BLOCKERS=（无）`、`MAJOR=（无）`。

- **合同合规 / 分层 / fail-closed / 测试充分 / 越界审计 / 证据纪律 / 路径安全**：均 PASS。
- **MINOR-2（路由歧义 fail-open）**：已修复（commit `78e5372`，`RoutePolicy.findRule` 对同优先级并列返回空，fail-closed）。
- **其余 MINOR（1/3/4/5）**：记录为 refinement，不阻断授权。
- 详见 `evidence/qa-review/qa-review-report.md`。

## 2. 评审结论下达后的工作进展（As-Is）

> 评审前已完成：工具链可复现（EV-P20-ENV-003）、Route & Activation shadow slice（EV-P20-ROUTE-004）、两场景 Shadow E2E + 安全门禁（EV-P20-E2E-005）。

| 工作包 | 证据 | 提交 | 验证 |
|---|---|---|---|
| **Gap G3 受控语义查询**（评审后） | EV-P20-SQ-006 | `461f7c8` | semantic-runtime 16 测试；任意 SPARQL/未注册 → `DENY_ONLY_REGISTERED_QUERY_ID`（SEC-005） |
| **Gap G4 上下文装配**（评审后） | EV-P20-CTX-007 | `ac5472c` | context-evidence 19 测试；计划驱动装配生成非空 EvidenceBundle（权限+来源完整） |
| **QA MINOR-2 修复**（评审后） | EV-P20-QA-008 | `78e5372` | knowledge-architecture 19 测试；路由歧义 fail-closed |
| **独立 QA 评审**（评审后） | EV-P20-QA-008 | `eb5ed51` | PASS_WITH_ISSUES |
| **基线治理计划**（评审后，待授权） | BASELINE_GOVERNANCE_PLAN.md | `1444cfd` | 两阻断处置选项待 Owner 决策 |

**当前 Loop 状态**：`status=in_progress`、`implementation_state=DEV_SELF_CHECK_PASS`、`gate_decision=READY_FOR_INDEPENDENT_QA`、Baton=`feature_pilot`、`implementation_actor=feature_pilot`、`qa_actor=null`。

**正式 Gate 通过**：`contract_generate` / `contract_check` / `knowledge_architecture_check` / `security_check` / `shadow_e2e`（hash-attested）。

**正式 Gate 未通过**：`backend_test`（因基线 OWASP npm nanoid + apps/api JaCoCo 覆盖率 0.69<0.80）。

### 关键协调阻断

```text
loop_guard 强制 ready_for_independent_qa 需 all gates pass（line 116）。
→ backend_test（基线治理）是独立 QA attestation 的前置。
→ 基线治理超出 P20 shadow 授权范围，需 Owner 明确授权。
```

## 3. 下一步多阶段详细开发计划（Next Plan）

> 全部为 SHADOW；不切换 FUSION、不迁移生产、不执行生产写回、不改变现有业务行为。

### 阶段 P1：基线治理（前置，需 Owner 授权）

| 子项 | 处置 | 影响面 |
|---|---|---|
| P1a OWASP npm nanoid | 选项：`docx` 升级 / npm overrides / 豁免 | 根 `package-lock.json` / `package.json` |
| P1b apps/api JaCoCo 覆盖率 | 选项：补测试 / 调门槛（不推荐） | `apps/api` 测试 + `pom.xml` |

- 通过标准：`make backend-test` → PASS（全反应堆 Java 测试 + dependency-check 无阻断）。

### 阶段 P2：独立 QA Attestation

- 前置：P1 后 `backend_test` 全绿 → `STATE.json` 转 `ready_for_independent_qa`。
- 执行：独立 QA Actor 运行 `python3 scripts/qa_attest.py --loop P20-wiki-ontology-fusion --decision pass --actor <independent_qa>`。
- 通过标准：`qa_attest.py` 记录 `qa_pass`；仅独立 QA Actor 可签署；开发角色不得自签。

### 阶段 P3：两场景 Shadow E2E 深化（Gap G5 收尾）

- 在 `run_p20_shadow_e2e.py` 基础上补充：重复运行结果确定且一致（可重放）；非法输入（production/写回/第三场景）fail-closed 已覆盖。
- 通过标准：两场景 shadow 证据可重放；`formal_output_changed=False`。

### 阶段 P4：合同索引与生成物最终核对

- 将新增 Port/record 对应合同（CTR-KMAP/ASSET/ACTIVATION/ROUTE/PLAN/SEMANTIC）注册到 `specs/CONTRACT_INDEX.yaml`。
- 运行 `make generate` → 确认 `generated/` 无手工改动。
- 通过标准：`make check` 全绿；合同哈希一致。

### 阶段 P5：Owner 审批与关闭

- Owner 审查独立 QA `qa_pass` + 全量回归报告。
- 通过标准：Owner 批准后可 merge 到主线；在此之前不得启用 fusion / production cutover / 生产切换。

### 阶段依赖

```text
P1（基线治理，Owner 授权）
  → P2（backend_test 全绿 → ready_for_independent_qa → 独立 QA attest）
  → P3/P4（可并行）
  → P5（Owner 审批关闭）
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
P20 评审结论（PASS_WITH_ISSUES）已下达，无 BLOCKER/MAJOR；评审后的 Gap G3/G4、路由歧义修复、
shadow e2e、security gate 均已 DEV_SELF_CHECK 通过并提交。当前 5/6 正式 Loop Gate 通过；
唯一未通过为 backend_test，源于预存基线（OWASP npm nanoid + apps/api 覆盖率），已提交基线治理计划待 Owner 授权。
独立 QA attestation 需 backend_test 全绿（loop_guard 强制 all gates pass）后方可进行。
```

### 6.2 申请评审提示词（请评审 Agent 使用）

```text
你是 P20 独立评审 Agent（只读，禁止修改代码）。请交叉评审
loops/P20-wiki-ontology-fusion/REVIEW_PROGRESS.md 所述的评审后进展与 shadow 实现
（模块：modules/knowledge-architecture、adapters/knowledge-filesystem、modules/semantic-runtime、
modules/context-evidence；脚本：scripts/run_p20_shadow_e2e.py）。

请按以下清单输出结构化评审结论：

1. 评审后变更核验：
   - 独立 QA 结论 PASS_WITH_ISSUES 是否已被如实采纳？MINOR-2 修复是否真实有效？
   - 其余 MINOR（1/3/4/5）是否被恰当记录而非掩盖？
2. 新增工作包：
   - Gap G3 语义查询：注册 ID 可执行、任意 SPARQL 拒绝，是否满足 SEC-005？
   - Gap G4 上下文装配：EvidenceBundle 是否非空、权限与来源是否完整、权限未决是否 fail-closed？
   - 路由歧义修复：同优先级并列是否 fail-closed？
3. 合同合规：record/Port 字段是否与 specs/knowledge-architecture/schemas/*.json 一致？是否发明合同未定义字段？
4. 分层与越界：是否遵循 Port→Adapter→App？是否未启用 fusion/迁移生产/写回/改现有行为？
5. 测试充分性：单元+集成是否覆盖通过与全部拒绝路径？是否用真实合同数据比对黄金确定性字段？
6. 证据纪律：是否把 DEV_SELF_CHECK 误标为 QA_PASS？FAILURES.md 是否 append-only？EVIDENCE.json 是否 hash-attested？
7. 协调与风险：backend_test 基线阻断是否如实记录？P1 基线治理计划是否合理？
   P2 独立 QA attestation 前置是否成立？

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
| v2 | 2026-08-18 | 本文件：评审结论后进展 + 多阶段开发计划（P1~P5）+ 汇报/评审提示词 |
