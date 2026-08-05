import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ── 自定义指标 ──────────────────────────────────────────────
const errorRate = new Rate('errors');
const journeyStartLatency = new Trend('journey_start_latency', true);
const insightLatency = new Trend('insight_latency', true);
const previsitLatency = new Trend('previsit_latency', true);
const postvisitLatency = new Trend('postvisit_latency', true);

// ── 配置 ────────────────────────────────────────────────────
const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const API_KEY = __ENV.API_KEY || 'test-api-key';

export const options = {
  stages: [
    { duration: '10s', target: 5 },    // warm-up
    { duration: '40s', target: 20 },    // ramp to 20 VU
    { duration: '10s', target: 0 },     // cool-down
  ],
  thresholds: {
    http_req_duration: ['p(50)<300', 'p(95)<800', 'p(99)<2000'],
    errors: ['rate<0.10'],
  },
};

export default function () {
  const headers = {
    'X-API-KEY': API_KEY,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  const customerId = __ENV.TEST_CUSTOMER_ID || 'CUST-CORP-0001';
  const rmId = __ENV.TEST_RM_ID || 'RM-ZW-001';

  // ── 步骤1: 开启经营旅程 ──────────────────────────────────
  const startPayload = JSON.stringify({
    customerId: customerId,
    rmId: rmId,
    signalDescription: `k6 load test signal - VU ${__VU} iteration ${__ITER}`,
  });

  const startRes = http.post(`${BASE_URL}/api/journeys`, startPayload, { headers });
  journeyStartLatency.add(startRes.timings.duration);

  const journeyStarted = check(startRes, {
    'journey start status 2xx': (r) => r.status >= 200 && r.status < 300,
  }) || errorRate.add(1);

  let journeyId = null;
  if (journeyStarted && startRes.json) {
    try {
      const body = typeof startRes.json === 'function' ? startRes.json() : startRes.json;
      journeyId = body.journeyId || body.id;
    } catch (e) {
      // response not JSON, continue
    }
  }

  sleep(1);

  // ── 步骤2: 生成洞察 ──────────────────────────────────────
  if (journeyId) {
    const insightPayload = JSON.stringify({
      claimType: 'FINANCING_NEED',
      content: `k6 test insight - VU ${__VU}`,
      confidence: 0.85,
    });

    const insightRes = http.post(
      `${BASE_URL}/api/journeys/${journeyId}/insights`,
      insightPayload,
      { headers }
    );
    insightLatency.add(insightRes.timings.duration);

    check(insightRes, {
      'insight status 2xx': (r) => r.status >= 200 && r.status < 300,
    }) || errorRate.add(1);
  }

  sleep(1);

  // ── 步骤3: 访前准备 ──────────────────────────────────────
  if (journeyId) {
    const previsitPayload = JSON.stringify({
      rmId: rmId,
      summary: `k6 previsit summary - VU ${__VU}`,
    });

    const previsitRes = http.post(
      `${BASE_URL}/api/journeys/${journeyId}/previsit`,
      previsitPayload,
      { headers }
    );
    previsitLatency.add(previsitRes.timings.duration);

    check(previsitRes, {
      'previsit status 2xx': (r) => r.status >= 200 && r.status < 300,
    }) || errorRate.add(1);
  }

  sleep(1);

  // ── 步骤4: 访后分析 ──────────────────────────────────────
  if (journeyId) {
    const postvisitPayload = JSON.stringify({
      outcome: 'CUSTOMER_AGREED',
      followUpAction: 'Schedule product demo',
      rmId: rmId,
    });

    const postvisitRes = http.post(
      `${BASE_URL}/api/journeys/${journeyId}/postvisit`,
      postvisitPayload,
      { headers }
    );
    postvisitLatency.add(postvisitRes.timings.duration);

    check(postvisitRes, {
      'postvisit status 2xx': (r) => r.status >= 200 && r.status < 300,
    }) || errorRate.add(1);
  }

  sleep(1);
}

export function handleSummary(data) {
  return {
    'scripts/perf/results/journey-summary.json': JSON.stringify(data, null, 2),
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
║       Customer Journey Performance Report        ║
╠══════════════════════════════════════════════════╣
║  P50 Latency:  ${p50} ms
║  P95 Latency:  ${p95} ms
║  P99 Latency:  ${p99} ms
║  Throughput:   ${rps} req/s
║  Error Rate:   ${metrics.errors ? (metrics.errors.values.rate * 100).toFixed(2) : 'N/A'}%
║
║  Stage Breakdown:
║    Journey Start:  P95=${metrics.journey_start_latency ? metrics.journey_start_latency.values['p(95)'] : 'N/A'} ms
║    Insight:        P95=${metrics.insight_latency ? metrics.insight_latency.values['p(95)'] : 'N/A'} ms
║    Previsit:       P95=${metrics.previsit_latency ? metrics.previsit_latency.values['p(95)'] : 'N/A'} ms
║    Postvisit:      P95=${metrics.postvisit_latency ? metrics.postvisit_latency.values['p(95)'] : 'N/A'} ms
╚══════════════════════════════════════════════════╝
`;
}
