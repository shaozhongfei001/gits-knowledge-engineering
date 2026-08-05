# GITS 知识工程平台运维手册

> 文档编号：OPERATIONS-MANUAL  
> 版本：1.0  
> 编制日期：2026-08-05  
> 适用范围：GITS-KNO-DEV-PACKAGE-V0.1

---

## 1. 监控体系

### 1.1 Prometheus 指标采集

Prometheus 配置文件位于 `monitoring/prometheus.yml`，采集目标包括：

| 目标 | 地址 | 采集间隔 | 说明 |
|------|------|---------|------|
| API 服务 | `api:8080` | 15s | Spring Boot Actuator 指标 |
| Worker 服务 | `worker:8090` | 15s | Spring Boot Actuator 指标 |

Prometheus 采集端点：`/actuator/prometheus`

核心指标列表：

| 指标名 | 类型 | 说明 |
|--------|------|------|
| `http_server_requests_seconds_*` | Summary/Timer | HTTP 请求延迟 |
| `hikaricp_connections_active` | Gauge | 活跃数据库连接数 |
| `hikaricp_connections_idle` | Gauge | 空闲数据库连接数 |
| `hikaricp_connections_pending` | Gauge | 等待获取连接的线程数 |
| `jvm_memory_used_bytes` | Gauge | JVM 内存使用量 |
| `jvm_threads_live_threads` | Gauge | JVM 活跃线程数 |
| `system_cpu_usage` | Gauge | 系统 CPU 使用率 |
| `disk_total_bytes` / `disk_free_bytes` | Gauge | 磁盘空间 |
| `llm_client_call_seconds_*` | Timer | LLM 调用延迟 |
| `crm_writeback_seconds_*` | Timer | CRM 回写延迟 |

### 1.2 Grafana 仪表盘

Grafana 访问地址：`http://<host>:3000`

默认凭据：`admin / admin`（首次登录后需修改密码）

推荐仪表盘配置：

| 仪表盘名称 | 面板内容 |
|-----------|---------|
| GITS API 概览 | QPS、错误率、P50/P95/P99 延迟、活跃连接数 |
| GITS 数据库 | 连接池使用率、查询延迟、慢查询数 |
| GITS JVM | 堆内存、GC 次数/耗时、线程数 |
| GITS 业务指标 | LLM 调用次数/延迟、CRM 回写次数/延迟 |

Grafana 数据源配置：

```yaml
# Prometheus 数据源
Name: Prometheus
URL: http://prometheus:9090
Access: Server (default)
```

### 1.3 健康检查端点

| 端点 | 方法 | 说明 | 预期响应 |
|------|------|------|---------|
| `/actuator/health` | GET | 综合健康状态 | `{"status":"UP"}` |
| `/actuator/health/readiness` | GET | 就绪探针 | `{"status":"UP"}` |
| `/actuator/health/liveness` | GET | 存活探针 | `{"status":"UP"}` |
| `/actuator/info` | GET | 应用信息 | 版本、构建时间等 |
| `/actuator/prometheus` | GET | Prometheus 指标 | 文本格式指标 |

健康检查组件：

- `db`：数据库连接状态
- `diskSpace`：磁盘空间状态
- `ping`：应用存活状态
- `llm`：LLM 服务可达性

---

## 2. 告警规则

### 2.1 API 可用性告警

```yaml
# API 服务不可用
- alert: APIServiceDown
  expr: up{job="gits-api"} == 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "GITS API 服务不可用"
    description: "API 服务已停止响应超过 1 分钟"
```

### 2.2 错误率告警

```yaml
# 5xx 错误率超过阈值
- alert: HighErrorRate
  expr: |
    rate(http_server_requests_seconds_count{status=~"5.."}[5m])
    / rate(http_server_requests_seconds_count[5m]) > 0.05
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "API 5xx 错误率超过 5%"
    description: "最近 5 分钟 5xx 错误率为 {{ $value | humanizePercentage }}"
```

```yaml
# 4xx 错误率超过阈值
- alert: HighClientErrorRate
  expr: |
    rate(http_server_requests_seconds_count{status=~"4.."}[5m])
    / rate(http_server_requests_seconds_count[5m]) > 0.1
  for: 10m
  labels:
    severity: info
  annotations:
    summary: "API 4xx 错误率超过 10%"
```

### 2.3 延迟阈值告警

```yaml
# P95 延迟超过阈值
- alert: HighLatencyP95
  expr: |
    histogram_quantile(0.95,
      rate(http_server_requests_seconds_bucket[5m])
    ) > 2.0
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "API P95 延迟超过 2 秒"
    description: "当前 P95 延迟为 {{ $value }}s"
```

```yaml
# P99 延迟超过阈值
- alert: HighLatencyP99
  expr: |
    histogram_quantile(0.99,
      rate(http_server_requests_seconds_bucket[5m])
    ) > 5.0
  for: 3m
  labels:
    severity: critical
  annotations:
    summary: "API P99 延迟超过 5 秒"
```

### 2.4 数据库连接池告警

```yaml
# 连接池使用率超过 80%
- alert: HighConnectionPoolUsage
  expr: |
    hikaricp_connections_active / hikaricp_connections_max > 0.8
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "数据库连接池使用率超过 80%"
    description: "当前使用率 {{ $value | humanizePercentage }}"

# 连接泄漏检测
- alert: ConnectionLeakDetected
  expr: hikaricp_connections_pending > 5
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "数据库连接等待数异常"
    description: "当前等待连接数 {{ $value }}"
```

### 2.5 JVM 告警

```yaml
# 堆内存使用率超过 85%
- alert: HighHeapMemoryUsage
  expr: |
    jvm_memory_used_bytes{area="heap"} 
    / jvm_memory_max_bytes{area="heap"} > 0.85
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "JVM 堆内存使用率超过 85%"

# GC 频率过高
- alert: HighGCRate
  expr: rate(jvm_gc_pause_seconds_count[5m]) > 10
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "GC 频率异常，每分钟超过 10 次"
```

### 2.6 业务告警

```yaml
# LLM 调用失败率
- alert: LLMCallFailureRate
  expr: |
    rate(llm_client_call_seconds_count{outcome="error"}[5m])
    / rate(llm_client_call_seconds_count[5m]) > 0.1
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "LLM 调用失败率超过 10%"

# CRM 回写失败
- alert: CRMWritebackFailure
  expr: rate(crm_writeback_seconds_count{outcome="error"}[5m]) > 0
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "CRM 回写出现失败"
```

---

## 3. 日志管理

### 3.1 日志级别

| 日志级别 | 使用场景 | 生产建议 |
|---------|---------|---------|
| ERROR | 系统错误、异常 | 始终开启 |
| WARN | 潜在问题、降级 | 始终开启 |
| INFO | 关键业务操作 | 始终开启 |
| DEBUG | 调试信息 | 按需开启 |
| TRACE | 详细追踪 | 禁用 |

关键 Logger 级别配置：

```yaml
logging:
  level:
    root: INFO
    com.gien.gits: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: WARN
    org.flywaydb: INFO
    # 调试时按需开启
    # com.gien.gits.engagement: DEBUG
    # org.hibernate.SQL: DEBUG
```

### 3.2 日志格式

默认 JSON 格式输出（生产环境推荐）：

```json
{
  "timestamp": "2026-08-05T10:30:00.000Z",
  "level": "INFO",
  "logger": "com.gien.gits.api.controller.CustomerContextController",
  "message": "Customer context loaded",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "context": {
    "customerId": "CUST-001",
    "tenantId": "TENANT-001"
  }
}
```

### 3.3 日志收集

Docker 环境日志收集：

```bash
# 查看实时日志
docker compose -f docker-compose.staging.yaml logs -f api

# 查看最近 100 行日志
docker compose -f docker-compose.staging.yaml logs --tail 100 api

# 查看指定时间范围日志
docker compose -f docker-compose.staging.yaml logs \
  --since "2026-08-05T10:00:00" --until "2026-08-05T11:00:00" api

# 导出日志到文件
docker compose -f docker-compose.staging.yaml logs api > api-$(date +%Y%m%d).log
```

推荐日志收集架构：

```
应用容器 → Docker 日志驱动 → Fluentd/Filebeat → Elasticsearch → Kibana
```

---

## 4. 备份恢复

### 4.1 数据库备份策略

#### MySQL 备份

```bash
# 全量备份
docker compose -f docker-compose.staging.yaml exec mysql \
  mysqldump -u root -p gits_kno > backup_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
docker compose -f docker-compose.staging.yaml exec mysql \
  mysqldump -u root -p gits_kno | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

#### 定时备份脚本

```bash
#!/bin/bash
# /usr/local/bin/gits-backup.sh
BACKUP_DIR=/data/backups/mysql
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 执行备份
docker compose -f /opt/gits/docker-compose.staging.yaml exec -T mysql \
  mysqldump -u root -p${MYSQL_ROOT_PASSWORD} gits_kno \
  | gzip > ${BACKUP_DIR}/gits_kno_${TIMESTAMP}.sql.gz

# 清理过期备份
find $BACKUP_DIR -name "gits_kno_*.sql.gz" -mtime +${RETENTION_DAYS} -delete

echo "Backup completed: gits_kno_${TIMESTAMP}.sql.gz"
```

Cron 配置：

```cron
# 每天凌晨 2 点执行备份
0 2 * * * /usr/local/bin/gits-backup.sh >> /var/log/gits-backup.log 2>&1
```

#### 备份策略表

| 备份类型 | 频率 | 保留时间 | 存储位置 |
|---------|------|---------|---------|
| 全量备份 | 每日 | 30 天 | 本地 + 远程 |
| 增量备份 | 每小时 | 7 天 | 本地 |
| Binlog 归档 | 实时 | 7 天 | 本地 |

### 4.2 数据库恢复

```bash
# 从备份恢复
gunzip < backup_20260805_020000.sql.gz | \
  docker compose -f docker-compose.staging.yaml exec -T mysql \
  mysql -u root -p gits_kno

# 恢复前先创建数据库（如需）
docker compose -f docker-compose.staging.yaml exec mysql \
  mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gits_kno;"
```

### 4.3 配置备份

```bash
# 备份应用配置
tar czf config-backup-$(date +%Y%m%d).tar.gz \
  .env.staging \
  apps/api/src/main/resources/application.yaml \
  apps/api/src/main/resources/application-staging.yaml \
  monitoring/prometheus.yml \
  docker-compose.staging.yaml
```

---

## 5. 扩缩容策略

### 5.1 水平扩展

API 服务支持水平扩展，通过增加 Docker 容器实例实现：

```yaml
# docker-compose.staging.yaml 中扩展 API 实例
api:
  deploy:
    replicas: 3
    resources:
      limits:
        cpus: '2.0'
        memory: 2G
      reservations:
        cpus: '1.0'
        memory: 1G
```

注意事项：
- 扩展前确保数据库连接池配置足够（`hikari.maximum-pool-size`）
- 多实例需要配置负载均衡（Nginx upstream）
- Session 管理需使用无状态设计（已实现）

### 5.2 垂直扩展

| 资源 | 扩展指标 | 建议阈值 | 操作 |
|------|---------|---------|------|
| CPU | CPU 使用率 | > 70% 持续 15 分钟 | 增加CPU核心或升级实例 |
| 内存 | 堆内存使用率 | > 85% 持续 10 分钟 | 增加 JVM 堆内存或升级实例 |
| 磁盘 | 磁盘使用率 | > 80% | 扩展磁盘或清理日志 |
| 连接池 | 活跃连接/最大连接 | > 80% 持续 5 分钟 | 增加 `hikari.maximum-pool-size` |

### 5.3 JVM 参数调优

```bash
# 生产环境推荐 JVM 参数
java -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/data/heapdump.hprof \
  -jar gits-kno-api-0.1.0-SNAPSHOT.jar
```

---

## 6. 安全运维

### 6.1 API Key 轮转

详细流程参见 `docs/security/API-KEY-ROTATION.md`。

核心流程：

1. **生成新 Key**：生成新的 API Key
2. **配置轮转**：设置 `engagement.security.api-key-rotation` 和 `api-key-rotation-deadline`
3. **通知客户端**：告知下游系统在截止时间前切换到新 Key
4. **完成轮转**：截止时间后，旧 Key 自动失效

```yaml
# 轮转配置示例
engagement:
  security:
    api-key: "new-api-key-2026"
    api-key-rotation: "old-api-key-2025"
    api-key-rotation-deadline: "2026-09-01T00:00:00Z"
```

### 6.2 审计日志查询

系统关键操作均记录审计日志，可通过以下方式查询：

```bash
# 查询 API 访问日志
docker compose -f docker-compose.staging.yaml logs api | grep "AUDIT"

# 查询特定时间范围
docker compose -f docker-compose.staging.yaml logs api \
  --since "2026-08-05T10:00:00" | grep "AUDIT"

# 查询特定操作类型
docker compose -f docker-compose.staging.yaml logs api | grep "CLAIM_RECORDED"
```

审计事件类型：

| 事件 | 说明 |
|------|------|
| `CLAIM_CANDIDATE_RECORDED` | 声明候选项记录 |
| `CONTROLLED_ACTION_REQUESTED` | 受控行动请求 |
| `JOURNEY_STARTED` | 旅程启动 |
| `POSTVISIT_COMPLETED` | 访后分析完成 |
| `NEW_EVIDENCE` | 新证据录入 |
| `CRM_WRITEBACK` | CRM 回写 |

### 6.3 安全加固检查

详细安全加固指南参见 `docs/security/SECURITY-HARDENING.md`。

日常安全检查项：

- [ ] API Key 已配置且非默认值
- [ ] CORS 配置仅允许可信源
- [ ] 数据库密码已修改且强密码
- [ ] LLM API Key 安全存储
- [ ] Oracle 凭据安全存储
- [ ] Swagger UI 已在生产环境禁用
- [ ] H2 控制台已在生产环境禁用
- [ ] Actuator 端点仅暴露必要端点
- [ ] 追踪采样率已降低到合理水平
- [ ] 日志中不包含敏感信息

### 6.4 SSL/TLS 配置

生产环境必须启用 HTTPS：

```yaml
# application-staging.yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: gits-api
  port: 8443
```

Nginx SSL 配置：

```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/nginx/ssl/gits.crt;
    ssl_certificate_key /etc/nginx/ssl/gits.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    # ... 其他配置
}
```
