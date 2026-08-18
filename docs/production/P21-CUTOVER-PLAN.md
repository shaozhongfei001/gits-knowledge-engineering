# P21｜生产 Cutover 与回滚计划

```text
DOC_ID=P21-CUTOVER-PLAN-001
STATUS=APPROVED_PREPARATION（仅准备，不执行实际 cutover）
AUTHOR=tech_lead
CREATED_AT=2026-08-19
RELATED=loops/P21-production-readiness
SCOPE=production-ready preparation only（FUSION_CUTOVER=NOT_EXECUTED, PRODUCTION_CUTOVER=NOT_EXECUTED）
```

> 本计划仅作**准备**。实际生产切换须经 Owner 另行批准（`FUSION_CUTOVER` / `PRODUCTION_CUTOVER` 均为 NOT_EXECUTED），并满足全部 gate 与独立 QA。

---

## 1. 目标与边界

- 目标：将 P20 已验证的 wiki-ontology 控制面（Knowledge Map / Route Policy / ActivationPlan / 受控语义查询 / 上下文装配）从 shadow 推进到生产运行。
- **边界**：本阶段只产出计划与合同；不连接真实生产系统、不写回、不迁移数据。

## 2. 前置条件（cutover 门槛）

- [ ] P20/P21 全部 gate PASS，独立 QA 正式 QA_PASS。
- [ ] `application-prod.yaml` 生产凭据 fail-closed 校验通过（已完成，见 `verify_prod_profile_fail_closed.sh`）。
- [ ] 真实平台接入（real RAG / GraphDB / OpenMetadata）Port 合同已登记到 `CONTRACT_INDEX.yaml`。
- [ ] Oracle 写回（若启用）已获独立 ADR + 数据所有者授权 + 专用 Loop（quarantine 资产）。
- [ ] Owner 明确批准 `FUSION_CUTOVER=YES`。

## 3. 分阶段 Cutover

| 阶段 | 动作 | 回滚 |
|---|---|---|
| C0 预检 | 全量回归 `make verify`（除 db-check 需凭据环境）；prod fail-closed 校验 | 不启动生产 |
| C1 控制面只读 | 生产环境以 `prod` profile 启动，启用知识地图/路由/ActivationPlan 只读消费 | 停止服务 / 回滚配置 |
| C2 受控激活 | 启用受控语义查询与上下文装配，仅对授权客户/操作 | 关闭 feature flag，回退 V1.1 路径 |
| C3 fusion | 双路径融合为生产默认（ActivationPlan 权威） | 切换 `engagement.reconciliation.mode` 回 fallback，恢复 V1.1 |
| C4 写回（可选） | CRM 写回仅当 Owner 批准 `PRODUCTION_WRITEBACK=YES` | 停用 `crm.mode=http` → `logging` |

## 4. 回滚方案

- **配置回滚**：保留 V1.1 基线 `main` tag（`44785fb`），任一阶段失败即 `git revert` 对应 feature commit 或切换 profile。
- **数据回滚**：生产写回仅通过幂等命令 + 审计；异常时停用写回，Oracle/CRM 侧人工对账。
- **熔断**：`ProdConfigValidator` 缺凭据即启动失败（fail-closed），防止半配置上线。
- **健康探测**：`/actuator/health/readiness` + Prometheus 指标监控，异常自动告警。

## 5. 真实平台接入计划

| 平台 | Port 合同（拟） | 适配器（拟） | 状态 |
|---|---|---|---|
| real RAG | `RagEmbeddingPort` | `MilvusRagAdapter`（shadow=MockRag） | 待登记合同 |
| GraphDB | `KnowledgeGraphPort` | `Neo4jGraphAdapter` | 待登记合同 |
| OpenMetadata | `MetadataCatalogPort` | `OpenMetadataAdapter` | 待登记合同 |

> 以上 Port 均为**拟新增合同**，必须先登记 `CONTRACT_INDEX.yaml` → `make generate` → `make check`，再实现适配器。本阶段仅定义计划，不实现。

## 6. 退出条件

- 计划文档存在且完整（本文件 + 回滚 + 分阶段 + 真实平台计划）。
- Port 合同候选已登记 CONTRACT_INDEX（见 `docs/production/P21-REAL-PLATFORM-PORTS.md`）。
- Owner 未批准前不执行实际 cutover。
