# P6 端到端业务链落地 — 长程任务规划

> 创建时间：2026-08-02
> 状态：规划中，待用户确认后分批启动
> 前置：P5 QA pass（已满足）

## 目标

从当前的「域对象骨架 + 内存编排」推进到「Spring Boot 应用启动 → REST API 触发 → H2持久化 → 完整业务链闭环」，实现真正的端到端业务链场景。

## 现状盘点

| 层次 | 已有 | 缺失 |
|---|---|---|
| 域对象 | OperatingCase/Claim/Interaction/10个本体record ✅ | Interaction数据库表不匹配新14字段设计 ❌ |
| 编排层 | CustomerJourneyOrchestrator(纯内存) ✅ | Spring Service注入+事务管理 ❌ |
| 持久化 | operating_case/claim/interaction DDL(V001) ✅ | Interaction表只有5列，需V002迁移 ❌ |
| REST API | ArchitectureStatusController(仅健康检查) ✅ | 客户旅程CRUD+状态推进API ❌ |
| 应用启动 | GitsKnoApiApplication ✅ | 未接入persistence-relational依赖 ❌ |
| 业务链测试 | 内存编排测试(33个) ✅ | Spring Boot集成测试(H2) ❌ |
| 数据库 | V001 Flyway DDL ✅ | H2配置未启用 ❌ |

## 分批任务

### 批次1：基础设施连通（纯无人值守）

> 不需要任何人类决策，纯技术实施

| # | 任务 | 预估 | 输出 |
|---|---|---|---|
| T1 | V002 Flyway迁移：Interaction表扩展14字段 | 15min | V002__interaction_enriched.sql |
| T2 | Interaction+Claim+Journey JDBC Repository | 30min | JdbcInteractionRepository等 |
| T3 | api/pom.xml添加persistence-relational依赖 | 5min | pom.xml |
| T4 | application.yaml添加H2+F flyway配置 | 10min | application.yaml |
| T5 | Spring Boot启动验证(集成测试) | 15min | GitsKnoApiApplicationTest |

### 批次2：业务链API（纯无人值守）

> API设计遵循合同CTR-API-001，不需要人类决策

| # | 任务 | 预估 | 输出 |
|---|---|---|---|
| T6 | CustomerJourneyController — 开户/查询/状态推进 | 30min | REST API |
| T7 | InteractionController — 交互记录CRUD | 20min | REST API |
| T8 | ClaimController — Claim CRUD + 状态推进 | 20min | REST API |
| T9 | CustomerJourneyOrchestrator → Spring @Service | 15min | Bean化 |
| T10 | 业务链集成测试(HTTP→H2→完整M17→M22) | 30min | E2eIT |

### 批次3：业务链剧情演示（需人类确认方向）

> 需要确认演示场景细节

| # | 任务 | 预估 | 需人类 |
|---|---|---|---|
| T11 | 王磊/鑫达贸易剧情脚本(curl序列) | 20min | 确认场景细节 |
| T12 | 剧情自动化验证脚本 | 15min | 否 |
| T13 | 端到端演示README | 10min | 否 |

### 批次4：后续场景（需人类决策优先级）

> M08-M16/M19的选择需要业务决策

| # | 任务 | 预估 | 需人类 |
|---|---|---|---|
| T14 | M19 产品知识解读模块骨架 | 30min | 选哪个模块 |
| T15 | M08-M16中的一个纵向切片 | 60min | 选哪个 |
| T16 | 交互链追溯(prevInteractionId) | 20min | 否 |

## 无人值守任务分配

批次1+2（T1-T10）共10个任务，预估3小时，可完全无人值守执行。
批次3的T12+T13也可无人值守。

启动方式：分3个cron job并行执行
- Job-A: T1-T5（基础设施）
- Job-B: T6-T10（API层，依赖Job-A完成）
- Job-C: T12-T13（演示脚本，依赖Job-B完成）
