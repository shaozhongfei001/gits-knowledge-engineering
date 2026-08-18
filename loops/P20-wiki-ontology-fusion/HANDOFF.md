# P20｜Handoff

| 项 | 状态 |
|---|---|
| 合同包 | `CANDIDATE` |
| Owner授权 | `APPROVE_P20_SHADOW_IMPLEMENTATION` + `APPROVE_P20_BASELINE_GOVERNANCE_LIMITED` + `APPROVE_P20_CASCADE_SECURITY_REMEDIATION_LIMITED` + `APPROVE_P20_API_RUNTIME_SECURITY_REMEDIATION_LIMITED` |
| 开发实现 | `COMPLETED（route_activation_shadow_slice + G3/G4）` |
| 开发自检 | `DEV_SELF_CHECK_PASS` |
| 独立QA预审 | `PRE_ATTESTATION_REVIEW_VERDICT=PASS_WITH_ISSUES` |
| 正式QA Attestation | `PASS（independent_qa，actor≠feature_pilot）` |
| 生产就绪 | `NO` |
| 冻结 | `NO` |
| 与P19并行 | `AUTHORIZED_BY_OWNER` |

## 状态语义说明（自然语言）

- 最终 `status=qa_pass`、`implementation_state=DEV_SELF_CHECK_PASS`、
  `formal_qa_attestation=completed`、`gate_decision=ALL_GATES_PASS`。
- **独立 QA 交叉预审**（PASS_WITH_ISSUES，无 BLOCKER/MAJOR）**不是**正式 QA Attestation；本 Loop 已由独立 QA Actor（`independent_qa`，≠ `feature_pilot`）通过 `qa_attest.py` 完成正式 Attestation，符合"开发不得自签"红线。
- 执行顺序已全部完成：**P1 基线治理 → P3/P4（可并行）→ 全量回归 6/6 → P2 正式独立 QA Attestation → P5 Owner 受控合并（待 Owner 审查）**。
- `memory-check` 与 `evidence-check` 均 PASS；Baton 已交接至 `independent_qa`。

## 当前交付物

- 六项新合同 Schema；知识地图、20 项资产 Manifest、激活合同、路由策略；
- ADR-0015～0017 候选；两个黄金 ActivationPlan 和负向测试集；
- shadow 基础设施：readers + ActivationPlanner + Route Policy Evaluator + fail-closed 决策；
- Gap G3 受控语义查询（SEC-005）；Gap G4 计划驱动上下文装配（EvidenceBundle 非空）；
- 两场景 Shadow E2E（`run_p20_shadow_e2e.py`）+ 安全门禁。

## 当前 Gate

- 通过（6/6）：`contract_generate` / `contract_check` / `knowledge_architecture_check` / `security_check` / `shadow_e2e` / `backend_test`
- `backend_test` 已完成：P1a OWASP npm nanoid 最小范围升级、apps/api JaCoCo 覆盖率 0.804≥0.80（补有效断言测试，未降门槛/未排除核心类）
- 下一步：P5 Owner 受控合并审查（独立 QA 已 PASS）

## 开放项

1. ~~P1 基线治理~~ → 已完成（nanoid 最小升级 + 覆盖率 0.804）
2. ~~P3 Shadow E2E 深化~~ → 已完成（确定性/重放/fail-closed 证明）
3. ~~P4 合同索引核对~~ → 已完成（make check 全绿）
4. ~~正式独立 QA Attestation~~ → 已完成（independent_qa PASS，actor≠feature_pilot）
5. 仓库历史冻结状态声明需要另行治理；真实平台连接不在本 Loop 范围；
6. 未经 Owner 后续批准不得 merge 到主线、不得启用 production fusion、不得执行生产切换、不得执行生产写回。
