# Deployment Architecture

## Production Deployment Topology

```
                    ┌─────────────┐
                    │   Nginx     │
                    │  (reverse   │
                    │   proxy)    │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌──▼──────────┐ │
       │  API Server │ │   Worker    │ │
       │  (Spring    │ │  (Spring    │ │
       │   Boot)     │ │   Boot)     │ │
       └──────┬──────┘ └──────┬──────┘ │
              │               │        │
              └────────┬──────┘        │
                       │               │
              ┌────────▼────────┐      │
              │     MySQL 8.x   │      │
              │  (primary +     │      │
              │   replica)      │      │
              └────────┬────────┘      │
                       │               │
       ┌───────────────┼───────────────┤
       │               │               │
┌──────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
│ Prometheus  │ │   Zipkin    │ │  Grafana   │
│ (metrics    │ │  (tracing)  │ │(dashboards)│
│  scrape)    │ │             │ │            │
└─────────────┘ └─────────────┘ └────────────┘
```

### Component Details

| Component | Image | Port | Purpose |
|-----------|-------|------|---------|
| API Server | `gits-kno-api` | 8080 | REST API, Swagger UI, Actuator endpoints |
| Worker | `gits-kno-worker` | 8081 | Background job processing |
| MySQL | `mysql:8.0` | 3306 | Primary data store |
| Nginx | `nginx:alpine` | 80/443 | TLS termination, static frontend, reverse proxy |
| Prometheus | `prom/prometheus` | 9090 | Metrics scraping and alerting |
| Zipkin | `openzipkin/zipkin` | 9411 | Distributed trace collection and UI |
| Grafana | `grafana/grafana` | 3000 | Metrics dashboards |

## Environment Variables

### API Server / Worker (Common)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | `jdbc:h2:mem:gitskno` | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | (empty) | Database password |
| `SPRING_PROFILES_ACTIVE` | No | (none) | Active Spring profiles (`mysql`, `prod`) |
| `LLM_BASE_URL` | No | `https://api.openai.com` | LLM API base URL |
| `LLM_API_KEY` | Yes (prod) | (empty) | LLM API key |
| `LLM_MODEL` | No | `gpt-4o-mini` | LLM model identifier |
| `CRM_WRITEBACK_URL` | No | (empty) | CRM writeback endpoint URL |
| `CRM_AUTH_TOKEN` | No | (empty) | CRM authentication token |
| `ZIPKIN_URL` | No | `http://localhost:9411/api/v2/spans` | Zipkin endpoint |
| `ENGAGEMENT_SECURITY_API_KEY` | No | (empty) | API key for endpoint auth (empty = disabled) |
| `ENGAGEMENT_SECURITY_CORS_ALLOWED_ORIGINS` | No | `http://localhost:5173` | CORS allowed origins |

### MySQL

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | Yes | - | Root password |
| `MYSQL_DATABASE` | Yes | `gitskno` | Database name |
| `MYSQL_USER` | Yes | `gitskno` | Application user |
| `MYSQL_PASSWORD` | Yes | - | Application user password |

## Monitoring & Alerting Strategy

### Metrics (Prometheus)

**Application Metrics** (scraped from `/actuator/prometheus`):

| Metric | Alert Threshold | Description |
|--------|-----------------|-------------|
| `http_server_requests_seconds` | p99 > 2s | API response latency |
| `jdbc_connections_active` | > 80% pool | Database connection saturation |
| `jvm_memory_used_bytes` | > 85% max | JVM memory pressure |
| `process_cpu_usage` | > 80% | CPU utilization |
| `gits_evaluation_score` | - | Business metric: evaluation scores |

**Infrastructure Metrics** (node exporter / cAdvisor):

| Metric | Alert Threshold | Description |
|--------|-----------------|-------------|
| `node_disk_io_time_seconds` | p99 > 100ms | Disk I/O latency |
| `container_memory_usage_bytes` | > 90% limit | Container memory |
| `node_network_transmit_bytes` | - | Network throughput |

### Alerting Rules

1. **API Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05` → PagerDuty
2. **High Latency**: `histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 2` → Slack
3. **DB Connection Pool Exhaustion**: `jdbc_connections_active / jdbc_connections_max > 0.8` → Slack
4. **JVM OOM Risk**: `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85` → PagerDuty

### Tracing (Zipkin)

- 100% trace sampling in development; 10% in production (configurable via `management.tracing.sampling.probability`).
- Critical paths traced: API requests, LLM calls, CRM writeback, DMN evaluation.
- Trace IDs propagated in response headers (`X-Trace-Id`) for support correlation.

### Logging

- Structured JSON via `logstash-logback-encoder`.
- Log levels: `ERROR` (alerts), `WARN` (investigation), `INFO` (audit trail), `DEBUG` (development only).
- Trace/span IDs injected via `Slf4jScopeDecorator` for log-trace correlation.

## Capacity Planning

### Recommended Production Resources

| Component | CPU | Memory | Disk | Instances |
|-----------|-----|--------|------|-----------|
| API Server | 2 cores | 2 GB | 10 GB | 2 (HA) |
| Worker | 1 core | 1 GB | 5 GB | 1 |
| MySQL | 4 cores | 8 GB | 100 GB SSD | 1 primary + 1 replica |
| Nginx | 0.5 core | 512 MB | 5 GB | 1 |
| Prometheus | 1 core | 2 GB | 50 GB SSD | 1 |
| Zipkin | 1 core | 1 GB | 10 GB | 1 |
| Grafana | 0.5 core | 512 MB | 5 GB | 1 |

### Scaling Guidelines

- **API Server**: Scale horizontally behind Nginx. Target p99 latency < 500ms.
- **MySQL**: Scale reads via read replica. Connection pool max = 20 per API instance.
- **Prometheus**: Retention 15 days by default; increase disk for longer retention.
- **Zipkin**: Use Elasticsearch or Cassandra storage for > 1M spans/day.

### Database Sizing Estimate

- **Operating Cases**: ~10K cases/year → ~5 MB
- **Claims**: ~50K claims/year → ~25 MB
- **Interactions**: ~100K interactions/year → ~50 MB
- **Total (1 year)**: ~100 MB data + indexes → ~500 MB with indexes
- **Recommended initial storage**: 100 GB SSD (supports 5+ years of growth)
