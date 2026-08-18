# P21 Dispatch｜P20 wiki-ontology fusion 生产就绪准备

```text
DISPATCH_ID=P21-PRODUCTION-READINESS
STATUS=APPROVED_PREPARATION
OWNER_DECISION=OWNER_APPROVED_P21_PRODUCTION_READINESS_PREPARATION
BASE_COMMIT=11e2cfe4fb2bd455b809095578fd8f704ff20598
LOOP=loops/P21-production-readiness
CONTRACT_CANDIDATE_HOLDER=tech_lead
IMPLEMENTATION_ACTOR=tech_lead
IMPLEMENTATION_SCOPE=preparation_only（不做实际生产切换）
PRODUCTION_READY=preparation_in_progress
FROZEN=NO
QA_ACTOR=MUST_BE_INDEPENDENT
```

## 目标

将 P20 已验证的 wiki-ontology 融合控制面从 shadow 推进到**生产就绪准备**（非执行切换）：

1. 验证 `application-prod.yaml` 生产 profile 在无凭据环境下 fail-closed（缺必需凭据必须拒绝，不静默降级）。
2. 定义真实平台接入（real RAG / GraphDB / OpenMetadata）的 Port 合同与适配器计划，登记到 `CONTRACT_INDEX.yaml`。
3. 产出生产 cutover 计划 + 回滚方案。
4. 通过独立 QA 与 Owner 门禁。

## 明确排除

- Oracle 写回（quarantine 隔离资产，需单独 ADR + 数据所有者授权 + 专用 Loop）；
- 生产凭据落盘 / 真实生产连接；
- 生产数据迁移；
- direct CRM 写回；
- legacy 移除；
- 实际生产切换 / fusion 切换执行。

## 开发规则

1. 先合同后实现；`generated/` 只读。
2. 失败先记录到 `FAILURES.md`。
3. 生产配置 fail-closed，不因缺凭据静默降级。
4. Oracle 隔离资产不并入本 Loop。
5. 未经 Owner 批准不得执行实际生产切换/写回。
6. 开发只记录 `DEV_SELF_CHECK_PASS`，独立 QA 单独签署。
