# P3｜映射权威源选择与工程缺口闭环派工

| 字段 | 值 |
|---|---|
| packet | `P3-MAPPING-AND-GAPS` |
| status | `IN_PROGRESS` |
| baseline | `specs/BASELINE_INDEX.yaml` |
| contract registry | `specs/CONTRACT_INDEX.yaml` |
| implementation actor | `tech_lead`（经共享记忆派工） |
| QA actor | `independent_qa` |

## Owner 已确认决策（2026-08-02）

1. **权威源**：确认 `A_ZHCX_CUST_BASE`（EDWCRM）为 `gits:Customer` 的候选权威源，替代不存在的 `AUTHORIZED_CUSTOMER_VIEW`。
2. **本体路径**：维持 `CTR-MAP-001` 为 `spike_only`；**现在不引入** `gits:Customer` 进入核心运行本体。

## 客户可感知目标

映射 Spike 的源定位与工程缺口（`created_by`、Source Contract 候选、机制 E2E 定位证据）闭环；独立 QA 复跑全门禁。

## 验收边界

- 不读客户行数据；Oracle 仅只读元数据；
- `gits:Customer` 不得自动升入核心本体；
- 合同变更先改源再 `make generate && make check`；
- 开发不得自签 QA。
