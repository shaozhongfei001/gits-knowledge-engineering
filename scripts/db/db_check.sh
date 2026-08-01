#!/usr/bin/env bash
# Database validity check for the gits_ke management store.
#
# Connects with the EXISTING gits@% MySQL user, ensures the gits_ke database
# exists, and runs a read/write roundtrip. Requires GITS_DB_PASSWORD in the
# environment (set outside the repository). Fail-closed: no silent fallback,
# no password on the command line.
set -euo pipefail

: "${GITS_KEDB_HOST:=127.0.0.1}"
: "${GITS_KEDB_PORT:=3306}"
: "${GITS_KEDB_NAME:=gits_ke}"
: "${GITS_KEDB_USER:=ontos}"

if [[ -z "${GITS_KEDB_PASSWORD:-}" ]]; then
    echo "db-check: FAIL: GITS_KEDB_PASSWORD is required (set it outside the repository)"
    exit 2
fi

# The mysql CLI treats host "localhost" as a unix-socket connection and looks
# for the socket at a compiled default path that often differs from the actual
# one (e.g. /tmp/mysql.sock vs /var/run/mysqld/mysqld.sock). Spring JDBC, by
# contrast, treats "localhost" as TCP to 127.0.0.1. Normalize the CLI connection
# to 127.0.0.1 so the check matches how the application actually connects.
conn_host="${GITS_KEDB_HOST}"
if [[ "${conn_host}" == "localhost" ]]; then
    conn_host="127.0.0.1"
fi

defaults_file="$(mktemp)"
trap 'rm -f "${defaults_file}"' EXIT
chmod 600 "${defaults_file}"
printf '[client]\nuser=%s\npassword=%s\nhost=%s\nport=%s\n' \
    "${GITS_KEDB_USER}" "${GITS_KEDB_PASSWORD}" "${conn_host}" "${GITS_KEDB_PORT}" > "${defaults_file}"

mysql_args=(--defaults-extra-file="${defaults_file}")

# 1. connectivity as the configured user
if ! mysql "${mysql_args[@]}" -e "SELECT 1" >/dev/null 2>&1; then
    echo "db-check: FAIL: cannot connect as ${GITS_KEDB_USER}@${GITS_KEDB_HOST}:${GITS_KEDB_PORT}"
    exit 2
fi
echo "db-check: OK: connected as ${GITS_KEDB_USER}@${GITS_KEDB_HOST}:${GITS_KEDB_PORT}"

# 2. ensure the management database exists (user must hold CREATE privilege)
if ! mysql "${mysql_args[@]}" -e \
    "CREATE DATABASE IF NOT EXISTS \`${GITS_KEDB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" 2>&1; then
    echo "db-check: FAIL: cannot create database ${GITS_KEDB_NAME} (check CREATE privilege for ${GITS_KEDB_USER}@%)"
    exit 2
fi
echo "db-check: OK: database ${GITS_KEDB_NAME} ready"

# 3. read/write roundtrip on the management database
roundtrip="$(mysql "${mysql_args[@]}" "${GITS_KEDB_NAME}" -e "
    CREATE TABLE IF NOT EXISTS _db_check (id INT PRIMARY KEY, v VARCHAR(32));
    INSERT INTO _db_check VALUES (1,'ok') ON DUPLICATE KEY UPDATE v=VALUES(v);
    SELECT v FROM _db_check WHERE id=1;" 2>&1)"
if ! printf '%s' "${roundtrip}" | grep -q ok; then
    echo "db-check: FAIL: read/write roundtrip on ${GITS_KEDB_NAME} failed"
    printf '%s\n' "${roundtrip}" | sed 's/^/  /'
    exit 2
fi
mysql "${mysql_args[@]}" "${GITS_KEDB_NAME}" -e "DROP TABLE IF EXISTS _db_check" >/dev/null 2>&1 || true

echo "db-check: PASS"
