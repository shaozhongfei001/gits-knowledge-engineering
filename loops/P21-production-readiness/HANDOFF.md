# P21-production-readiness｜Handoff

| 项 | 状态 |
|---|---|
| 交付对象 | 以dispatch和LOOP为准 |
| 开发自检 | `COMPLETED`（6/6 实现 gate PASS，actor=tech_lead） |
| 独立QA | `PASS`（actor=independent_qa，qa_attest.py EXIT=0） |
| 生产就绪 | `preparation_in_progress`（FUSION/PRODUCTION CUTOVER=NOT_EXECUTED） |

## 交付物

- `application-prod.yaml`：`gits.security.api-key` 改为 fail-closed（`${API_KEY}` 无默认）。
- `apps/api/.../config/ProdConfigValidator.java`：prod 启动时校验 api-key / llm(real) / crm(http) 凭据非空（fail-closed）。
- `scripts/verify_prod_profile_fail_closed.sh`：生产 profile fail-closed 验证。
- `docs/production/P21-CUTOVER-PLAN.md`：分阶段 cutover C0-C4 + 回滚方案 + 前置条件（preparation only）。
- `docs/production/P21-REAL-PLATFORM-PORTS.md`：真实平台接入 Port 合同候选（RagEmbeddingPort / KnowledgeGraphPort / MetadataCatalogPort）。
- `scripts/verify_production_plan.sh`：生产计划验证。

## 开放项与禁止声明

- **实际生产切换 / fusion cutover / 写回 仍为 NOT_EXECUTED**，须 Owner 另行批准后执行（P21 仅为准备）。
- 真实平台 Port 合同候选**未登记** CONTRACT_INDEX，需经合同审查后再实现，禁止直接实现。
- Oracle 写回属 quarantine 资产，需单独 ADR + 数据所有者授权 + 专用 Loop。
- 不得将开发自检写成QA、真实E2E、客户验收或生产冻结。
