# P20｜Handoff

| 项 | 状态 |
|---|---|
| 合同包 | `CANDIDATE` |
| Owner授权 | `APPROVE_P20_SHADOW_IMPLEMENTATION` + `APPROVE_P20_BASELINE_GOVERNANCE_LIMITED` |
| 开发实现 | `IN_PROGRESS（route_activation_shadow_slice + G3/G4）` |
| 开发自检 | `DEV_SELF_CHECK_PASS` |
| 独立QA预审 | `PRE_ATTESTATION_REVIEW_VERDICT=PASS_WITH_ISSUES` |
| 正式QA Attestation | `PENDING` |
| 生产就绪 | `NO` |
| 冻结 | `NO` |
| 与P19并行 | `AUTHORIZED_BY_OWNER` |

## 状态语义说明（自然语言）

- 当前 `status=in_progress`、`implementation_state=DEV_SELF_CHECK_PASS`、
  `formal_qa_attestation=pending`、`gate_readiness=pending_baseline_governance`。
- **独立 QA 交叉预审**（PASS_WITH_ISSUES，无 BLOCKER/MAJOR）**不是**正式 QA Attestation。
- 在 `backend_test` 通过、P3/P4 完成之前，**不得**声明最终 `READY_FOR_INDEPENDENT_QA`。
- 执行顺序：**P1 基线治理 → P3/P4（可并行）→ 全量回归 6/6 → P2 正式独立 QA Attestation → P5 Owner 受控合并**。
  正式 QA Attestation 必须针对所有实现/合同变更完成后的最终候选 HEAD，否则 QA 签署的 HEAD 与证据哈希会失效。
- 上述字段采用仓库现有 schema 的最保守状态；如仓库无对应枚举，以此自然语言说明为准。

## 当前交付物

- 六项新合同 Schema；知识地图、20 项资产 Manifest、激活合同、路由策略；
- ADR-0015～0017 候选；两个黄金 ActivationPlan 和负向测试集；
- shadow 基础设施：readers + ActivationPlanner + Route Policy Evaluator + fail-closed 决策；
- Gap G3 受控语义查询（SEC-005）；Gap G4 计划驱动上下文装配（EvidenceBundle 非空）；
- 两场景 Shadow E2E（`run_p20_shadow_e2e.py`）+ 安全门禁。

## 当前 Gate

- 通过（5/6）：`contract_generate` / `contract_check` / `knowledge_architecture_check` / `security_check` / `shadow_e2e`
- 未通过（1/6）：`backend_test`（基线 OWASP npm nanoid + apps/api JaCoCo 覆盖率 0.69<0.80）
- 下一步：P1 基线治理 → P3/P4 → 全量回归 → 正式 QA Attestation

## 开放项

1. P1 基线治理：nanoid（优先父依赖升级/最小 overrides）+ apps/api 覆盖率（ADD_TESTS，≥0.80）；
2. P3 Shadow E2E 深化（确定性/重放/fail-closed 证明）；P4 合同索引核对；
3. 全量回归 6/6 Gate 通过后，由独立 QA Actor 对最终候选 HEAD 做正式 QA Attestation；
4. 独立 QA Actor 尚未分配，`QA_PASS` 由独立 QA 才可记录；
5. 仓库历史冻结状态声明需要另行治理；真实平台连接不在本 Loop 范围；
6. 未经 Owner 后续批准不得 merge 到主线、不得启用 production fusion、不得执行生产切换、不得执行生产写回。
