#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
# G9: 性能基线一键执行脚本
# 用法: ./scripts/perf/run-perf.sh [customers|journey|all]
# ──────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
mkdir -p "${RESULTS_DIR}"

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
API_KEY="${API_KEY:-test-api-key}"
TEST_CUSTOMER_ID="${TEST_CUSTOMER_ID:-CUST-CORP-0001}"
TEST_RM_ID="${TEST_RM_ID:-RM-ZW-001}"

# 检查 k6 是否安装
if ! command -v k6 &>/dev/null; then
  echo "ERROR: k6 is not installed. Install from https://k6.io/docs/get-started/installation/"
  exit 1
fi

echo "╔══════════════════════════════════════════════════╗"
echo "║     GITS Knowledge Engineering - Perf Suite     ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  Target: ${API_BASE_URL}"
echo "║  Time:   $(date -Iseconds)"
echo "╚══════════════════════════════════════════════════╝"
echo ""

SCENARIO="${1:-all}"

run_customers() {
  echo ">>> Running Customer API load test (50 VU, 30s)..."
  k6 run \
    --env API_BASE_URL="${API_BASE_URL}" \
    --env API_KEY="${API_KEY}" \
    --env TEST_CUSTOMER_ID="${TEST_CUSTOMER_ID}" \
    --out json="${RESULTS_DIR}/customers-$(date +%Y%m%d-%H%M%S).json" \
    "${SCRIPT_DIR}/k6-customers.js" || true
  echo ""
}

run_journey() {
  echo ">>> Running Customer Journey load test (20 VU, 60s)..."
  k6 run \
    --env API_BASE_URL="${API_BASE_URL}" \
    --env API_KEY="${API_KEY}" \
    --env TEST_CUSTOMER_ID="${TEST_CUSTOMER_ID}" \
    --env TEST_RM_ID="${TEST_RM_ID}" \
    --out json="${RESULTS_DIR}/journey-$(date +%Y%m%d-%H%M%S).json" \
    "${SCRIPT_DIR}/k6-journey.js" || true
  echo ""
}

case "${SCENARIO}" in
  customers)
    run_customers
    ;;
  journey)
    run_journey
    ;;
  all)
    run_customers
    run_journey
    ;;
  *)
    echo "Usage: $0 [customers|journey|all]"
    exit 1
    ;;
esac

echo "╔══════════════════════════════════════════════════╗"
echo "║  Performance tests completed.                   ║"
echo "║  Results saved to: ${RESULTS_DIR}/              ║"
echo "╚══════════════════════════════════════════════════╝"
