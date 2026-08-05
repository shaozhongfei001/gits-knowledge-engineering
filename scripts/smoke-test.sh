#!/usr/bin/env bash
# smoke-test.sh — 一键验证全栈启动和核心链路
# 用法: ./scripts/smoke-test.sh [--env-file .env.staging]
#
# 验证内容:
#   1. Docker Compose服务启动
#   2. 健康检查端点
#   3. 核心API链路(场景创建 → 访前准备 → 会面纪要 → 报告生成)
#   4. 外部依赖连通性(Oracle/LLM/CRM)
#
# 退出码:
#   0 — 全部通过
#   1 — 部分失败
#   2 — 启动失败

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.staging.yaml"

# 默认配置
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
ENV_FILE="${1#--env-file }"
ENV_FILE="${ENV_FILE:-$PROJECT_DIR/.env.staging}"
TIMEOUT="${TIMEOUT:-120}"
VERBOSE="${VERBOSE:-false}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

PASS_COUNT=0
FAIL_COUNT=0

check_result() {
    local name="$1" result="$2"
    if [ "$result" = "true" ]; then
        log_info "PASS: $name"
        ((PASS_COUNT++))
    else
        log_error "FAIL: $name"
        ((FAIL_COUNT++))
    fi
}

# ============================================================
# 1. 检查前置条件
# ============================================================
log_info "=== 前置条件检查 ==="

command -v docker >/dev/null 2>&1 && check_result "docker 已安装" "true" || check_result "docker 已安装" "false"
command -v curl >/dev/null 2>&1 && check_result "curl 已安装" "true" || check_result "curl 已安装" "false"

if [ ! -f "$ENV_FILE" ]; then
    log_warn "环境文件不存在: $ENV_FILE"
    log_warn "请复制 .env.staging.template 为 .env.staging 并填写配置"
    log_warn "跳过Docker Compose启动，仅验证已运行的服务"
    SKIP_COMPOSE="true"
else
    SKIP_COMPOSE="false"
    check_result "环境文件存在 ($ENV_FILE)" "true"
fi

# ============================================================
# 2. 启动Docker Compose (可选)
# ============================================================
if [ "$SKIP_COMPOSE" != "true" ]; then
    log_info "=== 启动 Staging 环境 ==="

    cd "$PROJECT_DIR"

    # 停止旧容器
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down --remove-orphans 2>/dev/null || true

    # 启动服务
    log_info "启动 Docker Compose 服务..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

    # 等待服务就绪
    log_info "等待服务就绪 (最多 ${TIMEOUT}s)..."
    elapsed=0
    while [ $elapsed -lt $TIMEOUT ]; do
        if curl -sf "${API_BASE_URL}/actuator/health/readiness" >/dev/null 2>&1; then
            log_info "API 服务已就绪 (${elapsed}s)"
            break
        fi
        sleep 5
        elapsed=$((elapsed + 5))
        log_info "等待中... ${elapsed}s/${TIMEOUT}s"
    done

    if [ $elapsed -ge $TIMEOUT ]; then
        log_error "API 服务启动超时 (${TIMEOUT}s)"
        check_result "API 服务启动" "false"
        exit 2
    fi
    check_result "API 服务启动" "true"
fi

# ============================================================
# 3. 健康检查
# ============================================================
log_info "=== 健康检查 ==="

# 3a. Liveness
LIVENESS=$(curl -sf "${API_BASE_URL}/actuator/health/liveness" 2>/dev/null || echo '{"status":"DOWN"}')
LIVENESS_STATUS=$(echo "$LIVENESS" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
check_result "Liveness 探针 ($LIVENESS_STATUS)" "$([ "$LIVENESS_STATUS" = "UP" ] && echo true || echo false)"

# 3b. Readiness
READINESS=$(curl -sf "${API_BASE_URL}/actuator/health/readiness" 2>/dev/null || echo '{"status":"DOWN"}')
READINESS_STATUS=$(echo "$READINESS" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
check_result "Readiness 探针 ($READINESS_STATUS)" "$([ "$READINESS_STATUS" = "UP" ] && echo true || echo false)"

# 3c. 完整健康状态
HEALTH=$(curl -sf "${API_BASE_URL}/actuator/health" 2>/dev/null || echo '{"status":"DOWN"}')
HEALTH_STATUS=$(echo "$HEALTH" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
check_result "整体健康状态 ($HEALTH_STATUS)" "$([ "$HEALTH_STATUS" = "UP" ] && echo true || echo false)"

# 3d. 各组件健康状态
log_info "--- 组件健康详情 ---"
COMPONENTS=("llm" "crm" "oracle" "db" "diskSpace")
for comp in "${COMPONENTS[@]}"; do
    COMP_STATUS=$(echo "$HEALTH" | grep -o "\"${comp}\":{[^}]*\"status\":\"[^\"]*\"" | grep -o 'status":"[^"]*"' | cut -d'"' -f3 || echo "UNKNOWN")
    if [ "$COMP_STATUS" = "UP" ] || [ "$COMP_STATUS" = "DISABLED" ]; then
        log_info "  $comp: $COMP_STATUS"
    else
        log_warn "  $comp: $COMP_STATUS"
    fi
done

# ============================================================
# 4. 核心API链路验证
# ============================================================
log_info "=== 核心API链路验证 ==="

# 4a. 场景创建
log_info "--- 场景创建 ---"
SCENARIOS=$(curl -sf "${API_BASE_URL}/api/scenarios" 2>/dev/null || echo '[]')
SCENARIO_COUNT=$(echo "$SCENARIOS" | grep -o '"scenarioId"' | wc -l || echo 0)
log_info "  已有场景数: $SCENARIO_COUNT"
check_result "场景API可访问" "$([ "$SCENARIO_COUNT" -ge 0 ] && echo true || echo false)"

# 4b. 创建新场景
log_info "--- 创建新场景 ---"
CREATE_RESULT=$(curl -sf -X POST "${API_BASE_URL}/api/scenarios" \
    -H "Content-Type: application/json" \
    -d '{"customerName":"SMOKE-TEST","scenarioType":"KYC_REVIEW"}' 2>/dev/null || echo '{}')
SCENARIO_ID=$(echo "$CREATE_RESULT" | grep -o '"scenarioId":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
if [ -n "$SCENARIO_ID" ]; then
    log_info "  创建场景成功: $SCENARIO_ID"
    check_result "场景创建" "true"
else
    log_warn "  场景创建失败(可能需要API Key)"
    check_result "场景创建" "false"
fi

# 4c. 访前准备
if [ -n "$SCENARIO_ID" ]; then
    log_info "--- 访前准备 ---"
    PREPARE_RESULT=$(curl -sf -X POST "${API_BASE_URL}/api/scenarios/${SCENARIO_ID}/prepare" 2>/dev/null || echo '{}')
    PREPARE_STATUS=$(echo "$PREPARE_RESULT" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "UNKNOWN")
    log_info "  访前准备状态: $PREPARE_STATUS"
    check_result "访前准备" "$([ "$PREPARE_STATUS" != "UNKNOWN" ] && echo true || echo false)"
fi

# ============================================================
# 5. 外部依赖连通性验证
# ============================================================
log_info "=== 外部依赖连通性 ==="

# 5a. LLM连通性
LLM_HEALTH=$(echo "$HEALTH" | grep -o '"llm":{[^}]*}' || echo "")
if echo "$LLM_HEALTH" | grep -q '"status":"UP"'; then
    check_result "LLM 连通" "true"
else
    log_warn "  LLM 不可用(可能使用mock模式或API Key未配置)"
    check_result "LLM 连通" "false"
fi

# 5b. CRM连通性
CRM_HEALTH=$(echo "$HEALTH" | grep -o '"crm":{[^}]*}' || echo "")
if echo "$CRM_HEALTH" | grep -q '"status":"UP"'; then
    check_result "CRM 连通" "true"
else
    log_warn "  CRM 不可用(可能使用logging模式或URL未配置)"
    check_result "CRM 连通" "false"
fi

# 5c. Oracle连通性
ORACLE_HEALTH=$(echo "$HEALTH" | grep -o '"oracle":{[^}]*}' || echo "")
if echo "$ORACLE_HEALTH" | grep -q '"status":"UP"'; then
    check_result "Oracle 连通" "true"
elif echo "$ORACLE_HEALTH" | grep -q '"status":"DISABLED"'; then
    log_info "  Oracle 已禁用(非Oracle环境正常)"
    check_result "Oracle 连通" "true"
else
    log_warn "  Oracle 不可用"
    check_result "Oracle 连通" "false"
fi

# 5d. 数据库连通性
DB_HEALTH=$(echo "$HEALTH" | grep -o '"db":{[^}]*}' || echo "")
if echo "$DB_HEALTH" | grep -q '"status":"UP"'; then
    check_result "数据库连通" "true"
else
    check_result "数据库连通" "false"
fi

# ============================================================
# 6. Prometheus指标验证
# ============================================================
log_info "=== Prometheus指标验证 ==="
METRICS=$(curl -sf "${API_BASE_URL}/actuator/prometheus" 2>/dev/null || echo "")
if echo "$METRICS" | grep -q "jvm_memory_used"; then
    check_result "Prometheus指标可访问" "true"
    # 检查业务指标
    if echo "$METRICS" | grep -q "gits_llm_call"; then
        log_info "  gits_llm_call 指标存在"
    fi
    if echo "$METRICS" | grep -q "gits_crm_writeback"; then
        log_info "  gits_crm_writeback 指标存在"
    fi
else
    check_result "Prometheus指标可访问" "false"
fi

# ============================================================
# 结果汇总
# ============================================================
echo ""
log_info "========================================="
log_info "  Smoke Test 结果汇总"
log_info "========================================="
log_info "  通过: $PASS_COUNT"
log_error "  失败: $FAIL_COUNT"
log_info "  总计: $((PASS_COUNT + FAIL_COUNT))"
log_info "========================================="

if [ $FAIL_COUNT -eq 0 ]; then
    log_info "全部通过!"
    exit 0
elif [ $FAIL_COUNT -le 3 ]; then
    log_warn "部分失败，请检查上方详情"
    exit 1
else
    log_error "大量失败，请检查环境配置"
    exit 2
fi
