# P20 Wiki-first × Ontology 融合｜待评审汇总（REVIEW PENDING）

```text
DOC_ID=REVIEW-P20-INFRA-001
STATUS=REVIEW_PENDING
AUTHOR=tech_lead
CREATED_AT=2026-08-18
BASELINE_COMMIT=8e120ad5c2e1ea465acdd5b180f7f6cc59d8e2f1
LOOP=loops/P20-wiki-ontology-fusion
DISPATCH=docs/dispatch/P20-wiki-ontology-fusion.md
LOOP_GATE_FILE=loops/P20-wiki-ontology-fusion/LOOP.yaml
EVIDENCE=loops/P20-wiki-ontology-fusion/EVIDENCE.md
FAILURES=loops/P20-wiki-ontology-fusion/FAILURES.md
```

> 本文档用于提交其他智能体（Tech Lead / Feature Pilot / E2E Owner / Independent QA）交叉评审。
> 请评审人按文末「交叉评审提示词」逐项核查，并将结论记录到 `REVIEW_PENDING.md` 或回写 `HANDOFF.md`。

---

## 1. 当前情况（As-Is）

### 1.1 Loop 状态

- **LOOP_ID**: `P20-wiki-ontology-fusion`
- **Dispatch 状态**: `PROPOSED_PENDING_OWNER_AUTHORIZATION`
- **STATE.json**: `status=planned`，`current_phase=contract_candidate_review`，`blocked=true`，`blocking_reason=OWNER_AUTHORIZATION_PENDING`
- **Baton holder**: `tech_lead`（仅维护合同候选；开发 Actor 与独立 QA Actor 均未正式分配）
- **正式 Loop Gate**: 全部 `pending`（`contract_generate` / `contract_check` / `knowledge_architecture_check` / `backend_test` / `security_check` / `shadow_e2e`）

### 1.2 已完成（shadow 基础设施两批，DEV_SELF_CHECK，非 QA_PASS）

| 批次 | 交付物 | 位置 | 验证 |
|---|---|---|---|
| EV-P20-INFRA-001 | 4 Port 接口 + 4 领域 record + 4 filesystem readers + fail-closed 校验 | `modules/knowledge-architecture`、`adapters/knowledge-filesystem` | 单元测试 27/27 |
| EV-P20-INFRA-002 | ActivationPlan + PlanDecision + DefaultActivationPlanner | `modules/knowledge-architecture` | 单测 7/7 + 集成 3/3 |

**具体实现**
- **Ports**: `KnowledgeMapPort`、`AssetCatalogPort`、`ActivationContractPort`、`RoutePolicyPort`、`ActivationPlannerPort`
- **领域 record**: `KnowledgeMap`、`AssetManifest`、`ActivationContract`、`RoutePolicy`、`ActivationPlan`（字段与对应 JSON Schema 合同一致）
- **Readers**: 支持纯 JSON 与两种 Markdown 前导布局（`---{json}---` 单行内联、`---\n{json}\n---` 标准 frontmatter），四类资产目录扫描
- **Planner**: Route Policy → Activation Contract → Asset Catalog → 地图，汇合为可重放 `ActivationPlan`

### 1.3 Fail-closed 决策矩阵（对齐 routing / negative-security 用例）

| 条件 | 决策码 | 对应用例 |
|---|---|---|
| 任务未映射 / taskType 为空 | `DENY_UNMAPPED_TASK` | ROUTE-005 |
| 合同引用 `AC-NOT-IN-P20` | `DENY_NOT_IN_P20` | ROUTE-003/004 |
| 权限未决 / 缺失 | `DENY_PERMISSION_PENDING` | ROUTE-006、SEC-001/002 |
| 激活资产未在 Asset Manifest 登记 | `DENY`（不返回部分计划） | SEC-003 |
| 解析失败 / 必需字段缺失（reader 层） | `Optional.empty` / 跳过，不抛异常 | — |

### 1.4 已验证目标

- `make check` → **PASS**（contract-check、knowledge-architecture-check、loop-guard、secret-scan、enum-consistency、semantic-rule-gate 全通过）
- `make framework-test` → **3/3 OK**
- `make tooling-test` → **14/14 OK**
- `mvn -q compile`（全反应堆）→ **EXIT 0**
- 两个 P20 场景（`PRE_VISIT_PREPARATION` / `FACT_RECONCILIATION_30M`）生成的 ActivationPlan 与黄金计划 `AP-PREVISIT-GOLDEN` / `AP-FACT-RECON-GOLDEN` 确定性字段一致

### 1.5 环境阻断（已解除）

- `ENV-P20-G0-001`: `make check` / `make tooling-test` 曾因 `gits-kno-p20-venv` 缺 PyYAML 阻断；已由默认 `python3`（含 yaml）解决，`tooling-test` 现 14/14 通过。记录见 `FAILURES.md`（append-only）。

---

## 2. 下一步开发计划（Next Plan）

> 按 Loop 范围与 Dispatch Gap 分解排列，**全部为 SHADOW，不切换 FUSION，不迁移生产，不修改现有业务行为**。

### 里程碑 M1：补齐语义查询 Gate（Gap G3）

- 新增 `SemanticQueryPort` 与注册表：仅允许注册的 Semantic Query ID 执行，任意 SPARQL 一律拒绝（SEC-005）。
- 在 `modules/knowledge-architecture` 或 `adapters/knowledge-filesystem` 落地，绑定黄金用例。
- 通过标准：注册 CQ 可执行；任意 SPARQL 返回 `DENY_ONLY_REGISTERED_QUERY_ID`。

### 里程碑 M2：上下文装配 Gate（Gap G4）

- 新增 `ContextPackagePort`：在 ActivationPlan 基础上装配 EvidenceBundle，权限与来源完整，`EvidenceBundle` 不再为空。
- 复用 `modules/context-evidence` 既有 Port（不迁移、不修改现有行为）。
- 通过标准：ContextPackage 含 permissionDecisionId 与 sourceVersion；权限未决不返回上下文。

### 里程碑 M3：两场景 Shadow E2E（Gap G5，正式 Gate `shadow_e2e`）

- 编写 `scripts/run_p20_shadow_e2e.py --mode shadow --scenario PRE_VISIT_PREPARATION --scenario FACT_RECONCILIATION_30M`。
- 新旧链路并行，输出差异报告，**不改变正式输出**。
- 通过标准：两场景产出可重放的 shadow 证据；正式业务输出 diff 为空。

### 里程碑 M4：回归与 QA 包（Gap G6）

- 执行 `make backend-test`、`make security-check`、`make verify` 全量回归。
- 整理回归报告与负向安全用例结果，交独立 QA。
- 通过标准：No-Go 用例、安全负向用例与原链路回归全部通过；独立 QA 方可记录 `QA_PASS`。

### 依赖与门禁

- M1～M4 需先由 Owner 批准 P20 Shadow 实现（解除 `OWNER_AUTHORIZATION_PENDING`）。
- 正式 Loop Gate 逐项推进，每个 Gate 完成后更新 `STATE.json` / `EVIDENCE.md`。

---

## 3. 通过标准（Acceptance Criteria）

> 下列为从 LOOP.yaml gates + Dispatch Gaps 提取的可核查标准。**行覆盖率 ≥ 80%**、`make check` 全绿为硬性门槛。

| # | 标准 | 验证命令 | 达标要求 |
|---|---|---|---|
| AC-1 | 合同与生成物一致 | `make check` | contract-check、enum-consistency、semantic-rule-gate 全部 PASS |
| AC-2 | 知识架构可校验 | `python3 scripts/validate_knowledge_architecture.py` | 6 schemas、4 maps、20 assets、5 skills、2 activations、1 route、2 plans 通过 fail-closed 校验 |
| AC-3 | 路由黄金用例通过 | `validate_knowledge_architecture` 路由用例 | ROUTE-001～006 期望决策全部命中 |
| AC-4 | 计划确定性与交叉引用 | planner 集成测试 | 两场景 ActivationPlan 确定性字段与黄金一致；planHash 可重放 |
| AC-5 | 语义查询受控 | M1 后 | 注册 CQ 可执行；任意 SPARQL → `DENY_ONLY_REGISTERED_QUERY_ID` |
| AC-6 | 上下文非空 | M2 后 | EvidenceBundle 非空；权限与来源完整；权限未决不返回上下文 |
| AC-7 | 后端回归 | `make backend-test` | 全量 Java 测试通过，行覆盖率 ≥ 80% |
| AC-8 | 安全回归 | `make security-check` | secret-scan、sensitive-permissions、quarantine guard 通过 |
| AC-9 | 两场景 Shadow E2E | `make verify` | 差异报告产出；正式输出 diff 为空 |
| AC-10 | 独立 QA | `qa_attest.py` | 仅独立 QA Actor 记录 `QA_PASS`；开发角色仅 `DEV_SELF_CHECK_PASS` |

---

## 4. 已明确排除（Out of Scope）

- 真实 RAG / OpenSPG / GraphDB / OpenMetadata / Oracle 接通；
- 全量业务模块迁移、删除 Legacy、生产切换；
- 自动 CRM 写回；
- 开发自签 QA、复用 P19 证据或签署；
- 未经 Owner 批准从 SHADOW 切到 FUSION。

---

## 5. 交叉评审提示词（请评审 Agent 使用）

> 将以下提示词提交给独立评审智能体，要求其对本文档与代码进行交叉评审。

```text
你是 P20 独立评审 Agent。请交叉评审
loops/P20-wiki-ontology-fusion/REVIEW_PENDING.md 所述的 shadow 基础设施
（模块：modules/knowledge-architecture、adapters/knowledge-filesystem），
仅做只读核查，不修改代码。

请按以下清单输出结构化评审结论：

1. 合同合规：
   - 领域 record / Port 字段是否与 specs/knowledge-architecture/schemas/*.json 一致？
   - 是否发明了合同未定义的字段或接口？
2. 分层正确：
   - 是否遵循 Port → Adapter → App 分层？
   - 模块间是否避免直接依赖实现类？
3. Fail-closed 完整性：
   - 是否覆盖 ROUTE-001~006、SEC-001~008 的期望决策？
   - 是否存在返回部分计划 / 吞异常 / 泄露堆栈的路径？
4. 测试充分性：
   - 单元测试 + 集成测试是否覆盖通过路径与全部拒绝路径？
   - 集成测试是否用真实 specs/knowledge-architecture 数据比对黄金确定性字段？
5. 不越界审计：
   - 是否未启用 fusion、未迁移生产、未修改现有业务行为、未跨越 P20 shadow 范围？
   - 是否未手工编辑 generated/、未引入敏感信息？
6. 证据纪律：
   - 是否将 DEV_SELF_CHECK 误标为 QA_PASS？是否记录了 FAILURES.md？
7. 风险与建议：
   - 指出任何必须在 Owner 授权前修复的问题（BLOCKER/MAJOR/MINOR）。

结论输出格式：
REVIEW_VERDICT=PASS / PASS_WITH_ISSUES / BLOCKED
BLOCKERS=...
MAJOR=...
MINOR=...
```

---

## 6. 变更记录

| 版本 | 时间 | 说明 |
|---|---|---|
| v0.1 | 2026-08-18 | 初稿：汇总 shadow 基础设施现状、下一步计划、通过标准与交叉评审提示词 |
