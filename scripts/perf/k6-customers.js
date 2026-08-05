import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ── 自定义指标 ──────────────────────────────────────────────
const errorRate = new Rate('errors');
const customerListLatency = new Trend('customer_list_latency', true);
const customerDetailLatency = new Trend('customer_detail_latency', true);

// ── 配置 ────────────────────────────────────────────────────
const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const API_KEY = __ENV.API_KEY || 'test-api-key';

export const options = {
  stages: [
    { duration: '5s', target: 10 },   // warm-up
    { duration: '20s', target: 50 },   // ramp to 50 VU
    { duration: '5s', target: 0 },     // cool-down
  ],
  thresholds: {
    http_req_duration: ['p(50)<200', 'p(95)<500', 'p(99)<1000'],
    errors: ['rate<0.05'],
  },
};

export default function () {
  const headers = {
    'X-API-KEY': API_KEY,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  // ── 场景1: 客户列表查询 ──────────────────────────────────
  const listRes = http.get(`${BASE_URL}/api/customers`, { headers });
  customerListLatency.add(listRes.timings.duration);

  check(listRes, {
    'customer list status 200': (r) => r.status === 200,
    'customer list has content': (r) => r.body && r.body.length > 0,
  }) || errorRate.add(1);

  sleep(0.5);

  // ── 场景2: 客户详情查询 ──────────────────────────────────
  // 使用已知测试客户ID，或从列表中提取
  const customerId = __ENV.TEST_CUSTOMER_ID || 'CUST-CORP-0001';
  const detailRes = http.get(`${BASE_URL}/api/customers/${customerId}`, { headers });
  customerDetailLatency.add(detailRes.timings.duration);

  check(detailRes, {
    'customer detail status 200 or 404': (r) => r.status === 200 || r.status === 404,
  }) || errorRate.add(1);

  sleep(0.5);
}

export function handleSummary(data) {
  return {
    'scripts/perf/results/customers-summary.json': JSON.stringify(data, null, 2),
    stdout: generateTextSummary(data),
  };
}

function generateTextSummary(data) {
  const metrics = data.metrics || {};
  const reqDuration = metrics.http_req_duration || {};
  const p50 = reqDuration.values ? reqDuration.values['p(50)'] : 'N/A';
  const p95 = reqDuration.values ? reqDuration.values['p(95)'] : 'N/A';
  const p99 = reqDuration.values ? reqDuration.values['p(99)'] : 'N/A';
  const rps = metrics.http_reqs ? metrics.http_reqs.values.rate : 'N/A';

  return `
╔══════════════════════════════════════════════════╗
║          Customer API Performance Report         ║
╠══════════════════════════════════════════════════╣
║  P50 Latency:  ${p50} ms
║  P95 Latency:  ${p95} ms
║  P99 Latency:  ${p99} ms
║  Throughput:   ${rps} req/s
║  Error Rate:   ${metrics.errors ? (metrics.errors.values.rate * 100).toFixed(2) : 'N/A'}%
╚══════════════════════════════════════════════════╝
`;
}
