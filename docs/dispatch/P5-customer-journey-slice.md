# P5-customer-journey-slice Dispatch

| 字段 | 值 |
|---|---|
| packet | `P5-CUSTOMER-JOURNEY-SLICE` |
| status | `CLOSED` |
| baseline | `specs/BASELINE_INDEX.yaml` |
| contract registry | `specs/CONTRACT_INDEX.yaml` |
| implementation actor | `tech_lead` |
| QA actor | pending |
| loop | `P5-customer-journey-slice` |
| created_at | `2026-08-02T14:35:00+00:00` |

## Scope — 客户经营纵向切片

首期纵向主链：M17 → M18 → M20 → M21 → M22，统一 OperatingCase 状态链。

| 模块 | 名称 | 交付物 |
|---|---|---|
| M17 | 客户经营（KYC问题辅助）智能体 | gits:OperatingCase + gits:CustomerJourney + 状态转换 |
| M18 | 客户洞察智能体 | gits:InsightClaim → OperatingCase 证据链 |
| M20 | 产品候选组合智能体 | gits:ProductCandidateClaim → insight→product 链接 |
| M21 | 访前报告智能体 | gits:PrevisitReport ← 累积 claims/evidence |
| M22 | 访后分析智能体 | gits:PostvisitAnalysis → OperatingCase 闭环 |

## Authorization

- Dispatch 由 Runner 发起，基于 SDD 长程任务规划（4阶段7工单）
- 首期纵向主链定义见 MODULE_CATALOG.md
- 合同注册：CTR-API-001, CTR-SEM-001, CTR-SEM-002, CTR-DATA-001, CTR-DATA-002
- 排除：M19（产品知识解读）、M08-M16（非首期场景）

## Dependencies

- A1 ✅ P4 QA pass
- A3 ✅ SPEC_INDEX.md 已创建
- B1 ✅ P1 Oracle readonly（LOOP.yaml 已修复）

## Constraints

- OperatingCase 状态机是唯一的客户经营状态载体——场景模块不得各自复制
- AI 输出只能是 Candidate Claim / Proposal——不得写成 BUSINESS_SIGNED
- Oracle/Ossie 隔离资产不得启用
- P5 未 QA pass 之前不得开始新 P6+ dispatch
