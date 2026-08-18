#!/usr/bin/env bash
# Verify that the production profile (application-prod.yaml) is fail-closed:
# required credentials must NOT have insecure empty-defaults that would silently degrade
# production startup when env vars are absent; and a prod startup validator exists.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROD_YAML="${ROOT}/apps/api/src/main/resources/application-prod.yaml"
VALIDATOR="${ROOT}/apps/api/src/main/java/com/gien/gits/api/config/ProdConfigValidator.java"

if [[ ! -f "${PROD_YAML}" ]]; then
    echo "prod-profile-fail-closed: FAIL: application-prod.yaml missing"
    exit 2
fi

fail=0

echo "prod-profile-fail-closed: scanning ${PROD_YAML}"

# 1. Auth api-key must NOT have empty-default (${API_KEY:} is a fail-open risk).
if grep -qE 'api-key: \$\{API_KEY:\}' "${PROD_YAML}"; then
    echo "prod-profile-fail-closed: FAIL: gits.security.api-key has empty-default (${API_KEY:}) — fail-open"
    fail=1
elif grep -qE 'api-key: \$\{API_KEY\}' "${PROD_YAML}"; then
    echo "prod-profile-fail-closed: OK: gits.security.api-key uses required env (no default) — fail-closed"
else
    echo "prod-profile-fail-closed: FAIL: gits.security.api-key pattern not found/unexpected"
    fail=1
fi

# 2. A prod startup validator must exist to enforce mode-aware fail-closed for llm/crm.
if [[ -f "${VALIDATOR}" ]] && grep -q 'gits.security.api-key' "${VALIDATOR}" \
   && grep -q 'engagement.llm.api-key' "${VALIDATOR}" \
   && grep -q 'engagement.crm.writeback-url' "${VALIDATOR}"; then
    echo "prod-profile-fail-closed: OK: ProdConfigValidator enforces mode-aware fail-closed"
else
    echo "prod-profile-fail-closed: FAIL: ProdConfigValidator missing or incomplete (must check api-key / llm.api-key / crm.writeback-url)"
    fail=1
fi

# 3. Datasource must not have empty-default password/username.
if grep -qE 'password: \$\{MYSQL_PASSWORD\}' "${PROD_YAML}" && grep -qE 'username: \$\{MYSQL_USER\}' "${PROD_YAML}"; then
    echo "prod-profile-fail-closed: OK: datasource uses required env (no empty default) — fail-closed"
else
    echo "prod-profile-fail-closed: FAIL: datasource username/password not fail-closed"
    fail=1
fi

if [[ "${fail}" -ne 0 ]]; then
    echo "prod-profile-fail-closed: FAIL"
    exit 2
fi
echo "prod-profile-fail-closed: PASS"
