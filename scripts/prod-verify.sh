#!/usr/bin/env bash
# 生产环境接口连通性验证脚本
# 用法: ./scripts/prod-verify.sh [API_BASE_URL]
# 默认: http://localhost:8080

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0
WARN=0

log_pass() { PASS=$((PASS+1)); echo "  ✅ PASS: $1"; }
log_fail() { FAIL=$((FAIL+1)); echo "  ❌ FAIL: $1"; }
log_warn() { WARN=$((WARN+1)); echo "  ⚠️  WARN: $1"; }

echo "========================================="
echo "GITS 生产环境接口连通性验证"
echo "目标: ${BASE_URL}"
echo "时间: $(date -Iseconds)"
echo "========================================="

# 1. 基础健康检查
echo ""
echo "--- 1. 基础健康检查 ---"
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null) && log_pass "Health endpoint (HTTP ${HTTP_CODE})" || log_fail "Health endpoint unreachable"

HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health/readiness" 2>/dev/null) && log_pass "Readiness probe (HTTP ${HTTP_CODE})" || log_fail "Readiness probe unreachable"

HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health/liveness" 2>/dev/null) && log_pass "Liveness probe (HTTP ${HTTP_CODE})" || log_fail "Liveness probe unreachable"

# 2. MySQL连接验证
echo ""
echo "--- 2. MySQL连接验证 ---"
HEALTH_JSON=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null || echo '{}')
echo "${HEALTH_JSON}" | grep -q '"db".*"status":"UP"' && log_pass "MySQL连接正常" || log_warn "MySQL状态未确认 (可能需要认证查看详情)"

# 3. Oracle连接验证 (可选)
echo ""
echo "--- 3. Oracle连接验证 (可选) ---"
ORACLE_ENABLED=$(echo "${HEALTH_JSON}" | grep -o '"oracle".*"status":"UP"' || true)
if [ -n "${ORACLE_ENABLED}" ]; then
    log_pass "Oracle连接正常"
else
    log_warn "Oracle未启用或未连接 (生产环境可能不需要)"
fi

# 4. LLM接口验证
echo ""
echo "--- 4. LLM接口验证 ---"
LLM_HEALTH=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"llm".*"status":"UP"' || true)
if [ -n "${LLM_HEALTH}" ]; then
    log_pass "LLM服务连通"
else
    LLM_HEALTH=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"llm".*"status":"DOWN"' || true)
    if [ -n "${LLM_HEALTH}" ]; then
        log_warn "LLM服务DOWN (将自动降级到Mock模式)"
    else
        log_warn "LLM健康状态未确认"
    fi
fi

# 5. CRM接口验证
echo ""
echo "--- 5. CRM接口验证 ---"
CRM_HEALTH=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"crm".*"status":"UP"' || true)
if [ -n "${CRM_HEALTH}" ]; then
    log_pass "CRM服务连通"
else
    CRM_HEALTH=$(curl -sf "${BASE_URL}/actuator/health" 2>/dev/null | grep -o '"crm".*"status":"DOWN"' || true)
    if [ -n "${CRM_HEALTH}" ]; then
        log_warn "CRM服务DOWN (将自动降级到日志模式)"
    else
        log_warn "CRM健康状态未确认"
    fi
fi

# 6. API端点验证
echo ""
echo "--- 6. API端点验证 ---"
API_KEY="${API_KEY:-}"
AUTH_HEADER=""
if [ -n "${API_KEY}" ]; then
    AUTH_HEADER="-H 'X-API-Key: ${API_KEY}'"
fi

for endpoint in \
    "/api/v1/customer-contexts" \
    "/api/v1/engagement-journeys" \
    "/api/v1/knowledge-rules" \
    "/api/v1/kyc-insights" \
    "/api/v1/operating-cases"; do
    HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" ${AUTH_HEADER} "${BASE_URL}${endpoint}" 2>/dev/null || true)
    if [ "${HTTP_CODE}" = "200" ] || [ "${HTTP_CODE}" = "401" ]; then
        log_pass "${endpoint} (HTTP ${HTTP_CODE})"
    elif [ "${HTTP_CODE}" = "403" ]; then
        log_warn "${endpoint} 需要认证 (HTTP 403)"
    else
        log_fail "${endpoint} (HTTP ${HTTP_CODE:-unreachable})"
    fi
done

# 7. Prometheus指标验证
echo ""
echo "--- 7. Prometheus指标验证 ---"
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/prometheus" 2>/dev/null) && log_pass "Prometheus指标端点 (HTTP ${HTTP_CODE})" || log_fail "Prometheus指标端点不可达"

# 8. 安全验证
echo ""
echo "--- 8. 安全验证 ---"
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" -H "Content-Type: application/json" 2>/dev/null)
if [ "${HTTP_CODE}" = "200" ]; then
    log_pass "Health端点无需认证 (符合预期)"
else
    log_warn "Health端点返回HTTP ${HTTP_CODE}"
fi

# Swagger应被禁用
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/swagger-ui.html" 2>/dev/null || true)
if [ "${HTTP_CODE}" = "404" ] || [ "${HTTP_CODE}" = "403" ]; then
    log_pass "Swagger UI已禁用 (HTTP ${HTTP_CODE})"
else
    log_fail "Swagger UI未禁用 (HTTP ${HTTP_CODE})"
fi

# H2控制台应被禁用
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL}/h2-console" 2>/dev/null || true)
if [ "${HTTP_CODE}" = "404" ] || [ "${HTTP_CODE}" = "403" ]; then
    log_pass "H2控制台已禁用 (HTTP ${HTTP_CODE})"
else
    log_fail "H2控制台未禁用 (HTTP ${HTTP_CODE})"
fi

# 汇总
echo ""
echo "========================================="
echo "验证结果汇总"
echo "========================================="
echo "  ✅ 通过: ${PASS}"
echo "  ❌ 失败: ${FAIL}"
echo "  ⚠️  警告: ${WARN}"
echo ""

if [ "${FAIL}" -gt 0 ]; then
    echo "❌ 存在失败项，请检查后重试"
    exit 1
else
    echo "✅ 所有关键项通过，生产环境就绪"
    exit 0
fi
