# GITS 知识工程平台故障排查手册

> 文档编号：TROUBLESHOOTING  
> 版本：1.0  
> 编制日期：2026-08-05  
> 适用范围：GITS-KNO-DEV-PACKAGE-V0.1

---

## 1. 常见问题分类

### 1.1 启动失败

#### 问题：应用启动报端口占用

| 项目 | 内容 |
|------|------|
| **症状** | `Web server failed to start. Port 8080 was already in use.` |
| **原因** | 其他进程已占用 8080 端口 |
| **排查** | `lsof -i :8080` 或 `ss -tlnp \| grep 8080` |
| **解决** | 1. 终止占用进程：`kill -9 <PID>` <br> 2. 或修改端口：`export SERVER_PORT=8081` |

#### 问题：应用启动报 Bean 创建失败

| 项目 | 内容 |
|------|------|
| **症状** | `UnsatisfiedDependencyException` 或 `NoSuchBeanDefinitionException` |
| **原因** | 依赖注入缺失、配置类未扫描、条件注解不满足 |
| **排查** | 1. 查看完整异常堆栈 <br> 2. 确认 `@ComponentScan` 范围 <br> 3. 检查 `@ConditionalOn*` 条件 |
| **解决** | 1. 确认相关 Bean 所在包在扫描范围内 <br> 2. 检查 `application.yaml` 中相关配置是否启用 <br> 3. 检查 `@ConditionalOnBean` 依赖的 Bean 是否存在 |

#### 问题：Flyway 迁移校验失败

| 项目 | 内容 |
|------|------|
| **症状** | `FlywayValidateException: Migration checksum mismatch` |
| **原因** | 已执行的迁移脚本被修改 |
| **排查** | `./mvnw -pl apps/api flyway:info` 查看迁移状态 |
| **解决** | 1. **不要修改已执行的迁移脚本** <br> 2. 新增迁移脚本修复问题 <br> 3. 紧急情况：`flyway:repair` 后重新迁移（慎用） |

---

### 1.2 数据库连接问题

#### 问题：MySQL 连接超时

| 项目 | 内容 |
|------|------|
| **症状** | `Communications link failure`、`Connection timed out` |
| **原因** | MySQL 服务未启动、网络不通、防火墙阻断 |
| **排查** | 1. `docker compose ps mysql` 检查容器状态 <br> 2. `telnet <host> 3306` 检查网络连通性 <br> 3. `docker compose logs mysql` 查看MySQL日志 |
| **解决** | 1. 启动 MySQL：`docker compose up -d mysql` <br> 2. 检查防火墙规则 <br> 3. 确认 `MYSQL_URL` 配置正确 |

#### 问题：MySQL 认证失败

| 项目 | 内容 |
|------|------|
| **症状** | `Access denied for user` |
| **原因** | 用户名/密码错误、用户权限不足 |
| **排查** | `docker compose exec mysql mysql -u <user> -p -e "SELECT 1"` |
| **解决** | 1. 确认 `.env.staging` 中 `MYSQL_USER`/`MYSQL_PASSWORD` 配置 <br> 2. 重置密码：`ALTER USER 'ontos'@'%' IDENTIFIED BY 'new_password';` <br> 3. 检查用户权限：`SHOW GRANTS FOR 'ontos'@'%';` |

#### 问题：连接池耗尽

| 项目 | 内容 |
|------|------|
| **症状** | `HikariPool-1 - Connection is not available, request timed out` |
| **原因** | 连接泄漏、查询慢、连接池配置过小 |
| **排查** | 1. 查看连接池指标：`/actuator/metrics/hikaricp.connections.active` <br> 2. 检查慢查询日志 <br> 3. 检查 `leak-detection-threshold` 配置 |
| **解决** | 1. 增加 `hikari.maximum-pool-size` <br> 2. 优化慢查询 <br> 3. 开启泄漏检测：`hikari.leak-detection-threshold=60000` <br> 4. 检查是否有未关闭的事务 |

---

### 1.3 Oracle 只读数据源问题

#### 问题：Oracle 连接失败

| 项目 | 内容 |
|------|------|
| **症状** | `ORA-12154: TNS:could not resolve the connect identifier` 或 `ORA-17002: IO Error` |
| **原因** | Oracle 服务不可达、JDBC URL 错误、驱动版本不兼容 |
| **排查** | 1. 确认 `ORACLE_SOURCE_ENABLED=true` <br> 2. 检查 `ORACLE_JDBC_URL` 格式 <br> 3. 测试网络连通性 |
| **解决** | 1. 禁用 Oracle：`ORACLE_SOURCE_ENABLED=false` <br> 2. 修正 JDBC URL 格式：`jdbc:oracle:thin:@//host:1521/service` <br> 3. 确认 Oracle 19c 客户端兼容性 |

#### 问题：Oracle 轮询超时

| 项目 | 内容 |
|------|------|
| **症状** | Oracle Source Adapter 日志报轮询超时 |
| **原因** | 查询执行时间过长、网络延迟 |
| **排查** | 检查 `oracle.source.poll-interval` 配置 |
| **解决** | 1. 增大轮询间隔：`oracle.source.poll-interval=120s` <br> 2. 优化 Oracle 端查询 <br> 3. 检查 Oracle 端负载 |

---

### 1.4 LLM 调用问题

#### 问题：LLM 调用超时

| 项目 | 内容 |
|------|------|
| **症状** | API 请求返回 504，日志报 `Read timed out` |
| **原因** | LLM API 响应慢、网络延迟、请求内容过长 |
| **排查** | 1. 检查 LLM 健康状态：`/actuator/health` 中 `llm` 组件 <br> 2. 查看 `llm_client_call_seconds` 指标 <br> 3. 检查 `engagement.llm.timeout` 配置 |
| **解决** | 1. 增大超时：`engagement.llm.timeout=60` <br> 2. 切换到 mock 模式：`engagement.llm.mode=mock` <br> 3. 检查网络代理配置 |

#### 问题：LLM API Key 无效

| 项目 | 内容 |
|------|------|
| **症状** | `401 Unauthorized` 或 `Invalid API Key` |
| **原因** | API Key 未配置、已过期、或格式错误 |
| **排查** | 检查 `LLM_API_KEY` 环境变量是否设置 |
| **解决** | 1. 设置正确的 API Key：`export LLM_API_KEY=sk-xxx` <br> 2. 切换到 mock 模式：`export LLM_MODE=mock` <br> 3. 检查 API Key 是否有余额 |

#### 问题：LLM 返回格式错误

| 项目 | 内容 |
|------|------|
| **症状** | JSON 解析失败，`JsonProcessingException` |
| **原因** | LLM 返回内容不符合预期格式 |
| **排查** | 查看日志中 LLM 原始返回内容 |
| **解决** | 1. 系统会自动 fallback 到模板/正则逻辑 <br> 2. 检查 system prompt 是否正确 <br> 3. 考虑切换到更稳定的模型 |

---

### 1.5 CRM 回写问题

#### 问题：CRM 回写失败

| 项目 | 内容 |
|------|------|
| **症状** | 日志报 CRM 回写 HTTP 错误 |
| **原因** | CRM 服务不可达、认证失败、数据格式错误 |
| **排查** | 1. 检查 `engagement.crm.mode` 配置 <br> 2. 检查 `CRM_BASE_URL` 是否可访问 <br> 3. 查看 `crm_writeback_seconds` 指标 |
| **解决** | 1. 切换到 logging 模式：`engagement.crm.mode=logging` <br> 2. 检查 CRM 服务状态 <br> 3. 确认 `CRM_AUTH_TOKEN` 有效 <br> 4. 检查请求数据格式 |

#### 问题：CRM 回写数据不一致

| 项目 | 内容 |
|------|------|
| **症状** | CRM 中的数据与系统不一致 |
| **原因** | 网络抖动导致部分回写丢失、CRM 端处理异常 |
| **排查** | 1. 查看审计日志中的 CRM_WRITEBACK 事件 <br> 2. 对比系统内部报告与 CRM 数据 |
| **解决** | 1. 手动触发数据同步 <br> 2. 检查 CRM 回写接口幂等性 <br> 3. 增加重试机制 |

---

## 2. 日志查看命令

### 2.1 Docker 环境

```bash
# API 服务实时日志
docker compose -f docker-compose.staging.yaml logs -f api

# 最近 200 行日志
docker compose -f docker-compose.staging.yaml logs --tail 200 api

# 指定时间范围
docker compose -f docker-compose.staging.yaml logs \
  --since "2026-08-05T10:00:00" api

# 搜索错误日志
docker compose -f docker-compose.staging.yaml logs api 2>&1 | grep -i error

# 搜索特定异常
docker compose -f docker-compose.staging.yaml logs api 2>&1 | grep "Communications link failure"

# Worker 服务日志
docker compose -f docker-compose.staging.yaml logs -f worker

# 所有服务日志
docker compose -f docker-compose.staging.yaml logs -f
```

### 2.2 本地开发

```bash
# 查看 API 日志（Maven 运行）
./mvnw -pl apps/api spring-boot:run 2>&1 | tee api.log

# 过滤错误
tail -f api.log | grep -iE "error|exception|failed"

# 搜索特定模式
grep -n "HikariPool" api.log
```

### 2.3 关键日志模式

| 日志关键词 | 含义 | 严重程度 |
|-----------|------|---------|
| `HikariPool-1 - Connection is not available` | 连接池耗尽 | 高 |
| `Communications link failure` | 数据库连接失败 | 高 |
| `FlywayValidateException` | 迁移校验失败 | 高 |
| `LLM call failed` | LLM 调用失败 | 中 |
| `CRM writeback failed` | CRM 回写失败 | 中 |
| `Falling back to template` | LLM fallback 触发 | 低 |
| `AUDIT:` | 审计日志 | 信息 |
| `Claim candidate recorded` | 声明候选项记录 | 信息 |

---

## 3. 紧急联系流程

### 3.1 问题升级矩阵

| 严重等级 | 定义 | 响应时间 | 处理人 |
|---------|------|---------|--------|
| P0 - 紧急 | 系统完全不可用，影响所有用户 | 15 分钟 | 值班工程师 + 技术负责人 |
| P1 - 严重 | 核心功能不可用，影响大部分用户 | 30 分钟 | 值班工程师 |
| P2 - 一般 | 非核心功能异常，有临时替代方案 | 4 小时 | 相关模块负责人 |
| P3 - 轻微 | 体验问题，不影响业务 | 24 小时 | 相关模块负责人 |

### 3.2 紧急处理流程

```
发现问题
  ↓
确认严重等级（P0/P1 → 立即升级）
  ↓
通知值班工程师
  ↓
初步排查（查看日志、健康检查）
  ↓
┌─────────────────┐
│ 能否快速恢复？    │
├──── 是 ────┤──── 否 ────┐
↓            ↓            ↓
执行恢复    降级运行    升级至技术负责人
(重启/回滚) (切mock/   (组织攻坚)
            切logging)
  ↓            ↓            ↓
验证恢复    监控观察    根因分析
  ↓            ↓            ↓
编写事故报告              编写事故报告
```

### 3.3 降级运行策略

| 故障组件 | 降级策略 | 操作 |
|---------|---------|------|
| MySQL | 切换到 H2 内存库（仅开发） | `SPRING_PROFILES_ACTIVE=default` |
| LLM API | 切换到 mock 模式 | `LLM_MODE=mock` |
| CRM 回写 | 切换到 logging 模式 | `CRM_MODE=logging` |
| Oracle | 禁用 Oracle 数据源 | `ORACLE_SOURCE_ENABLED=false` |
| Zipkin | 禁用追踪 | 删除 `ZIPKIN_URL` 环境变量 |

### 3.4 快速恢复命令

```bash
# 重启 API 服务
docker compose -f docker-compose.staging.yaml restart api

# 重启所有服务
docker compose -f docker-compose.staging.yaml restart

# 完全重建
docker compose -f docker-compose.staging.yaml down
docker compose -f docker-compose.staging.yaml up -d

# 清理并重建（删除数据）
docker compose -f docker-compose.staging.yaml down -v
docker compose -f docker-compose.staging.yaml up -d
```

---

## 4. 诊断工具箱

### 4.1 健康检查脚本

```bash
#!/bin/bash
# health-check.sh - 一键健康检查

echo "=== GITS 知识工程平台健康检查 ==="
echo ""

# API 服务
echo "1. API 服务健康状态:"
curl -sf http://localhost:8080/actuator/health | jq . || echo "  [FAIL] API 服务不可达"
echo ""

# Worker 服务
echo "2. Worker 服务健康状态:"
curl -sf http://localhost:8090/actuator/health | jq . || echo "  [FAIL] Worker 服务不可达"
echo ""

# 数据库连接
echo "3. 数据库连接池:"
curl -sf http://localhost:8080/actuator/metrics/hikaricp.connections.active | jq '.measurements[0].value' || echo "  [FAIL] 无法获取指标"
echo ""

# LLM 状态
echo "4. LLM 组件状态:"
curl -sf http://localhost:8080/actuator/health | jq '.components.llm' || echo "  [FAIL] 无法获取 LLM 状态"
echo ""

# 磁盘空间
echo "5. 磁盘空间:"
df -h / | tail -1
echo ""

echo "=== 检查完成 ==="
```

### 4.2 常用排查命令

```bash
# 查看 JVM 内存使用
curl -sf http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements[0].value / 1024 / 1024 | floor' 

# 查看 HTTP 请求 QPS
curl -sf http://localhost:8080/actuator/metrics/http.server.requests | jq '.measurements[0].value'

# 查看活跃线程数
curl -sf http://localhost:8080/actuator/metrics/jvm.threads.live | jq '.measurements[0].value'

# 查看 GC 次数
curl -sf http://localhost:8080/actuator/metrics/jvm.gc.count | jq '.measurements[0].value'
```
