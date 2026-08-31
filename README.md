# GITS-CBanking(Corporate Banking 虚拟对公银行)

GITS BANK 不是现实世界中的银行，是存在于数字虚拟世界中的银行镜像，模拟对公银行如何利用知识工程体系支撑智能体化的对公业务。

当前工程实现的是已确认技术方向的可启动底座：**可编译语义合同＋运行本体控制平面＋多种可重建投影**。它不是22个业务模块已经开发完成的声明，也不代表真实 AIOS、CRM、Oracle、IAM、RAG、模型网关或写回接口已经接通。

## 当前状态

```text
PACKAGE_ID=GITS-KNO-DEV-PACKAGE-V0.1
FRAMEWORK=GITS-SDD-FRAMEWORK-V0.2
STATUS=DEV_PACKAGE_CANDIDATE
QA_GATE=PENDING_INDEPENDENT_QA
PRODUCTION_READY=NO
FROZEN=NO
```

## 架构边界

- Java 21、Spring Boot 3.5.16、Spring Modulith 1.4.12；
- Maven多模块的模块化单体 API 与独立 Worker；
- Apache Jena 6.2.0封装在端口后，关系库存放权威运行状态；
- OpenAPI、AsyncAPI、语义、规则、Skill、Action、Evidence、Evaluation等合同统一由 `specs/CONTRACT_INDEX.yaml` 管理；
- AI只能产生候选 Claim/Proposal，正式 Action 必须经过人工确认、权限复核、幂等、原值校验和回执；
- 图、搜索、向量和分析宽表均属于可重建投影，不是权威事实源；
- Apache Ossie只允许作为实验性交换适配器，不是核心运行时依赖。

## 首次使用

```bash
make bootstrap-check
make generate
make check
make backend-test
make frontend-test
make new-loop LOOP=P1-first-slice HOLDER=tech_lead
```

`make bootstrap-check`要求Java 21、Maven Wrapper（仓库自带 `./mvnw`，首次需联网下载 Maven 3.9+）、Node 22+、npm、Python 3.11+、Git与ripgrep。工具缺失或版本不符会非零退出，不会“软通过”。

## 首期纵向验证

总体设计仍覆盖4个领域、22个模块和206项功能候选。首期工程只沿以下链路做真实验证：

```text
经营触发 → 访前准备 → 互动记录 → 访后分析 → 持续经营 → 反馈评测
```

对应重点模块为 M17、M18、M20、M21、M22，并由 M01—M07公共支撑能力托底。模块身份、范围和阶段见 `docs/governance/MODULE_CATALOG.md`。

## 目录

| 目录 | 责任 |
|---|---|
| `specs/` | 长期SSOT、合同源、基线索引与生成物 |
| `apps/` | API启动应用和独立Worker |
| `modules/` | 运行本体、语义、证据上下文、Action和评测模块 |
| `adapters/` | Jena、关系库、AIOS及外部系统防腐层 |
| `frontend/` | Vue 3岗位工作台入口；展示结构仅限Experience/BFF边界 |
| `docs/dispatch/` | 批次派工，不复制PRD和合同正文 |
| `loops/` | Loop状态、证据、失败、交接与Baton |
| `tools/quarantine/` | 未获准默认启用的Oracle/Ossie等隔离资产 |

## 证据纪律

- `make generate`只从合同权威源生成；`generated/`只读；
- `make check`会重新编译到临时目录并比较哈希；
- 任何红测先进入 `FAILURES.md`/ITER，再修复；
- 开发自检只能写 `DEV_SELF_CHECK_PASS`，不得写 `QA_PASS`；
- 真实E2E必须由独立角色在真实环境留存命令、日志、截图或回执证据。

## 端到端业务链演示

基于"王磊/鑫达贸易"业务场景，验证M17→M22完整客户旅程链路。

### 快速启动（H2内存数据库，默认）

```bash
# 1. 编译
./mvnw clean install -DskipTests

# 2. 运行端到端集成测试
./mvnw test -pl apps/api

# 3. 交互式curl演示
./scripts/e2e-demo.sh
```

### 使用MySQL

```bash
# 1. 确保MySQL已启动，配置见 apps/api/src/main/resources/application-mysql.yaml
# 2. curl演示脚本指定mysql profile
./scripts/e2e-demo.sh mysql

# 3. 或直接启动应用
./mvnw spring-boot:run -pl apps/api -Dspring-boot.run.profiles=mysql
```

### 业务链路说明

| 步骤 | 模块 | API | 说明 |
|------|------|-----|------|
| 开户 | M17 | `POST /api/journey/open` | 创建CustomerJourney，阶段=KYC_COLLECT |
| 查询 | M17 | `GET /api/journey/{id}` | 查询旅程详情 |
| 交互 | M17 | `POST /api/interaction` | 创建交互记录（电话、拜访等） |
| 主张 | M18 | `POST /api/claim` | 创建AI候选主张 |
| 确认 | M18 | `POST /api/claim/{id}/status` | 人工确认推进主张状态 |
| 推进 | M17-M22 | `POST /api/journey/{id}/advance` | 推进旅程阶段 |

完整5步链路（signal→insight→product match→previsit→postvisit）通过`CustomerJourneyService`方法调用实现，JourneyPhase完整流转：

```text
KYC_COLLECT → INSIGHT_ANALYSIS → PRODUCT_MATCHING → PREVISIT_PREP → POSTVISIT_REVIEW → COMPLETED
```

### 集成测试覆盖

| 测试类 | 测试数 | 覆盖范围 |
|--------|--------|----------|
| `FullChainE2eIT` | 6 | HTTP→H2→M17→M22完整链路 + API独立验证 + DB完整性 |
| `PersistenceIntegrationTest` | 5 | JDBC Repository CRUD + Flyway迁移 |
| `ArchitectureStatusContractTest` | 1 | 架构状态合同 |
| `ArchitectureStatusControllerTest` | 1 | 架构状态REST端点 |
| `MechanismE2eIT` | 1 | 机制端到端验证 |
