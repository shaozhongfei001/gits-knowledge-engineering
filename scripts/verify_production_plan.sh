#!/usr/bin/env bash
# Verify that P21 production-readiness plan artifacts exist and are complete:
#   - cutover/rollback plan
#   - real-platform Port contract candidate plan
#   - production gate documentation (preparation only, not execution)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CUTOVER="${ROOT}/docs/production/P21-CUTOVER-PLAN.md"
PORTS="${ROOT}/docs/production/P21-REAL-PLATFORM-PORTS.md"

fail=0

echo "production-plan: verifying P21 production-readiness artifacts"

# 1. Cutover plan must exist and contain key sections
if [[ -f "${CUTOVER}" ]]; then
    for section in "回滚方案" "分阶段" "真实平台接入计划" "前置条件"; do
        if grep -q "${section}" "${CUTOVER}"; then
            echo "production-plan: OK: cutover plan contains section: ${section}"
        else
            echo "production-plan: FAIL: cutover plan missing section: ${section}"
            fail=1
        fi
    done
    if grep -q "NOT_EXECUTED" "${CUTOVER}"; then
        echo "production-plan: OK: cutover plan declares preparation-only (NOT_EXECUTED)"
    else
        echo "production-plan: FAIL: cutover plan must declare FUSION/PRODUCTION cutover as NOT_EXECUTED"
        fail=1
    fi
else
    echo "production-plan: FAIL: cutover plan missing: ${CUTOVER}"
    fail=1
fi

# 2. Real-platform Port contract candidate plan must exist and enumerate ports
if [[ -f "${PORTS}" ]]; then
    for port in "RagEmbeddingPort" "KnowledgeGraphPort" "MetadataCatalogPort"; do
        if grep -q "${port}" "${PORTS}"; then
            echo "production-plan: OK: real-platform plan contains port: ${port}"
        else
            echo "production-plan: FAIL: real-platform plan missing port: ${port}"
            fail=1
        fi
    done
else
    echo "production-plan: FAIL: real-platform port plan missing: ${PORTS}"
    fail=1
fi

if [[ "${fail}" -ne 0 ]]; then
    echo "production-plan: FAIL"
    exit 2
fi
echo "production-plan: PASS"
