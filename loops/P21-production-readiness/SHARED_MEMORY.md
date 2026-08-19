# P21-production-readiness｜Shared Memory

> Chat不是SSOT；未落盘等于不存在。

## Owner 决策（2026-08-19）

```text
OWNER_DECISION=OWNER_APPROVED_P21_PRODUCTION_READINESS_PREPARATION
CONTEXT=P20 wiki-ontology fusion shadow 验证通过（qa_pass），Owner 授权推进生产就绪准备
SCOPE=P20 控制面生产运行准备 + 真实平台接入 Port 合同/适配器计划 + cutover/回滚计划
NOT_IN_SCOPE=Oracle 写回（隔离资产）、生产凭据、生产数据迁移、direct CRM 写回、legacy 移除
PRODUCTION_CUTOVER=NOT_EXECUTED（仅准备，不做实际生产切换）
FUSION_CUTOVER=NOT_EXECUTED（仅准备，不做实际 fusion 切换）
PRODUCTION_WRITEBACK=NOT_AUTHORIZED
decision_source=Human Owner directive supplied through the execution prompt
```

## Current Snapshot

| 字段 | 值 |
|---|---|
| status | qa_pass |
| baseline_commit | `11e2cfe4fb2bd455b809095578fd8f704ff20598` |
| baton_holder | `independent_qa` |
| current_wave | `independent_qa_attestation` |
| qa_actor | `independent_qa` |
| updated_at | `2026-08-19T02:50:00+08:00` |

## 不变量

- P21 是**生产就绪准备**，不是生产切换执行；不实际连接生产系统、不写回。
- Oracle 是 quarantine 隔离资产，启用需 ADR + 数据所有者授权 + 专用 Loop，**不在 P21 scope**。
- 生产凭据（MYSQL/LLM/CRM/Oracle）通过环境变量注入，绝不落盘。
- 生产配置 fail-closed：缺必需凭据时必须拒绝，不静默降级到不安全默认。
- 真实平台接入（real RAG / GraphDB / OpenMetadata）需先定义 Port 合同并登记到 CONTRACT_INDEX。
- 保留 ADR-0017 OpenWiki 边界：投影不成为权威源/权限引擎/本体运行时。

## Role Results

| 角色 | 状态 | 结果 | Handoff |
|---|---|---|---|
| `tech_lead` | completed | 6/6 实现 gate PASS（prod fail-closed + production plan + contract/security/backend） | `memory/handoffs/tech_lead.md` |
| `independent_qa` | attested_pass | 正式 QA PASS（actor=independent_qa，qa_attest.py EXIT=0） | — |

## 收尾说明

- P21 为生产就绪**准备** Loop：6/6 实现 gate + 正式独立 QA 均 PASS，STATE=`qa_pass`。
- `make verify` 除 `db-check`（需外部 GITS_KEDB_PASSWORD，P21 用 H2 不需 MySQL）外全绿；后端 317+22 tests、前端 100 tests、dependency-check 15 reports 全 PASS。
- **边界**：FUSION/PRODUCTION CUTOVER=NOT_EXECUTED；实际生产切换须 Owner 另行批准。真实平台 Port 合同候选待登记 CONTRACT_INDEX（禁止直接实现）。
