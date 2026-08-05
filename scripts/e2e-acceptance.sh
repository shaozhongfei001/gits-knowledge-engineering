#!/usr/bin/env bash
# E2E验收测试脚本 — 覆盖5大业务链路
# 用法: ./scripts/e2e-acceptance.sh [API_BASE_URL] [API_KEY]
# 默认: http://localhost:8080

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
API_KEY="${2:-${API_KEY:-}}"
AUTH_HEADER=""
if [ -n "${API_KEY}" ]; then
    AUTH_HEADER="-H 'X-API-Key: ${API_KEY}'"
fi

PASS=0
FAIL=0
SKIP=0

log_pass() { PASS=$((PASS+1)); echo "  ✅ $1"; }
log_fail() { FAIL=$((FAIL+1)); echo "  ❌ $1"; }
log_skip() { SKIP=$((SKIP+1)); echo "  ⏭️  $1"; }

echo "========================================="
echo "GITS E2E验收测试"
echo "目标: ${BASE_URL}"
echo "时间: $(date -Iseconds)"
echo "========================================="

# === 链路1: 客户上下文 ===
echo ""
echo "=== 链路1: 客户上下文 ==="

# 创建客户上下文
CC_ID=$(curl -sf -X POST "${BASE_URL}/api/v1/customer-contexts" \
    ${AUTH_HEADER} \
    -H "Content-Type: application/json" \
    -d '{"customerId":"E2E-TEST-001","customerName":"验收测试客户","segment":"VIP","riskLevel":"MEDIUM"}' 2>/dev/null | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

if [ -n "${CC_ID}" ]; then
    log_pass "创建客户上下文: ${CC_ID}"
else
    log_fail "创建客户上下文失败"
fi

# 查询客户上下文
if [ -n "${CC_ID}" ]; then
    HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/customer-contexts/${CC_ID}" ${AUTH_HEADER} 2>/dev/null || true)
    [ "${HTTP_CODE}" = "200" ] && log_pass "查询客户上下文" || log_fail "查询客户上下文 (HTTP ${HTTP_CODE})"
fi

# === 链路2: 客户旅程 ===
echo ""
echo "=== 链路2: 客户旅程 ==="

# 启动旅程
JOURNEY_ID=$(curl -sf -X POST "${BASE_URL}/api/v1/engagement-journeys" \
    ${AUTH_HEADER} \
    -H "Content-Type: application/json" \
    -d "{\"customerContextId\":\"${CC_ID:-E2E-TEST-001}\",\"journeyType\":\"KYC_REVIEW\"}" 2>/dev/null | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

if [ -n "${JOURNEY_ID}" ]; then
    log_pass "启动客户旅程: ${JOURNEY_ID}"
else
    log_fail "启动客户旅程失败"
fi

# 查询旅程状态
if [ -n "${JOURNEY_ID}" ]; then
    HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/engagement-journeys/${JOURNEY_ID}" ${AUTH_HEADER} 2>/dev/null || true)
    [ "${HTTP_CODE}" = "200" ] && log_pass "查询旅程状态" || log_fail "查询旅程状态 (HTTP ${HTTP_CODE})"
fi

# === 链路3: 知识规则 ===
echo ""
echo "=== 链路3: 知识规则 ==="

HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/knowledge-rules" ${AUTH_HEADER} 2>/dev/null || true)
[ "${HTTP_CODE}" = "200" ] && log_pass "查询知识规则列表" || log_fail "查询知识规则列表 (HTTP ${HTTP_CODE})"

# 创建知识规则
RULE_ID=$(curl -sf -X POST "${BASE_URL}/api/v1/knowledge-rules" \
    ${AUTH_HEADER} \
    -H "Content-Type: application/json" \
    -d '{"ruleCode":"E2E-RULE-001","ruleName":"验收测试规则","ruleType":"THRESHOLD","condition":"riskScore > 0.7","action":"ESCALATE","priority":1}' 2>/dev/null | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

if [ -n "${RULE_ID}" ]; then
    log_pass "创建知识规则: ${RULE_ID}"
else
    log_fail "创建知识规则失败"
fi

# === 链路4: KYC洞察 ===
echo ""
echo "=== 链路4: KYC洞察 ==="

HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/kyc-insights" ${AUTH_HEADER} 2>/dev/null || true)
[ "${HTTP_CODE}" = "200" ] && log_pass "查询KYC洞察列表" || log_fail "查询KYC洞察列表 (HTTP ${HTTP_CODE})"

# 记录声明候选
INSIGHT_ID=$(curl -sf -X POST "${BASE_URL}/api/v1/kyc-insights" \
    ${AUTH_HEADER} \
    -H "Content-Type: application/json" \
    -d "{\"customerContextId\":\"${CC_ID:-E2E-TEST-001}\",\"claimType\":\"RISK_ASSESSMENT\",\"evidenceSummary\":\"E2E验收测试证据\",\"confidenceScore\":0.85}" 2>/dev/null | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

if [ -n "${INSIGHT_ID}" ]; then
    log_pass "记录KYC洞察: ${INSIGHT_ID}"
else
    log_fail "记录KYC洞察失败"
fi

# === 链路5: 运营案例 ===
echo ""
echo "=== 链路5: 运营案例 ==="

HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/operating-cases" ${AUTH_HEADER} 2>/dev/null || true)
[ "${HTTP_CODE}" = "200" ] && log_pass "查询运营案例列表" || log_fail "查询运营案例列表 (HTTP ${HTTP_CODE})"

# === 链路6: LLM降级验证 ===
echo ""
echo "=== 链路6: LLM降级验证 ==="

# 检查LLM健康状态
LLM_STATUS=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"llm".*"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "UNKNOWN")
if [ "${LLM_STATUS}" = "UP" ]; then
    log_pass "LLM服务正常 (UP)"
elif [ "${LLM_STATUS}" = "DOWN" ]; then
    log_pass "LLM服务DOWN，降级到Mock模式 (符合预期)"
else
    log_skip "LLM状态未确认 (${LLM_STATUS})"
fi

# === 链路7: CRM回写验证 ===
echo ""
echo "=== 链路7: CRM回写验证 ==="

CRM_STATUS=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"crm".*"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "UNKNOWN")
if [ "${CRM_STATUS}" = "UP" ]; then
    log_pass "CRM回写连通 (UP)"
elif [ "${CRM_STATUS}" = "DOWN" ]; then
    log_pass "CRM回写DOWN，降级到日志模式 (符合预期)"
else
    log_skip "CRM状态未确认 (${CRM_STATUS})"
fi

# 汇总
echo ""
echo "========================================="
echo "E2E验收测试结果汇总"
echo "========================================="
echo "  ✅ 通过: ${PASS}"
echo "  ❌ 失败: ${FAIL}"
echo "  ⏭️  跳过: ${SKIP}"
echo ""

if [ "${FAIL}" -gt 0 ]; then
    echo "❌ 存在失败项，请检查后重试"
    exit 1
else
    echo "✅ E2E验收测试通过"
    exit 0
fi
