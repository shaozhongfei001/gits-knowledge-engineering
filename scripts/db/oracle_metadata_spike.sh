#!/usr/bin/env bash
# Oracle EDwCRM read-only METADATA spike.
# Loop: P1-oracle-readonly (wave W6, CTR-MAP-001). ADR: ADR-0007.
#
# METADATA ONLY: queries data-dictionary views (user_tables, user_tab_columns)
# only. No SELECT against any business/data table. No customer row data is read
# or stored. Output is schema locators (table/column names, types, nullability)
# only — per ADR-0007 source_capture: HASH_AND_LOCATOR_BY_DEFAULT.
#
# READ-ONLY: every sqlplus session begins with SET TRANSACTION READ ONLY.
# Fail-closed: any error exits non-zero and closes the connection.
#
# Credentials come from GITS_ORACLE_* in the environment (sourced outside the
# repository, e.g. from ~/.local_database.env). The password is referenced via
# the ${GITS_ORACLE_PASSWORD} shell variable and is never printed or placed on
# argv (sqlplus -S /nolog + CONNECT reads it from stdin).
#
# Output: a CANDIDATE source catalog (table list + column list) to stdout.
# This is a candidate Proposal — NOT_A_CONTRACT, CANDIDATE_ONLY,
# NOT_AUTO_PROMOTED — requires owner approval + contract change to become
# authoritative.
set -euo pipefail

: "${GITS_ORACLE_HOST:=oracle-vm}"
: "${GITS_ORACLE_PORT:=1521}"
: "${GITS_ORACLE_SERVICE:=ACRM}"
: "${GITS_ORACLE_USER:=edwcrm}"

if [[ -z "${GITS_ORACLE_PASSWORD:-}" ]]; then
    echo "oracle-metadata-spike: FAIL: GITS_ORACLE_PASSWORD is required (set it outside the repository)" >&2
    exit 2
fi

if ! command -v sqlplus >/dev/null 2>&1; then
    echo "oracle-metadata-spike: FAIL: sqlplus not found on PATH (install Oracle Instant Client)" >&2
    exit 2
fi

conn="${GITS_ORACLE_USER}/${GITS_ORACLE_PASSWORD}@${GITS_ORACLE_HOST}:${GITS_ORACLE_PORT}/${GITS_ORACLE_SERVICE}"

# /nolog + CONNECT keeps the credential out of process argv. WHENEVER SQLERROR
# makes any failure (including SET TRANSACTION READ ONLY rejection) exit
# non-zero. Only data-dictionary views are touched — never business tables.
out=$(sqlplus -S /nolog <<SQL 2>&1
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
WHENEVER OSERROR EXIT FAILURE ROLLBACK;
CONNECT ${conn}
SET TRANSACTION READ ONLY;
SET PAGESIZE 0
SET FEEDBACK OFF
SET HEADING ON
SET LINESIZE 200
SET TRIMSPOOL ON

PROMPT === SECTION: read-only transaction confirmed ===
PROMPT === SECTION: user_tables ===
SELECT table_name FROM user_tables ORDER BY table_name;
PROMPT === SECTION: user_tab_columns ===
SELECT table_name, column_name, data_type, nullable
  FROM user_tab_columns
 ORDER BY table_name, column_id;
EXIT;
SQL
) || { echo "oracle-metadata-spike: FAIL: connection, SET TRANSACTION READ ONLY, or metadata read failed" >&2; printf '%s\n' "${out}" | sed 's/^/  /' >&2; exit 2; }

# Fail-closed: confirm the read-only transaction was actually established.
# Use a here-string (not a pipe) so pipefail cannot turn grep's early -q exit
# into a SIGPIPE on the producer (which would falsely fail this check on large
# metadata output).
if ! grep -qi "Transaction set" <<< "${out}"; then
    echo "oracle-metadata-spike: FAIL: read-only transaction not confirmed in output" >&2
    printf '%s\n' "${out}" | sed 's/^/  /' >&2
    exit 2
fi

# Print the metadata catalog to stdout for capture into an evidence file.
printf '%s\n' "${out}"
