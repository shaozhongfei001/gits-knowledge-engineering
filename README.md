# gits-knowledge-engineering

杭州银行“知识工程体系和智能体基础能力建设项目”工程开发仓库候选骨架。

当前工程实现的是已确认技术方向的可启动底座：**可编译语义合同＋运行本体控制平面＋多种可重建投影**。它不是22个业务模块已经开发完成的声明，也不代表真实 AIOS、CRM、Oracle、IAM、RAG、模型网关或写回接口已经接通。

## 当前状态

```text
PACKAGE_ID=HZB-KNO-DEV-PACKAGE-V0.1
FRAMEWORK=HZB-SDD-FRAMEWORK-V0.2
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
