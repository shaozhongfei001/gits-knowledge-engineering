#!/usr/bin/env bash
# ============================================================================
# 场景数据版本切换脚本 — V1.0 ↔ V1.1
#
# 用法:
#   ./scripts/switch-scenario-version.sh v1.0   # 切换到V1.0 (classpath内嵌数据)
#   ./scripts/switch-scenario-version.sh v1.1   # 切换到V1.1 (外部文件系统数据)
#   ./scripts/switch-scenario-version.sh status  # 查看当前版本
#
# 机制:
#   - V1.0: 清空SCENARIO_DATA_ROOT环境变量，应用使用classpath内嵌数据
#   - V1.1: 设置SCENARIO_DATA_ROOT指向scenario/seed目录
#   - 也可通过symlink方式: /opt/gits/scenario-data → scenario/seed-v1.1/
#
# 注意:
#   - 切换版本后需重启应用
#   - V1.1数据目录必须包含完整的场景数据文件
#   - 环境变量优先级高于application.yaml配置
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SCENARIO_DATA_DIR="$PROJECT_ROOT/scenario/seed"
ENV_FILE="$PROJECT_ROOT/.env.scenario"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info()  { echo -e "${BLUE}[INFO]${NC} $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

check_v11_data() {
    if [ ! -d "$SCENARIO_DATA_DIR" ]; then
        log_error "V1.1 scenario/seed directory not found: $SCENARIO_DATA_DIR"
        return 1
    fi

    # 检查关键数据文件
    local missing=0
    for f in \
        "02_master_data/customer_master.json" \
        "02_master_data/legal_entities.csv" \
        "03_bank_data/accounts.csv" \
        "03_bank_data/credit_facilities.csv" \
        "04_external_data/external_events.jsonl" \
        "05_knowledge/product_knowledge_cards.yaml" \
        "06_interactions/historical_interactions.jsonl"; do
        if [ ! -f "$SCENARIO_DATA_DIR/$f" ]; then
            log_warn "Missing V1.1 data file: $f"
            missing=$((missing + 1))
        fi
    done

    if [ $missing -gt 0 ]; then
        log_warn "$missing V1.1 data files are missing"
        return 1
    fi
    return 0
}

switch_to_v10() {
    log_info "Switching to V1.0 (classpath embedded data)..."

    # 清空环境变量
    if [ -f "$ENV_FILE" ]; then
        sed -i '/^SCENARIO_DATA_ROOT=/d' "$ENV_FILE"
    fi

    log_ok "Switched to V1.0 mode"
    log_info "SCENARIO_DATA_ROOT will be empty (classpath fallback)"
    log_info "Restart the application to apply changes"
}

switch_to_v11() {
    log_info "Switching to V1.1 (filesystem external data)..."

    if ! check_v11_data; then
        log_error "V1.1 data validation failed. Aborting."
        exit 1
    fi

    # 写入环境变量文件
    mkdir -p "$(dirname "$ENV_FILE")"
    echo "SCENARIO_DATA_ROOT=$SCENARIO_DATA_DIR" > "$ENV_FILE"

    log_ok "Switched to V1.1 mode"
    log_info "SCENARIO_DATA_ROOT=$SCENARIO_DATA_DIR"
    log_info "Restart the application to apply changes"
    log_info "Or set env: export SCENARIO_DATA_ROOT=$SCENARIO_DATA_DIR"
}

show_status() {
    log_info "=== Scenario Data Version Status ==="

    # 检查环境变量
    local current_root=""
    if [ -f "$ENV_FILE" ]; then
        current_root=$(grep '^SCENARIO_DATA_ROOT=' "$ENV_FILE" | cut -d'=' -f2)
    fi
    if [ -z "$current_root" ]; then
        current_root="${SCENARIO_DATA_ROOT:-}"
    fi

    if [ -n "$current_root" ]; then
        log_ok "Current mode: V1.1 (filesystem)"
        log_info "SCENARIO_DATA_ROOT=$current_root"
        if [ -d "$current_root" ]; then
            log_ok "Data directory exists"
            # 统计文件数
            local file_count
            file_count=$(find "$current_root" -type f | wc -l)
            log_info "Total data files: $file_count"
        else
            log_error "Data directory NOT found: $current_root"
        fi
    else
        log_ok "Current mode: V1.0 (classpath fallback)"
        log_info "SCENARIO_DATA_ROOT is not set"
    fi

    # 检查V1.1数据可用性
    echo ""
    log_info "V1.1 data availability:"
    if [ -d "$SCENARIO_DATA_DIR" ]; then
        log_ok "scenario/seed/ directory exists at: $SCENARIO_DATA_DIR"
        check_v11_data && log_ok "All critical V1.1 data files present" || log_warn "Some V1.1 data files missing"
    else
        log_warn "scenario/seed/ directory not found"
    fi
}

# ========== Main ==========

case "${1:-status}" in
    v1.0|V1.0)
        switch_to_v10
        ;;
    v1.1|V1.1)
        switch_to_v11
        ;;
    status)
        show_status
        ;;
    *)
        echo "Usage: $0 {v1.0|v1.1|status}"
        echo ""
        echo "  v1.0    Switch to V1.0 mode (classpath embedded data)"
        echo "  v1.1    Switch to V1.1 mode (filesystem external data)"
        echo "  status  Show current version status"
        exit 1
        ;;
esac
