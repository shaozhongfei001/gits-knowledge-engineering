# P20 Evidence Log

当前没有实现或 QA 证据。本文件只定义证据追加格式；不得用计划、设计稿或本包校验替代执行证据。

```text
EVIDENCE_ID=
GATE=
ACTOR=
TIMESTAMP=
BASE_COMMIT=
COMMAND=
EXIT_CODE=
ARTIFACT_PATHS=
ARTIFACT_SHA256=
CLAIM_SCOPE=
```

初始状态：`OWNER_AUTHORIZATION_PENDING`。

---

## EVIDENCE_ID: EV-P20-INFRA-001（shadow 基础设施第一批）

```text
EVIDENCE_ID=EV-P20-INFRA-001
GATE=infrastructure_shadow_batch（非正式 Loop Gate；正式 Gate 仍 pending）
ACTOR=tech_lead
TIMESTAMP=2026-08-18
BASE_COMMIT=8e120ad5c2e1ea465acdd5b180f7f6cc59d8e2f1
COMMAND=mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test
EXIT_CODE=0
ARTIFACT_PATHS=modules/knowledge-architecture, adapters/knowledge-filesystem
CLAIM_SCOPE=
  - KnowledgeMapPort/AssetCatalogPort/ActivationContractPort/RoutePolicyPort 及领域 record
  - filesystem readers（ROOT 地图 MD 前导、四类资产目录扫描、activation/route JSON）
  - fail-closed：解析失败/文件缺失/必需字段缺失一律返回 Optional.empty 或跳过，不抛异常
  - 单元测试 27/27 通过；不启用 fusion、不迁移生产流程、不修改现有业务行为
```

附加验证（DEV_SELF_CHECK）：
- `make check` → PASS（含 contract-check、knowledge-architecture-check、loop-guard、secret-scan、enum-consistency、semantic-rule-gate 全通过）
- `make framework-test` → 3/3 OK
- `make tooling-test` → 14/14 OK（此前环境缺 PyYAML 已由默认 python3 提供解决，见 FAILURES.md ENV-P20-G0-001）
- `mvn -q compile`（全反应堆）→ EXIT 0

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-ENV-003（PyYAML 工具链可复现性关闭）

```text
EVIDENCE_ID=EV-P20-ENV-003
GATE=semantic_rule_gate / make check / make tooling-test
ACTOR=feature_pilot (tech_lead)
TIMESTAMP=2026-08-18
BASE_COMMIT=8e120ad5c2e1ea465acdd5b180f7f6cc59d8e2f1
COMMAND=
  ./gits-kno-p20-venv/bin/python scripts/semantic_rule_gate.py
  make PYTHON=./gits-kno-p20-venv/bin/python tooling-test
  make PYTHON=./gits-kno-p20-venv/bin/python check
EXIT_CODE=0
ARTIFACT_PATHS=requirements-tooling.txt, .gitignore, scripts/secret_scan.py
ARTIFACT_SHA256=(见 git 提交 hash；本批提交)
CLAIM_SCOPE=
  - PyYAML 与 rdflib 已声明到 requirements-tooling.txt，可复现
  - gits-kno-p20-venv 创建并从声明安装；新环境可导入 yaml 并运行 semantic rule gate（LinkML PASS）
  - 修复 secret_scan.py 的 git worktree 检测，使其尊重 .gitignore（不误扫 venv）
  - ENV-P20-G0-001 关闭，见 FAILURES.md（append-only，原始记录保留）
```

验证（DEV_SELF_CHECK，使用 venv Python）：
- `semantic_rule_gate.py` → `semantic-rule-gate: PASS`（含 `LinkML: PASS`），EXIT=0
- `make tooling-test` → 14/14 OK，EXIT=0
- `make check` → PASS（secret-scan PASS with 62 advisory findings），EXIT=0

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-INFRA-002（shadow 基础设施第二批：ActivationPlanner）

```text
EVIDENCE_ID=EV-P20-INFRA-002
GATE=infrastructure_shadow_batch（非正式 Loop Gate；正式 Gate 仍 pending）
ACTOR=tech_lead
TIMESTAMP=2026-08-18
BASE_COMMIT=8e120ad5c2e1ea465acdd5b180f7f6cc59d8e2f1
COMMAND=mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test
EXIT_CODE=0
ARTIFACT_PATHS=modules/knowledge-architecture（ActivationPlan/PlanDecision/DefaultActivationPlanner）
CLAIM_SCOPE=
  - ActivationPlan 领域 record（合同 CTR-PLAN-001）
  - DefaultActivationPlanner：Route Policy → Activation Contract → Asset Catalog 汇合为可重放 ActivationPlan
  - fail-closed 决策：DENY_UNMAPPED_TASK / DENY_NOT_IN_P20 / DENY_PERMISSION_PENDING / 资产未登记 DENY（SEC-003）
  - 单元测试：DefaultActivationPlannerTest 7/7；集成测试 DefaultActivationPlannerFilesystemIT 3/3（黄金确定性字段比对）
  - 不启用 fusion、不迁移生产流程、不修改现有业务行为
```

附加验证（DEV_SELF_CHECK）：
- `make check` → PASS；`make framework-test` → 3/3 OK；`make tooling-test` → 14/14 OK
- `mvn -q compile`（全反应堆）→ EXIT 0
- 两个 P20 场景（PRE_VISIT_PREPARATION / FACT_RECONCILIATION_30M）均可生成与黄金计划确定性字段一致的 ActivationPlan

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-ROUTE-004（Route & Activation shadow slice）

```text
EVIDENCE_ID=EV-P20-ROUTE-004
GATE=route_activation_shadow_slice（正式 Loop Gate 仍 pending：backend_test 因基线 OWASP/覆盖率阻断）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=8e120ad5c2e1ea465acdd5b180f7f6cc59d8e2f1
COMMAND=
  mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test
  mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am verify
  make PYTHON=./gits-kno-p20-venv/bin/python check / framework-test / tooling-test
EXIT_CODE=0（新模块全部测试与门禁）
ARTIFACT_PATHS=modules/knowledge-architecture（route/*）、adapters/knowledge-filesystem（PathSafety）
CLAIM_SCOPE=
  - RoutePolicyEvaluatorPort + DefaultRoutePolicyEvaluator：确定性 route 决策（AllowRoute/Deny），带 policy/contract ID、原因、优先级
  - DefaultActivationPlanner 重构：经 evaluator 解析唯一 route；新增 DENY_PRODUCTION_MODE / DENY_WRITEBACK / DENY_CONTRACT_MISMATCH（route↔contract 一致性）
  - ExecutionMode 枚举 + ActivationPlanRequest 扩展（executionMode/writebackRequested）
  - PathSafety：防路径越界、拒绝路径分隔符/`.`/`..`/绝对路径、不跟随基目录外符号链接
  - 两个 P20 场景（PRE_VISIT_PREPARATION / FACT_RECONCILIATION_30M）端到端与黄金计划一致
  - 不启用 fusion、不迁移生产、不执行生产写回、不修改现有业务行为
```

验证（DEV_SELF_CHECK）：
- 新模块单元+集成测试：领域 17 + 适配 32 = 49 全通过；`-am verify` BUILD SUCCESS
- `make check` → PASS（secret-scan 62 advisories 如实报告）
- `make framework-test` → 3/3 OK；`make tooling-test` → 14/14 OK
- loop memory/evidence guard → PASS
- `make backend-test` 因**基线** OWASP npm nanoid 阻断 + apps/api JaCoCo 覆盖率 0.69<0.80 失败（非本批回归；本批模块均 SUCCESS，未触碰 frontend/ 或 apps/api）

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。
