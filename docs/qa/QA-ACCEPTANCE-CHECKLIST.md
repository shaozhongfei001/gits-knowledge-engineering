# QA验收检查清单

> 版本: V1.0 | 日期: 2026-08-06 | 项目: GITS知识工程

## 使用说明

QA独立验收人员逐项检查，每项标记 ✅(通过) / ❌(失败) / N/A(不适用)。
所有必选项通过后，方可签署QA_PASS。

---

## 1. 构建与部署 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 1.1 | Maven构建通过 | `./mvnw verify -Ddependency-check.skip=true` | |
| 1.2 | 全部单元测试通过 | 构建日志中0 Failures, 0 Errors | |
| 1.3 | JaCoCo覆盖率≥80% | 构建日志中JaCoCo行覆盖率≥0.80 | |
| 1.4 | Docker镜像构建成功 | `docker build -t gits-kno-api .` | |
| 1.5 | 生产Docker Compose启动 | `docker compose -f docker-compose.prod.yaml up -d` | |
| 1.6 | 所有容器健康 | `docker compose -f docker-compose.prod.yaml ps` 全部healthy | |

## 2. 健康检查 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 2.1 | Health端点正常 | `GET /actuator/health` → 200 | |
| 2.2 | Readiness探针正常 | `GET /actuator/health/readiness` → 200 | |
| 2.3 | Liveness探针正常 | `GET /actuator/health/liveness` → 200 | |
| 2.4 | MySQL连接正常 | Health响应中db.status=UP | |
| 2.5 | Prometheus指标可采集 | `GET /actuator/prometheus` → 200 | |

## 3. API功能 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 3.1 | 客户上下文CRUD | POST/GET/PUT/DELETE /api/v1/customer-contexts | |
| 3.2 | 客户旅程CRUD | POST/GET /api/v1/engagement-journeys | |
| 3.3 | 知识规则CRUD | POST/GET/PUT/DELETE /api/v1/knowledge-rules | |
| 3.4 | KYC洞察CRUD | POST/GET /api/v1/kyc-insights | |
| 3.5 | 运营案例CRUD | POST/GET /api/v1/operating-cases | |
| 3.6 | 分页查询正常 | GET各端点?size=10&page=0 | |
| 3.7 | 输入校验生效 | POST空body → 400 | |
| 3.8 | 全局异常处理 | 请求不存在资源 → 404, 标准错误格式 | |

## 4. 安全 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 4.1 | API Key认证生效 | 无Key请求 → 401/403 | |
| 4.2 | API Key轮转支持 | 配置ROTATION_KEY后新旧Key均可用 | |
| 4.3 | Swagger UI禁用 | `GET /swagger-ui.html` → 404 | |
| 4.4 | H2控制台禁用 | `GET /h2-console` → 404 | |
| 4.5 | CORS配置生效 | 非允许域请求被拒绝 | |
| 4.6 | 健康详情不暴露 | Health响应无组件详情 | |

## 5. 业务链路 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 5.1 | 客户旅程完整流程 | 创建上下文→启动旅程→查询状态 | |
| 5.2 | KYC洞察声明流程 | 创建上下文→记录声明→查询洞察 | |
| 5.3 | 知识规则触发 | 创建规则→匹配条件→触发动作 | |
| 5.4 | LLM降级正常 | LLM不可用时自动降级到Mock | |
| 5.5 | CRM降级正常 | CRM不可用时自动降级到日志 | |

## 6. 性能基线 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 6.1 | 客户上下文P95<500ms | k6测试 | |
| 6.2 | 客户旅程P95<2s | k6测试 | |
| 6.3 | 知识规则P95<500ms | k6测试 | |
| 6.4 | 并发50无错误 | k6 ramping测试 | |

## 7. 可观测性 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 7.1 | Prometheus指标正常 | Grafana可查看API指标 | |
| 7.2 | Zipkin链路追踪 | 请求后可在Zipkin查看链路 | |
| 7.3 | 日志输出正常 | API日志包含结构化信息 | |
| 7.4 | 日志滚动配置 | 日志文件按大小/时间滚动 | |

## 8. 数据与迁移 (必选)

| # | 检查项 | 验证方法 | 结果 |
|---|--------|----------|------|
| 8.1 | Flyway迁移成功 | 启动日志无迁移错误 | |
| 8.2 | 种子数据未加载 | 生产环境无种子数据 | |
| 8.3 | 数据持久化 | 重启后数据不丢失 | |

---

## 验收签署

| 角色 | 姓名 | 签署日期 | 结果 |
|------|------|----------|------|
| 独立QA | | | PASS / FAIL |
| 项目经理 | | | 确认 |

**说明**: 
- 所有"必选"项必须通过
- 如有N/A项需附书面理由
- FAIL项需记录缺陷并修复后重新验收
