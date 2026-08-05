# GITS 知识工程平台部署手册

> 文档编号：DEPLOYMENT-GUIDE  
> 版本：1.0  
> 编制日期：2026-08-05  
> 适用范围：GITS-KNO-DEV-PACKAGE-V0.1

---

## 1. 环境要求

### 1.1 硬件要求

| 环境 | CPU | 内存 | 磁盘 | 说明 |
|------|-----|------|------|------|
| 开发 | 2核 | 4 GB | 20 GB | 单机运行 |
| Staging | 4核 | 8 GB | 50 GB | Docker Compose 全栈 |
| 生产 | 8核+ | 16 GB+ | 100 GB+ | 按负载水平调整 |

### 1.2 软件要求

| 软件 | 最低版本 | 推荐版本 | 用途 |
|------|---------|---------|------|
| Java (JDK) | 21 | 21 (Eclipse Temurin) | 后端运行时 |
| Node.js | 20 | 24 | 前端构建 |
| Maven | 3.9+ | 3.9+ | Java 构建工具 |
| Docker | 24+ | 27+ | 容器化部署 |
| Docker Compose | 2.20+ | 2.30+ | 多容器编排 |
| MySQL | 8.0 | 8.4 | 主数据库（Staging/生产） |
| Oracle Client | 19c | 19c | Oracle 数据源连接（可选） |
| curl | 7.x+ | 最新 | 健康检查 |

### 1.3 网络要求

| 端口 | 服务 | 说明 |
|------|------|------|
| 8080 | API 服务 | 后端 REST API |
| 8090 | Worker 服务 | 后台任务处理 |
| 80/443 | Nginx | 前端静态资源 |
| 3306 | MySQL | 数据库 |
| 9090 | Prometheus | 指标采集 |
| 3000 | Grafana | 监控仪表盘 |
| 9411 | Zipkin | 分布式追踪 |

---

## 2. 配置说明

### 2.1 application.yaml 关键配置项

后端配置文件位于 `apps/api/src/main/resources/application.yaml`，核心配置项如下：

#### 数据源配置

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:gitskno;DB_CLOSE_DELAY=-1;MODE=MySQL    # 开发环境（H2内存库）
    driver-class-name: org.h2.Driver
    username: sa
    password:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
```

Staging 环境通过 `application-staging.yaml` 覆盖为 MySQL：

```yaml
spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://mysql:3306/gits_kno?useSSL=true&allowPublicKeyRetrieval=true}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: ${HIKARI_MAX_POOL:15}
      minimum-idle: ${HIKARI_MIN_IDLE:3}
      idle-timeout: ${HIKARI_IDLE_TIMEOUT:300000}
      connection-timeout: ${HIKARI_CONN_TIMEOUT:30000}
      leak-detection-threshold: ${HIKARI_LEAK_DETECTION:60000}
```

#### LLM 配置

```yaml
engagement:
  llm:
    mode: mock                          # mock | real
    base-url: "${LLM_BASE_URL:https://api.openai.com}"
    api-key: "${LLM_API_KEY:}"
    model: "${LLM_MODEL:gpt-4o-mini}"
    timeout: 30
```

- `mode=mock`：使用 MockLlmClient，返回预设结构化 JSON，无需外部 API 调用
- `mode=real`：使用 RealLlmClient，调用外部 LLM API

#### CRM 回写配置

```yaml
engagement:
  crm:
    mode: logging                       # logging | http
    writeback-url: ""
    auth-token: "${CRM_AUTH_TOKEN:}"
    timeout: 30
```

- `mode=logging`：仅记录日志，不实际调用 CRM
- `mode=http`：通过 REST 调用 CRM 回写接口

#### 安全配置

```yaml
engagement:
  security:
    api-key: ""                         # 空=禁用认证（开发模式）
    api-key-rotation: ""                # 轮转 Key
    api-key-rotation-deadline: ""       # 轮转截止时间（ISO 8601）
    cors:
      allowed-origins: "http://localhost:5173,http://localhost:8080"
```

#### 可观测性配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when-authorized
  tracing:
    sampling:
      probability: 1.0                  # 开发100%采样，生产建议0.1-0.5
  zipkin:
    tracing:
      endpoint: "${ZIPKIN_URL:http://localhost:9411/api/v2/spans}"
```

#### Oracle 数据源配置（可选）

```yaml
oracle:
  source:
    enabled: ${ORACLE_SOURCE_ENABLED:false}
    jdbc-url: ${ORACLE_JDBC_URL:}
    username: ${ORACLE_USER:}
    password: ${ORACLE_PASSWORD:}
    poll-interval: ${ORACLE_POLL_INTERVAL:60s}
```

### 2.2 环境变量

所有敏感配置通过环境变量注入，不硬编码到配置文件中：

| 环境变量 | 必填 | 说明 | 示例 |
|---------|------|------|------|
| `MYSQL_USER` | Staging必填 | MySQL 用户名 | `ontos` |
| `MYSQL_PASSWORD` | Staging必填 | MySQL 密码 | — |
| `MYSQL_ROOT_PASSWORD` | Staging必填 | MySQL root 密码 | — |
| `LLM_API_KEY` | LLM_MODE=real时必填 | LLM API 密钥 | — |
| `LLM_BASE_URL` | 否 | LLM API 地址 | `https://api.openai.com` |
| `LLM_MODEL` | 否 | LLM 模型名称 | `gpt-4o-mini` |
| `LLM_MODE` | 否 | LLM 模式 | `mock` / `real` |
| `CRM_BASE_URL` | CRM_MODE=http时必填 | CRM 回写 URL | — |
| `CRM_AUTH_TOKEN` | 否 | CRM 认证令牌 | — |
| `CRM_MODE` | 否 | CRM 模式 | `logging` / `http` |
| `API_KEY` | 否 | API 访问密钥 | — |
| `ORACLE_SOURCE_ENABLED` | 否 | 是否启用 Oracle | `true` / `false` |
| `ORACLE_JDBC_URL` | Oracle启用时必填 | Oracle JDBC URL | — |
| `ORACLE_USER` | Oracle启用时必填 | Oracle 用户名 | — |
| `ORACLE_PASSWORD` | Oracle启用时必填 | Oracle 密码 | — |
| `CORS_ALLOWED_ORIGINS` | 否 | CORS 允许的源 | `*` |
| `ZIPKIN_URL` | 否 | Zipkin 端点 | — |
| `TRACE_SAMPLING` | 否 | 追踪采样率 | `0.5` |

### 2.3 .env 模板

Staging 环境模板文件 `.env.staging.template`：

```bash
# === MySQL ===
MYSQL_DATABASE=gits_kno
MYSQL_USER=ontos
MYSQL_PASSWORD=           # 必填
MYSQL_ROOT_PASSWORD=      # 必填

# === Oracle (可选) ===
ORACLE_SOURCE_ENABLED=false
ORACLE_JDBC_URL=          # 如: jdbc:oracle:thin:@//host:1521/service
ORACLE_USER=
ORACLE_PASSWORD=

# === LLM ===
LLM_MODE=real
LLM_BASE_URL=https://api.openai.com
LLM_API_KEY=              # 必填 (当 LLM_MODE=real)
LLM_MODEL=gpt-4o-mini

# === CRM ===
CRM_MODE=http
CRM_BASE_URL=             # 必填 (当 CRM_MODE=http)
CRM_AUTH_TOKEN=

# === 安全 ===
API_KEY=
CORS_ALLOWED_ORIGINS=*

# === 端口 ===
API_PORT=8080
WORKER_PORT=8090
NGINX_PORT=80
MYSQL_PORT=3306
ZIPKIN_PORT=9411
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000

# === 可观测性 ===
TRACE_SAMPLING=0.5
GRAFANA_ADMIN_PASSWORD=admin
```

使用方法：

```bash
cp .env.staging.template .env.staging
# 编辑 .env.staging 填写真实配置
vi .env.staging
```

---

## 3. 启动步骤

### 3.1 Docker Compose 启动（Staging 环境）

#### 前置条件

- Docker 和 Docker Compose 已安装
- `.env.staging` 已配置

#### 启动命令

```bash
# 1. 克隆代码仓库
git clone <repository-url>
cd gits-knowledge-engineering

# 2. 准备环境配置
cp .env.staging.template .env.staging
vi .env.staging  # 填写必填项

# 3. 构建并启动所有服务
docker compose -f docker-compose.staging.yaml --env-file .env.staging up -d

# 4. 查看服务状态
docker compose -f docker-compose.staging.yaml ps

# 5. 查看日志
docker compose -f docker-compose.staging.yaml logs -f api
```

#### 服务启动顺序

```
mysql → api (健康检查通过) → worker + nginx + prometheus + grafana + zipkin
```

API 服务健康检查配置：

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -sf http://localhost:8080/actuator/health/readiness || exit 1"]
  interval: 15s
  timeout: 10s
  retries: 5
  start_period: 60s
```

#### 停止服务

```bash
docker compose -f docker-compose.staging.yaml down

# 停止并删除数据卷（慎用）
docker compose -f docker-compose.staging.yaml down -v
```

### 3.2 手动启动（开发环境）

#### 后端启动

```bash
# 1. 构建项目
./mvnw clean package -DskipTests

# 2. 启动 API 服务（默认使用 H2 内存数据库）
java -jar apps/api/target/gits-kno-api-0.1.0-SNAPSHOT.jar

# 3. 或使用 Maven 直接运行
./mvnw -pl apps/api spring-boot:run
```

#### 前端启动

```bash
cd frontend

# 1. 安装依赖
npm install

# 2. 开发模式启动
npm run dev

# 3. 生产构建
npm run build
```

#### Worker 启动

```bash
# 设置端口
export SERVER_PORT=8090
java -jar apps/worker/target/gits-kno-worker-0.1.0-SNAPSHOT.jar
```

### 3.3 Staging Profile 启动

Staging 环境使用 `staging` Spring Profile，连接真实 MySQL 数据库和外部服务：

```bash
# 通过环境变量激活 staging profile
export SPRING_PROFILES_ACTIVE=staging
export MYSQL_URL=jdbc:mysql://localhost:3306/gits_kno?useSSL=true&allowPublicKeyRetrieval=true
export MYSQL_USER=ontos
export MYSQL_PASSWORD=<your-password>

java -jar apps/api/target/gits-kno-api-0.1.0-SNAPSHOT.jar
```

Staging Profile 关键差异：

| 配置项 | 默认（开发） | Staging |
|--------|------------|---------|
| 数据库 | H2 内存库 | MySQL 8.4 |
| H2 控制台 | 启用 | 禁用 |
| 种子数据 | 启用 | 禁用 |
| LLM 模式 | mock | real |
| CRM 模式 | logging | http |
| Swagger UI | 启用 | 禁用 |
| 追踪采样率 | 100% | 50% |
| 连接池大小 | 5 | 15 |

---

## 4. 数据库初始化

### 4.1 Flyway 迁移

项目使用 Flyway 管理数据库版本迁移：

- H2 迁移脚本：`apps/api/src/main/resources/db/migration/h2/`
- MySQL 迁移脚本：`apps/api/src/main/resources/db/migration/mysql/`

Flyway 在应用启动时自动执行迁移：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration/h2    # 开发环境
    baseline-on-migrate: true
    baseline-version: "0"                    # Staging 环境
```

#### 手动迁移

```bash
# 使用 Maven Flyway 插件
./mvnw -pl apps/api flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/gits_kno \
  -Dflyway.user=ontos -Dflyway.password=<password>
```

#### 迁移状态检查

```bash
# 使用 Maven Flyway 插件
./mvnw -pl apps/api flyway:info -Dflyway.url=jdbc:mysql://localhost:3306/gits_kno \
  -Dflyway.user=ontos -Dflyway.password=<password>
```

### 4.2 种子数据

开发环境默认加载种子数据（`gits.seed.enabled=true`），包含：

- 示例客户数据
- 示例旅程记录
- 示例知识规则

Staging/生产环境禁用种子数据（`gits.seed.enabled=false`）。

也可通过 API 端点手动触发种子数据加载：

```bash
curl -X POST http://localhost:8080/api/seed/data
```

---

## 5. 健康检查验证

### 5.1 Actuator 端点

| 端点 | 说明 | 认证 |
|------|------|------|
| `GET /actuator/health` | 综合健康状态 | 否 |
| `GET /actuator/health/readiness` | 就绪探针 | 否 |
| `GET /actuator/health/liveness` | 存活探针 | 否 |
| `GET /actuator/info` | 应用信息 | 否 |
| `GET /actuator/metrics` | 指标列表 | 否 |
| `GET /actuator/prometheus` | Prometheus 指标 | 否 |

### 5.2 健康检查命令

```bash
# 检查 API 服务健康状态
curl -sf http://localhost:8080/actuator/health | jq .

# 检查就绪状态
curl -sf http://localhost:8080/actuator/health/readiness

# 检查存活状态
curl -sf http://localhost:8080/actuator/health/liveness

# 检查 Worker 服务
curl -sf http://localhost:8090/actuator/health/readiness
```

### 5.3 健康状态说明

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" },
    "llm": { "status": "UP" }
  }
}
```

- `UP`：组件正常
- `DOWN`：组件异常
- `OUT_OF_SERVICE`：组件已离线
- `DEGRADED`：组件降级运行

### 5.4 Docker 健康检查

Docker Compose 配置了自动健康检查：

```bash
# 查看容器健康状态
docker compose -f docker-compose.staging.yaml ps

# 输出示例：
# NAME    STATUS
# api     Up 2 minutes (healthy)
# worker  Up 1 minute (healthy)
# mysql   Up 3 minutes (healthy)
```

---

## 6. 常见部署问题

### 6.1 MySQL 连接失败

**症状**：API 服务日志报 `Communications link failure` 或 `Access denied`

**原因**：
- MySQL 服务未启动
- 密码配置错误
- 网络不通

**解决方案**：

```bash
# 检查 MySQL 容器状态
docker compose -f docker-compose.staging.yaml ps mysql

# 检查 MySQL 连接
docker compose -f docker-compose.staging.yaml exec mysql \
  mysql -u ontos -p -e "SELECT 1"

# 检查环境变量
docker compose -f docker-compose.staging.yaml exec api env | grep MYSQL
```

### 6.2 Flyway 迁移失败

**症状**：启动日志报 `FlywayException` 或 `Migration validation failed`

**原因**：
- 迁移脚本版本冲突
- 数据库状态与迁移脚本不一致

**解决方案**：

```bash
# 查看迁移状态
./mvnw -pl apps/api flyway:info

# 修复迁移校验（慎用）
./mvnw -pl apps/api flyway:repair

# 重新执行迁移
./mvnw -pl apps/api flyway:migrate
```

### 6.3 LLM API 调用失败

**症状**：API 返回 502/503，日志报 LLM 调用超时或认证失败

**原因**：
- API Key 未配置或无效
- 网络无法访问 LLM API
- 超时配置过短

**解决方案**：

```bash
# 切换到 mock 模式
export LLM_MODE=mock

# 检查 LLM 健康状态
curl http://localhost:8080/actuator/health | jq .components.llm

# 增加超时时间
export LLM_TIMEOUT=60
```

### 6.4 Oracle 数据源连接失败

**症状**：日志报 Oracle 连接异常

**原因**：
- Oracle 未启用或配置错误
- JDBC 驱动版本不兼容
- 网络不通

**解决方案**：

```bash
# 禁用 Oracle 数据源
export ORACLE_SOURCE_ENABLED=false

# 检查配置
docker compose -f docker-compose.staging.yaml exec api env | grep ORACLE
```

### 6.5 端口冲突

**症状**：启动报 `Port 8080 already in use`

**解决方案**：

```bash
# 查找占用端口的进程
lsof -i :8080

# 修改端口
export API_PORT=8081
```

### 6.6 前端构建失败

**症状**：`npm run build` 报错

**解决方案**：

```bash
cd frontend

# 清理缓存
rm -rf node_modules package-lock.json
npm install

# 检查 Node 版本
node --version  # 需要 20+

# 重新构建
npm run build
```

### 6.7 Docker 构建失败

**症状**：`docker compose build` 报 Maven 依赖下载失败

**解决方案**：

```bash
# 先在本地构建
./mvnw clean package -DskipTests

# 使用本地构建结果
docker compose -f docker-compose.staging.yaml build --no-cache
```

---

## 7. 部署检查清单

部署完成后，按以下清单逐项验证：

- [ ] MySQL 数据库可连接，Flyway 迁移成功
- [ ] API 服务健康检查返回 `UP`
- [ ] Worker 服务健康检查返回 `UP`
- [ ] 前端页面可通过 Nginx 访问
- [ ] LLM 调用正常（或 mock 模式运行）
- [ ] CRM 回写正常（或 logging 模式运行）
- [ ] Prometheus 指标可采集
- [ ] Grafana 仪表盘可访问
- [ ] Zipkin 追踪可访问
- [ ] API Key 认证已配置（生产环境）
- [ ] CORS 配置正确
- [ ] 种子数据已加载（开发环境）或已禁用（生产环境）
