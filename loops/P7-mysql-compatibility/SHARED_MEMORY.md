# P7-mysql-compatibility — Shared Memory

## Loop Goal

确保项目所有SQL migration脚本和JPA实体在MySQL 8.0下完全兼容，并实现MySQL profile的实际启动验证。

## Task Plan

| Task | Description | Owner | Status |
|------|-------------|-------|--------|
| T1 | V001 JSON列类型修复（JSON→VARCHAR(4000)） | tech_lead + architect | ✅ COMPLETED |
| T2 | JdbcInteractionRepository 兼容性验证 | backend-dev | ✅ COMPLETED - 无需修改 |
| T3 | application-mysql.yaml Flyway启用修复 | tech_lead | ✅ COMPLETED |
| T4 | Gate验证（generate/check/security-check） | QA | ✅ COMPLETED - 全部通过 |
| T5 | 独立QA：MySQL环境端到端验证 | independent_qa_agent | PENDING |

## Key Decisions

- 目标MySQL版本：8.0.41（本地 127.0.0.1:3306）
- 数据库名：gits_ke
- 用户：root / szf123
- `precision` 是MySQL 8.0保留字，需用反引号
- **2026-08-04**: V001中3个`JSON`列类型改为`VARCHAR(4000)`以兼容H2（H2 MODE=MySQL不支持JSON类型）
- **2026-08-04**: 启用application-mysql.yaml中的Flyway（之前被禁用导致V001表未创建）

## Discoveries

- `status` 是 MySQL 8.0 保留字，`JdbcOperatingCaseRepository.java` 中 3 处 SQL 需用反引号包裹 ✅ 已修复
- `application-mysql.yaml` Flyway配置完善 ✅ 已完善
- **2026-08-04**: `producedClaimIds`本身不是问题（VARCHAR(4000)双库兼容）
- **2026-08-04**: **真正阻塞点**: V001中3个`JSON`原生列类型（`controlled_action.payload_json`, `evaluation_run.metrics_json`, `outbox_event.payload_json`），H2的`MODE=MySQL`不支持
- **2026-08-04**: `application-mysql.yaml`中`flyway.enabled: false`导致V001表在MySQL环境下未创建 ✅ 已修复为`true`
- **2026-08-04**: 修复后14个测试全部通过，V001/V002迁移在H2下正常执行
- **2026-08-04**: 所有Gate验证通过（generate, contract-check, loop-guard, secret-scan, semantic-rule-gate）

## Open Issues

- 当前环境未运行 MySQL 实例，Flyway 迁移验证、Spring Boot 实际启动验证、集成测试待 MySQL 环境就绪后执行
- 独立 QA 验证（T5）待开发自检完成后由独立 QA actor 执行
