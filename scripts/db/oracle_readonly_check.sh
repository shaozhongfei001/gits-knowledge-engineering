#!/usr/bin/env bash
# Oracle EDwCRM read-only connection check.
# Loop: P1-oracle-readonly. ADR: ADR-0007.
#
# Enforces SET TRANSACTION READ ONLY (fail-closed: any error exits non-zero and
# closes the connection). Reads only metadata (current user), never customer
# row data. Requires GITS_ORACLE_* in the environment (set outside the repo).
set -euo pipefail

: "${GITS_ORACLE_HOST:=oracle-vm}"
: "${GITS_ORACLE_PORT:=1521}"
: "${GITS_ORACLE_SERVICE:=ACRM}"
: "${GITS_ORACLE_USER:=edwcrm}"

if [[ -z "${GITS_ORACLE_PASSWORD:-}" ]]; then
    echo "oracle-readonly-check: FAIL: GITS_ORACLE_PASSWORD is required (set it outside the repository)"
    exit 2
fi

if ! command -v sqlplus >/dev/null 2>&1; then
    echo "oracle-readonly-check: FAIL: sqlplus not found on PATH (install Oracle Instant Client)"
    exit 2
fi

conn="${GITS_ORACLE_USER}/${GITS_ORACLE_PASSWORD}@${GITS_ORACLE_HOST}:${GITS_ORACLE_PORT}/${GITS_ORACLE_SERVICE}"

# /nolog + CONNECT keeps the credential out of process argv. WHENEVER SQLERROR
# makes any failure (including SET TRANSACTION READ ONLY rejection) exit non-zero.
out=$(sqlplus -S /nolog <<SQL 2>&1
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
CONNECT ${conn}
SET TRANSACTION READ ONLY;
SELECT username FROM user_users;
EXIT;
SQL
) || { echo "oracle-readonly-check: FAIL: connection or SET TRANSACTION READ ONLY failed"; printf '%s\n' "${out}" | sed 's/^/  /'; exit 2; }

# Oracle confirms a successful SET TRANSACTION READ ONLY with "Transaction set.".
# WHENEVER SQLERROR already guarantees any failure exits non-zero; this grep is
# a redundant confirmation that the read-only transaction and the read landed.
if ! printf '%s\n' "${out}" | grep -qi "Transaction set"; then
    echo "oracle-readonly-check: FAIL: read-only transaction not confirmed in output"
    printf '%s\n' "${out}" | sed 's/^/  /'
    exit 2
fi
if ! printf '%s\n' "${out}" | grep -qi "${GITS_ORACLE_USER}"; then
    echo "oracle-readonly-check: FAIL: metadata read did not return the connected user"
    printf '%s\n' "${out}" | sed 's/^/  /'
    exit 2
fi
echo "oracle-readonly-check: PASS"
