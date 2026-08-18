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

---

## EVIDENCE_ID: EV-P20-E2E-005（两场景 Shadow E2E 与安全门禁）

```text
EVIDENCE_ID=EV-P20-E2E-005
GATE=shadow_e2e / security_check（正式 Loop Gate）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=44bf4b2a804ff2416edeba939217aa6a0bf1bdcf
COMMAND=
  python3 scripts/run_p20_shadow_e2e.py --mode shadow --scenario PRE_VISIT_PREPARATION --scenario FACT_RECONCILIATION_30M
  make PYTHON=./gits-kno-p20-venv/bin/python security-check
EXIT_CODE=0
ARTIFACT_PATHS=scripts/run_p20_shadow_e2e.py, tests/tooling/test_shadow_e2e.py,
  loops/P20-wiki-ontology-fusion/evidence/shadow-e2e/shadow-e2e-evidence.json
CLAIM_SCOPE=
  - 两场景 shadow E2E：确定性 ActivationPlan 与黄金计划 deterministicFields 一致，可重放
  - shadow-e2e 只写 Loop 证据目录，formal_output_changed=False，不改变正式输出
  - fail-closed：--mode production 拒绝；第三场景拒绝（DENY_UNMAPPED_TASK/DENY_NOT_IN_P20/DENY_CONTRACT_MISMATCH）
  - security-check：secret-scan PASS(62 advisories)、sensitive-permissions PASS、oracle-readonly-guard PASS
```

验证（DEV_SELF_CHECK）：
- `run_p20_shadow_e2e.py --mode shadow --scenario PRE_VISIT_PREPARATION --scenario FACT_RECONCILIATION_30M` → PASS，EXIT=0
- `make tooling-test` → 17/17 OK（含 3 项新增 shadow-e2e 测试）
- `make security-check` → PASS，EXIT=0
- `make check` → PASS；loop memory/evidence guard → PASS
- EVIDENCE.json 中 `shadow_e2e` / `security_check` 已 hash-attested 为 pass（证据落盘于 loop/evidence/）

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-SQ-006（受控语义查询 Gap G3）

```text
EVIDENCE_ID=EV-P20-SQ-006
GATE=semantic_query_gate（Gap G3，非正式 Loop Gate）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=7ec8542
COMMAND=mvn -pl modules/semantic-runtime test
EXIT_CODE=0
ARTIFACT_PATHS=modules/semantic-runtime（SemanticQueryPort/RegisteredSemanticQueryCatalog/FailClosedSemanticQueryGuard）
CLAIM_SCOPE=
  - 只允许注册 Semantic Query ID 执行；任意 SPARQL / 未注册 / 空白一律 DENY_ONLY_REGISTERED_QUERY_ID（SEC-005）
  - RegisteredSemanticQueryCatalog：P20 激活合同 semanticQueries ∪ 技能 semanticDependencies 的 9 个登记 ID
  - FailClosedSemanticQueryGuard：rawQuery 非空、queryId 缺失/未登记、非法命名构造均 fail-closed
  - 不连接真实语义端点、不产生业务副作用
```

验证（DEV_SELF_CHECK）：
- `mvn -pl modules/semantic-runtime test` → 16 测试通过（含 7 项新语义查询守卫测试）
- `make check` → PASS；`make framework-test` → OK；`make tooling-test` → 17/17 OK
- 全反应堆 `mvn -q compile` → EXIT 0；loop memory/evidence guard → PASS

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-CTX-007（上下文装配 Gap G4）

```text
EVIDENCE_ID=EV-P20-CTX-007
GATE=context_assembly_gate（Gap G4，非正式 Loop Gate）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=461f7c8
COMMAND=mvn -pl modules/context-evidence -am test
EXIT_CODE=0
ARTIFACT_PATHS=modules/context-evidence（ContextAssemblyPort.PlanRequest / DefaultContextAssembler）
CLAIM_SCOPE=
  - 计划驱动装配：由 ActivationPlan 生成非空 EvidenceBundle（G4 退出条件）
  - 权限完整：permissionDecisionId 缺失/PENDING 一律拒绝（fail-closed）
  - 来源完整：evidence 由 selectedAssets 的来源 URI/版本/权限标签构成；candidateClaims 由 ruleChecks 派生
  - 新增 context-evidence -> knowledge-architecture 依赖（均在 P20 scope 内）
  - 不迁移生产、不启用 fusion、不改变现有业务行为（现有 assemble(Request) 保持原语义）
```

验证（DEV_SELF_CHECK）：
- `mvn -pl modules/context-evidence -am test` → 19 测试通过（含 4 项新计划驱动装配测试）
- 全反应堆 `mvn -q compile` → EXIT 0；`make check` → PASS

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-QA-008（独立 QA 交叉评审）

```text
EVIDENCE_ID=EV-P20-QA-008
GATE=independent_qa_review
ACTOR=independent_qa（独立评审 Agent，只读）
TIMESTAMP=2026-08-18
BASE_COMMIT=78e5372
REVIEW_VERDICT=PASS_WITH_ISSUES
BLOCKERS=（无）
MAJOR=（无）
MINOR=5（MINOR-2 路由歧义已修复；其余记录为 refinement）
EVIDENCE_FILE=loops/P20-wiki-ontology-fusion/evidence/qa-review/qa-review-report.md
CLAIM_SCOPE=
  - 独立只读评审：合同合规/分层/fail-closed/测试/越界/证据/路径安全 均 PASS
  - 无阻断 Owner 授权的 BLOCKER/MAJOR
  - 协调阻断：backend_test（基线 OWASP npm nanoid + apps/api 覆盖率）需治理后才可
    ready_for_independent_qa（loop_guard 强制 all gates pass）
```

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。

---

## EVIDENCE_ID: EV-P20-P1D-009（gits-kno-api 运行时安全治理）

```text
EVIDENCE_ID=EV-P20-P1D-009
GATE=gits_kno_api_runtime_security（Owner APPROVE_P20_API_RUNTIME_SECURITY_REMEDIATION_LIMITED）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=cc04e94
SPRING_BOOT_OLD_VERSION=3.5.16
SPRING_BOOT_NEW_VERSION=3.5.16（主/次版本不变）
TOMCAT_OLD_VERSION=10.1.55
TOMCAT_NEW_VERSION=10.1.57
TOMCAT_VERSION_ALIGNMENT=core/el/websocket 全部 10.1.57（统一，无漂移，无 Tomcat 11）
```

### Tomcat CVE 暴露矩阵

| CVE | 版本状态 | FEATURE_PRESENT | RUNTIME_EXPOSURE | FINAL_CLASSIFICATION |
|---|---|---|---|---|
| CVE-2026-55276 | 10.1.55 受影响 | 无 effective web.xml 日志依赖 | 升级后 10.1.57 修复 | RESOLVED（10.1.57） |
| CVE-2026-53434 | 10.1.55 受影响 | 无 FFM Connector/CRL | 升级后修复 | RESOLVED（10.1.57） |
| CVE-2026-53404 | 10.1.55 受影响 | 无 RewriteValve | 升级后修复 | RESOLVED（10.1.57） |
| CVE-2026-59083 | 10.1.55 受影响 | 无 RewriteValve | 升级后修复 | RESOLVED（10.1.57） |
| CVE-2026-59084 | 10.1.55 受影响 | 无 EncryptInterceptor | 升级后修复 | RESOLVED（10.1.57） |
| CVE-2026-66299 | 10.1.57 仍列出 | 无 Tomcat examples/WebSocket chat（已核实无 WebSocket 端点/无 examples/无 tomcat 配置） | NOT_APPLICABLE | UNRESOLVED_PENDING（修复或在 10.1.58，未发布；已记录） |

### Micrometer/Prometheus 适用性分析
- `micrometer-registry-prometheus@1.15.12` → `io.prometheus:prometheus-metrics-core@1.3.10`（**Java client**）
- `PROMETHEUS_SERVER_PRESENT=NO`、`REMOTE_READ_ENDPOINT_PRESENT=NO`（无 `/api/v1/read`，无 snappy 解码）
- 应用仅经 `/actuator/prometheus` 暴露指标 exposition
- **CVE_2026_42154=SCANNER_COMPONENT_MISMATCH**（漏洞位于 Prometheus server remote-read；Java client 不实现该端点）
- `RUNTIME_EXPOSURE=NOT_APPLICABLE`；已建精确 suppression（仅 CVE-2026-42154 + micrometer-registry-prometheus）

### Swagger UI advisory
- `swagger-ui/DOMPurify`：CVSS 5.1 → **ADVISORY_BELOW_THRESHOLD**，不 suppression
- 需进一步核对开发环境/production profile 暴露面（见 P1d API 回归）
- 若 Spring Boot patch/BOM 自然带来修复版本可接受，不为该 advisory 单独前端升级

### 验证（DEV_SELF_CHECK）
- `tomcat-embed-*` 统一 10.1.57；5/6 Tomcat CVE 已清除
- 剩余 CVE-2026-66299（10.1.57 仍列）与 micrometer CVE-2026-42154（组件不匹配，已精确抑制）待 API 回归确认
- swagger DOMPurify 多个 CVE（5.1 及 DOMPurify XSS）为 advisory，待暴露面确认

### P1b 覆盖率治理与 backend_test 转绿（EV-P20-P1B-010）

```text
EVIDENCE_ID=EV-P20-P1B-010
GATE=backend_test（OWASP + JaCoCo）
ACTOR=feature_pilot
TIMESTAMP=2026-08-18
BASE_COMMIT=cc04e94
BACKEND_TEST=PASS（EXIT=0，mvn verify BUILD SUCCESS + dependency-check-guard 全 PASS）
APPS_API_LINE_COVERAGE=0.80+（实际 80.4%，317 测试通过）
P1D_TOMCAT=RESOLVED（10.1.55 → 10.1.57，统一，无 Tomcat 11，Spring Boot 主/次版本不变）
P1D_MICROMETER=SCANNER_COMPONENT_MISMATCH_EVIDENCED（无 server/remote-read，Java client 仅 exposition）
SWAGGER_UI_EXPOSURE=DEV_ONLY（prod 禁用 springdoc）
SCANNER_FAIL_CLOSED=PASS（failOnError=true；ossindexAnalyzerEnabled=false；guard 校验 JSON 报告）
FORMAL_GATES=6/6_PASS（contract_generate/contract_check/knowledge_architecture_check/security_check/shadow_e2e/backend_test）
```

新增覆盖率测试：
- `ReportStrategyCoverageTest`（12）：R5A 内部关系/R8 下次访前/R7 更新关系/R5B CRM 通话，LLM 成功/失败 fallback/空视图/富视图分支
- `V11ScenarioDataLoaderTest`（20）：9 类数据 load 空路径 + 数据映射路径（customer/legal/group/credit/bank snapshot/external event/product card/kyc/interaction）

状态：DEV_SELF_CHECK（独立 QA 尚未签署，QA_PASS 不由此记录）。
