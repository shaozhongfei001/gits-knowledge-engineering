#!/bin/bash
# GITS 知识工程项目 - 全量回归测试执行脚本
# 版本：V1.0
# 日期：2026-08-05
# 用法：./scripts/regression-run.sh [--skip-build] [--skip-contract] [--skip-unit] [--skip-e2e]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
REGRESSION_DIR="${PROJECT_ROOT}/tests/regression"
REPORT_DIR="${PROJECT_ROOT}/tests/regression/reports"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 解析参数
SKIP_BUILD=false
SKIP_CONTRACT=false
SKIP_UNIT=false
SKIP_E2E=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --skip-build) SKIP_BUILD=true; shift ;;
    --skip-contract) SKIP_CONTRACT=true; shift ;;
    --skip-unit) SKIP_UNIT=true; shift ;;
    --skip-e2e) SKIP_E2E=true; shift ;;
    -h|--help)
      echo "用法: $0 [--skip-build] [--skip-contract] [--skip-unit] [--skip-e2e]"
      echo ""
      echo "选项:"
      echo "  --skip-build      跳过Maven构建"
      echo "  --skip-contract   跳过合同合规测试"
      echo "  --skip-unit       跳过单元测试"
      echo "  --skip-e2e        跳过端到端测试"
      exit 0
      ;;
    *) echo "未知参数: $1"; exit 1 ;;
  esac
done

# 初始化报告目录
mkdir -p "${REPORT_DIR}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="${REPORT_DIR}/regression-report-${TIMESTAMP}.txt"

echo "============================================" | tee "${REPORT_FILE}"
echo "GITS-KNO 全量回归测试" | tee -a "${REPORT_FILE}"
echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')" | tee -a "${REPORT_FILE}"
echo "报告文件: ${REPORT_FILE}" | tee -a "${REPORT_FILE}"
echo "============================================" | tee -a "${REPORT_FILE}"

TOTAL_PASSED=0
TOTAL_FAILED=0
TOTAL_SKIPPED=0

# 记录测试结果
record_result() {
  local name=$1
  local result=$2
  if [ "$result" = "PASS" ]; then
    echo -e "${GREEN}[PASS]${NC} ${name}" | tee -a "${REPORT_FILE}"
    TOTAL_PASSED=$((TOTAL_PASSED + 1))
  elif [ "$result" = "SKIP" ]; then
    echo -e "${YELLOW}[SKIP]${NC} ${name}" | tee -a "${REPORT_FILE}"
    TOTAL_SKIPPED=$((TOTAL_SKIPPED + 1))
  else
    echo -e "${RED}[FAIL]${NC} ${name}" | tee -a "${REPORT_FILE}"
    TOTAL_FAILED=$((TOTAL_FAILED + 1))
  fi
}

# ============================================
# 阶段1: 构建验证
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "--- 阶段1: 构建验证 ---" | tee -a "${REPORT_FILE}"

if [ "$SKIP_BUILD" = false ]; then
  echo "执行 Maven 构建..." | tee -a "${REPORT_FILE}"
  cd "${PROJECT_ROOT}"
  if ./mvnw clean verify -Denforcer.skip=true -q 2>>"${REPORT_DIR}/build-err-${TIMESTAMP}.log"; then
    record_result "Maven构建" "PASS"
  else
    record_result "Maven构建" "FAIL"
    echo "构建失败，详细日志: ${REPORT_DIR}/build-err-${TIMESTAMP}.log" | tee -a "${REPORT_FILE}"
    echo -e "${RED}构建失败，终止回归测试${NC}" | tee -a "${REPORT_FILE}"
    exit 1
  fi
else
  record_result "Maven构建" "SKIP"
fi

# ============================================
# 阶段2: 合同合规测试
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "--- 阶段2: 合同合规测试 ---" | tee -a "${REPORT_FILE}"

if [ "$SKIP_CONTRACT" = false ]; then
  echo "执行合同合规自动化校验..." | tee -a "${REPORT_FILE}"
  cd "${PROJECT_ROOT}"
  if python3 tests/regression/contract-compliance-test.py >>"${REPORT_DIR}/contract-${TIMESTAMP}.log" 2>&1; then
    record_result "合同合规测试(14份合同)" "PASS"
  else
    record_result "合同合规测试(14份合同)" "FAIL"
    echo "详细日志: ${REPORT_DIR}/contract-${TIMESTAMP}.log" | tee -a "${REPORT_FILE}"
  fi
else
  record_result "合同合规测试(14份合同)" "SKIP"
fi

# ============================================
# 阶段3: 单元/集成测试
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "--- 阶段3: 单元/集成测试 ---" | tee -a "${REPORT_FILE}"

if [ "$SKIP_UNIT" = false ]; then
  echo "执行 Maven 测试..." | tee -a "${REPORT_FILE}"
  cd "${PROJECT_ROOT}"
  if ./mvnw test -Denforcer.skip=true -q 2>>"${REPORT_DIR}/unit-err-${TIMESTAMP}.log"; then
    record_result "单元/集成测试" "PASS"
  else
    record_result "单元/集成测试" "FAIL"
    echo "详细日志: ${REPORT_DIR}/unit-err-${TIMESTAMP}.log" | tee -a "${REPORT_FILE}"
  fi
else
  record_result "单元/集成测试" "SKIP"
fi

# ============================================
# 阶段4: 端到端测试
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "--- 阶段4: 端到端测试 ---" | tee -a "${REPORT_FILE}"

if [ "$SKIP_E2E" = false ]; then
  echo "执行端到端场景测试..." | tee -a "${REPORT_FILE}"
  cd "${PROJECT_ROOT}"

  # 检查应用是否运行
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    # 健康检查
    if curl -sf http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
      record_result "应用健康检查" "PASS"
    else
      record_result "应用健康检查" "FAIL"
    fi

    # API端点验证
    if curl -sf http://localhost:8080/gits/api/v1/ontology/classes > /dev/null 2>&1; then
      record_result "API端点-本体类查询" "PASS"
    else
      record_result "API端点-本体类查询" "FAIL"
    fi

    # Prometheus端点验证
    if curl -sf http://localhost:8080/actuator/prometheus > /dev/null 2>&1; then
      record_result "可观测性-Prometheus端点" "PASS"
    else
      record_result "可观测性-Prometheus端点" "FAIL"
    fi
  else
    echo "应用未运行，跳过端到端测试" | tee -a "${REPORT_FILE}"
    record_result "端到端测试(应用未运行)" "SKIP"
  fi
else
  record_result "端到端测试" "SKIP"
fi

# ============================================
# 阶段5: 架构合规检查
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "--- 阶段5: 架构合规检查 ---" | tee -a "${REPORT_FILE}"

# 包命名规范检查
if find "${PROJECT_ROOT}/modules" "${PROJECT_ROOT}/adapters" "${PROJECT_ROOT}/apps" \
  -name "*.java" -exec grep -l "package com\.gien\.gits" {} \; 2>/dev/null | head -1 > /dev/null; then
  record_result "包命名规范(com.gien.gits)" "PASS"
else
  record_result "包命名规范(com.gien.gits)" "FAIL"
fi

# 检查是否有遗留的旧包名
if grep -r "package com\.hzb" "${PROJECT_ROOT}/modules" "${PROJECT_ROOT}/adapters" "${PROJECT_ROOT}/apps" \
  --include="*.java" 2>/dev/null | head -1 > /dev/null; then
  record_result "旧包名检查(应无com.hzb)" "FAIL"
else
  record_result "旧包名检查(应无com.hzb)" "PASS"
fi

# 生成产物存在性检查
if [ -f "${PROJECT_ROOT}/generated/openapi/gits-kno-api.normalized.json" ]; then
  record_result "生成产物-OpenAPI" "PASS"
else
  record_result "生成产物-OpenAPI" "FAIL"
fi

if [ -f "${PROJECT_ROOT}/generated/semantic/gits-core.schema.json" ]; then
  record_result "生成产物-语义Schema" "PASS"
else
  record_result "生成产物-语义Schema" "FAIL"
fi

if [ -f "${PROJECT_ROOT}/generated/rules/claim-reconciliation.normalized.dmn" ]; then
  record_result "生成产物-DMN规则" "PASS"
else
  record_result "生成产物-DMN规则" "FAIL"
fi

# ============================================
# 汇总报告
# ============================================
echo "" | tee -a "${REPORT_FILE}"
echo "============================================" | tee -a "${REPORT_FILE}"
echo "回归测试汇总" | tee -a "${REPORT_FILE}"
echo "============================================" | tee -a "${REPORT_FILE}"
echo "通过: ${TOTAL_PASSED}" | tee -a "${REPORT_FILE}"
echo "失败: ${TOTAL_FAILED}" | tee -a "${REPORT_FILE}"
echo "跳过: ${TOTAL_SKIPPED}" | tee -a "${REPORT_FILE}"
echo "总计: $((TOTAL_PASSED + TOTAL_FAILED + TOTAL_SKIPPED))" | tee -a "${REPORT_FILE}"
echo "" | tee -a "${REPORT_FILE}"

# 通过/失败判定
TOTAL_EXECUTED=$((TOTAL_PASSED + TOTAL_FAILED))
if [ ${TOTAL_FAILED} -eq 0 ]; then
  echo -e "${GREEN}回归测试结果: PASS (全部通过)${NC}" | tee -a "${REPORT_FILE}"
  EXIT_CODE=0
else
  echo -e "${RED}回归测试结果: FAIL (${TOTAL_FAILED}项失败)${NC}" | tee -a "${REPORT_FILE}"
  EXIT_CODE=1
fi

echo "结束时间: $(date '+%Y-%m-%d %H:%M:%S')" | tee -a "${REPORT_FILE}"
echo "报告文件: ${REPORT_FILE}" | tee -a "${REPORT_FILE}"

exit ${EXIT_CODE}
