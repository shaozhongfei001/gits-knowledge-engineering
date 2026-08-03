#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════
#  GITS Knowledge Engineering — 端到端业务链演示脚本
#
#  用法:
#    ./scripts/e2e-demo.sh           # H2内存数据库（默认）
#    ./scripts/e2e-demo.sh mysql     # MySQL profile
#
#  端口可通过PORT环境变量覆盖（默认8088，因8080常被docker占用）:
#    PORT=9090 ./scripts/e2e-demo.sh
#
#  前提: Java 21+, Maven 已安装
# ═══════════════════════════════════════════════════════════════
set -euo pipefail

PROFILE="${1:-}"
PORT="${PORT:-8088}"
BASE_URL="http://localhost:${PORT}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

log_step()   { echo -e "\n${CYAN}━━━ $1 ━━━${NC}"; }
log_ok()     { echo -e "  ${GREEN}✓ $1${NC}"; }
log_warn()   { echo -e "  ${YELLOW}⚠ $1${NC}"; }
log_err()    { echo -e "  ${RED}✗ $1${NC}"; }

# ── 1. 启动Spring Boot ──
log_step "启动Spring Boot应用 (端口=${PORT})"

cd "$PROJECT_DIR"

# 检查是否已有进程占用指定端口
if lsof -i :${PORT} >/dev/null 2>&1; then
    log_err "端口${PORT}已被占用，请先关闭占用的进程"
    exit 1
fi

MVN_ARGS="-pl apps/api spring-boot:run -Dspring-boot.run.arguments=--server.port=${PORT}"
if [ "$PROFILE" = "mysql" ]; then
    MVN_ARGS="$MVN_ARGS -Dspring-boot.run.profiles=mysql"
    log_warn "使用MySQL profile — 请确保MySQL已启动且配置正确"
fi

./mvnw $MVN_ARGS > /tmp/gits-demo.log 2>&1 &
APP_PID=$!
log_ok "Spring Boot启动中 (PID=$APP_PID)"

# 清理函数
cleanup() {
    log_step "关闭Spring Boot应用"
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
    log_ok "已关闭"
}
trap cleanup EXIT

# ── 2. 等待health endpoint返回UP ──
log_step "等待应用就绪"
MAX_WAIT=60
WAITED=0
while [ $WAITED -lt $MAX_WAIT ]; do
    STATUS=$(curl -sf "$BASE_URL/actuator/health" 2>/dev/null | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    if [ "$STATUS" = "UP" ]; then
        log_ok "应用已就绪 (等待了${WAITED}秒)"
        break
    fi
    sleep 2
    WAITED=$((WAITED + 2))
    echo -n "."
done

if [ "$STATUS" != "UP" ]; then
    log_err "应用未能在${MAX_WAIT}秒内就绪"
    log_err "日志: tail -50 /tmp/gits-demo.log"
    exit 1
fi

# ── 3. 创建OperatingCase（开户前置条件） ──
log_step "创建运营案例 — POST /api/case"

CASE_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
NOW=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

CASE_BODY=$(cat <<EOF
{
    "caseId": "$CASE_ID",
    "caseType": "CUSTOMER_JOURNEY",
    "status": "OPEN",
    "purpose": "跨境结算客户旅程跟进",
    "validFrom": "$NOW",
    "validTo": null,
    "createdBy": "RM-WANG-LEI"
}
EOF
)

CASE_RESP=$(curl -sf -X POST "$BASE_URL/api/case" \
    -H "Content-Type: application/json" \
    -d "$CASE_BODY")

echo "$CASE_RESP" | python3 -m json.tool 2>/dev/null || echo "$CASE_RESP"

# 验证案例已创建
CASE_CHECK=$(curl -sf "$BASE_URL/api/case/$CASE_ID" 2>/dev/null || echo "")
if [ -z "$CASE_CHECK" ]; then
    log_err "案例创建失败: GET /api/case/$CASE_ID 返回空"
    exit 1
fi
log_ok "运营案例创建成功: caseId=$CASE_ID"

# ── 4. 创建交互记录 ──
log_step "创建交互记录 — POST /api/interaction"

INTERACTION_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')

INTERACTION_BODY=$(cat <<EOF
{
    "interactionId": "$INTERACTION_ID",
    "caseId": "$CASE_ID",
    "journeyId": null,
    "type": "SIGNAL_TRIGGER",
    "direction": "OUTBOUND",
    "channel": "SYSTEM",
    "initiator": {
        "participantId": "AI-SIGNAL-001",
        "role": "AI_AGENT",
        "displayName": "AI信号引擎"
    },
    "participants": [],
    "contentSummary": "跨境结算量增长42%，存在汇率风险敞口",
    "producedClaimIds": [],
    "outcome": "INFORMATION_GATHERED",
    "occurredAt": "$NOW",
    "endedAt": null,
    "sourceHash": "demo-e2e-signal-hash-001"
}
EOF
)

INTERACTION_RESP=$(curl -sf -X POST "$BASE_URL/api/interaction" \
    -H "Content-Type: application/json" \
    -d "$INTERACTION_BODY")

echo "$INTERACTION_RESP" | python3 -m json.tool 2>/dev/null || echo "$INTERACTION_RESP"
log_ok "交互记录创建成功"

# ── 5. 创建Claim（预置主张） ──
log_step "创建预置主张 — POST /api/claim"

CLAIM_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')

CLAIM_BODY=$(cat <<EOF
{
    "claimId": "$CLAIM_ID",
    "caseId": "$CASE_ID",
    "claimType": "CUSTOMER_JOURNEY",
    "status": "CANDIDATE",
    "statement": "客户跨境结算量大幅增长，存在汇率风险敞口",
    "validFrom": "$NOW",
    "validTo": null,
    "recordedAt": "$NOW",
    "supersedesClaimId": null
}
EOF
)

CLAIM_RESP=$(curl -sf -X POST "$BASE_URL/api/claim" \
    -H "Content-Type: application/json" \
    -d "$CLAIM_BODY")

echo "$CLAIM_RESP" | python3 -m json.tool 2>/dev/null || echo "$CLAIM_RESP"
log_ok "主张创建成功"

# ── 6. M17: 开户——创建CustomerJourney ──
log_step "M17: 开户 — POST /api/journey/open"

OPEN_BODY=$(cat <<EOF
{
    "operatingCaseId": "$CASE_ID",
    "customerId": "CUST-XINDA-001",
    "customerName": "鑫达贸易有限公司",
    "signalDescription": "跨境结算量增长42%，存在汇率风险敞口"
}
EOF
)

OPEN_RESP=$(curl -sf -X POST "$BASE_URL/api/journey/open" \
    -H "Content-Type: application/json" \
    -d "$OPEN_BODY")

echo "$OPEN_RESP" | python3 -m json.tool 2>/dev/null || echo "$OPEN_RESP"

JOURNEY_ID=$(echo "$OPEN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['journeyId'])" 2>/dev/null || echo "")

if [ -z "$JOURNEY_ID" ]; then
    log_err "开户失败: 未获取到journeyId"
    cat /tmp/gits-demo.log | tail -30
    exit 1
fi
log_ok "开户成功: journeyId=$JOURNEY_ID"

# ── 7. 查询旅程详情 ──
log_step "查询旅程详情 — GET /api/journey/{journeyId}"

JOURNEY_RESP=$(curl -sf "$BASE_URL/api/journey/$JOURNEY_ID")
echo "$JOURNEY_RESP" | python3 -m json.tool 2>/dev/null || echo "$JOURNEY_RESP"
log_ok "旅程查询成功"

# ── 8. 推进Claim状态 ──
log_step "推进主张状态 — POST /api/claim/{claimId}/status"

STATUS_BODY='{"newStatus": "VERIFIED_FACT"}'

curl -sf -X POST "$BASE_URL/api/claim/$CLAIM_ID/status" \
    -H "Content-Type: application/json" \
    -d "$STATUS_BODY"

# 查询验证状态变更
CLAIM_AFTER=$(curl -sf "$BASE_URL/api/claim/$CLAIM_ID")
echo "$CLAIM_AFTER" | python3 -m json.tool 2>/dev/null || echo "$CLAIM_AFTER"
log_ok "主张状态已推进为VERIFIED_FACT"

# ── 9. 推进旅程阶段 ──
log_step "推进旅程阶段 — POST /api/journey/{journeyId}/advance"

ADVANCE_BODY='{"targetPhase": "INSIGHT_ANALYSIS"}'

ADVANCE_RESP=$(curl -sf -X POST "$BASE_URL/api/journey/$JOURNEY_ID/advance" \
    -H "Content-Type: application/json" \
    -d "$ADVANCE_BODY" || echo "ADVANCE_FAILED")

if [[ "$ADVANCE_RESP" == *"ADVANCE_FAILED"* ]]; then
    log_warn "阶段推进返回非200（当前advance为简化实现，完整流程通过Service调用）"
else
    echo "$ADVANCE_RESP" | python3 -m json.tool 2>/dev/null || echo "$ADVANCE_RESP"
    log_ok "旅程阶段推进成功"
fi

# ── 10. 查询交互列表 ──
log_step "查询案例交互列表 — GET /api/interaction?caseId="

INTERACTIONS_RESP=$(curl -sf "$BASE_URL/api/interaction?caseId=$CASE_ID")
echo "$INTERACTIONS_RESP" | python3 -m json.tool 2>/dev/null || echo "$INTERACTIONS_RESP"
log_ok "交互列表查询成功"

# ── 11. 查询运营案例 ──
log_step "查询运营案例 — GET /api/case/{caseId}"

CASE_FINAL_RESP=$(curl -sf "$BASE_URL/api/case/$CASE_ID")
echo "$CASE_FINAL_RESP" | python3 -m json.tool 2>/dev/null || echo "$CASE_FINAL_RESP"
log_ok "运营案例查询成功"

# ── 完成 ──
log_step "✅ 演示完成！"
echo ""
echo "  可用的API端点:"
echo "    POST   /api/case                   创建运营案例"
echo "    GET    /api/case/{caseId}           查询运营案例"
echo "    POST   /api/journey/open            开户（创建CustomerJourney）"
echo "    GET    /api/journey/{journeyId}      查询旅程详情"
echo "    POST   /api/journey/{journeyId}/advance  推进旅程阶段"
echo "    POST   /api/interaction              创建交互记录"
echo "    GET    /api/interaction/{id}          查询交互"
echo "    GET    /api/interaction?caseId=       按案例查询交互"
echo "    POST   /api/claim                    创建主张"
echo "    GET    /api/claim/{id}               查询主张"
echo "    POST   /api/claim/{id}/status        更新主张状态"
echo "    GET    /actuator/health             健康检查"
echo ""
echo "  H2 Console: $BASE_URL/h2-console"
echo "    JDBC URL: jdbc:h2:mem:gitskno"
echo "    Username: sa  Pass: (empty)"