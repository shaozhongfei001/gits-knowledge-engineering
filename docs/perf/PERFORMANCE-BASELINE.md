# 性能基线报告

> GITS Knowledge Engineering — P17 G5 性能基线实测

## 1. 测试环境

| 项目 | 值 |
|------|------|
| 应用运行时 | Java 21 + Spring Boot 3.5 |
| 数据库 | PostgreSQL 16 (Docker) |
| 本体存储 | Apache Jena TDB2 |
| 负载工具 | k6 (Grafana) |
| 部署方式 | Docker Compose (本地) |
| CPU | 4 核 |
| 内存 | 8 GB |

## 2. 客户查询 API 基线

### GET /api/customers — 客户列表

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 100 ms | > 200 ms | 待staging实测 |
| P95 延迟 | < 300 ms | > 600 ms | 待staging实测 |
| P99 延迟 | < 500 ms | > 1000 ms | 待staging实测 |
| 吞吐量 (RPS) | > 100 | < 50 | 待staging实测 |
| 错误率 | < 1% | > 5% | 待staging实测 |

### GET /api/customers/{id} — 客户详情

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 50 ms | > 100 ms | 待staging实测 |
| P95 延迟 | < 200 ms | > 400 ms | 待staging实测 |
| P99 延迟 | < 400 ms | > 800 ms | 待staging实测 |
| 吞吐量 (RPS) | > 150 | < 75 | 待staging实测 |
| 错误率 | < 1% | > 5% | 待staging实测 |

**负载配置**: 50 VU, 持续 30s

**性能阈值(P17新增)**: P95客户查询 < 500ms

## 3. 客户经营链路基线

### POST /api/journeys — 开启经营旅程

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 150 ms | > 300 ms | 待staging实测 |
| P95 延迟 | < 400 ms | > 800 ms | 待staging实测 |
| P99 延迟 | < 800 ms | > 1600 ms | 待staging实测 |

### POST /api/journeys/{id}/insights — 生成洞察

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 200 ms | > 400 ms | 待staging实测 |
| P95 延迟 | < 500 ms | > 1000 ms | 待staging实测 |
| P99 延迟 | < 1000 ms | > 2000 ms | 待staging实测 |

### POST /api/journeys/{id}/previsit — 访前准备

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 200 ms | > 400 ms | 待staging实测 |
| P95 延迟 | < 500 ms | > 1000 ms | 待staging实测 |
| P99 延迟 | < 1000 ms | > 2000 ms | 待staging实测 |

### POST /api/journeys/{id}/postvisit — 访后分析

| 指标 | 基线值 | 回归阈值 | staging实测值 |
|------|--------|----------|--------------|
| P50 延迟 | < 200 ms | > 400 ms | 待staging实测 |
| P95 延迟 | < 500 ms | > 1000 ms | 待staging实测 |
| P99 延迟 | < 1000 ms | > 2000 ms | 待staging实测 |

**负载配置**: 20 VU, 持续 60s, 完整闭环调用链

**性能阈值(P17新增)**: P95经营链路 < 2s

## 4. 回归检测规则

- **P95 延迟超过基线 2 倍** → 警告
- **P95 延迟超过基线 3 倍** → 阻断
- **错误率超过 5%** → 阻断
- **吞吐量低于基线 50%** → 警告

## 5. 性能阈值配置 (P17新增)

### k6-customers.js 阈值

```javascript
export const options = {
  stages: [
    { duration: '5s', target: 10 },   // warm-up
    { duration: '20s', target: 50 },   // ramp to 50 VU
    { duration: '5s', target: 0 },     // cool-down
  ],
  thresholds: {
    http_req_duration: ['p(50)<200', 'p(95)<500', 'p(99)<1000'],  // P95 < 500ms
    errors: ['rate<0.05'],
  },
};
```

### k6-journey.js 阈值

```javascript
export const options = {
  stages: [
    { duration: '10s', target: 5 },    // warm-up
    { duration: '40s', target: 20 },    // ramp to 20 VU
    { duration: '10s', target: 0 },     // cool-down
  ],
  thresholds: {
    http_req_duration: ['p(50)<300', 'p(95)<800', 'p(99)<2000'],  // P95 < 2s (全链路)
    errors: ['rate<0.10'],
  },
};
```

### 阈值说明

| 场景 | P95阈值 | 依据 |
|------|---------|------|
| 客户查询API | < 500ms | 单次数据库查询+序列化 |
| 经营链路(全链路) | < 2s | 多步操作(旅程启动+洞察+访前+访后) |

## 6. 执行方式

```bash
# 一键执行全部性能测试
./scripts/perf/run-perf.sh all

# 仅执行客户查询测试
./scripts/perf/run-perf.sh customers

# 仅执行经营链路测试
./scripts/perf/run-perf.sh journey

# 自定义目标
API_BASE_URL=https://staging.example.com API_KEY=prod-key ./scripts/perf/run-perf.sh
```

## 7. 脚本完整性验证

| 脚本 | 状态 | 备注 |
|------|------|------|
| k6-customers.js | 完整 | 包含自定义指标、阈值配置、结果输出 |
| k6-journey.js | 完整 | 包含4步链路测试、自定义指标、阈值配置 |
| run-perf.sh | 完整 | 支持customers/journey/all模式，环境变量配置 |

## 8. 基线更新记录

| 日期 | 版本 | 操作 | 备注 |
|------|------|------|------|
| 2026-08-05 | P16 | 初始建立 | 首次性能基线 |
| 2026-08-05 | P17 | 阈值配置 | 新增P95客户查询<500ms、P95经营链路<2s阈值 |
| 2026-08-05 | P17 | 实测列添加 | 添加staging实测值列(待填入) |
