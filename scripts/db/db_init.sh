#!/usr/bin/env bash
# Initialize / migrate the gits_ke management schema via Flyway.
#
# Requires GITS_KEDB_PASSWORD in the environment (set outside the repository).
# Fail-closed: any failure exits non-zero. The Flyway config is written to a
# temporary file (outside the repository) and the password is passed through
# Flyway's own ${env.VAR} substitution, so no literal credential is written
# into this script or the repository.
set -euo pipefail

: "${GITS_KEDB_HOST:=127.0.0.1}"
: "${GITS_KEDB_PORT:=3306}"
: "${GITS_KEDB_NAME:=gits_ke}"
: "${GITS_KEDB_USER:=ontos}"

if [[ -z "${GITS_KEDB_PASSWORD:-}" ]]; then
    echo "db-init: FAIL: GITS_KEDB_PASSWORD is required (set it outside the repository)"
    exit 2
fi

conn_host="${GITS_KEDB_HOST}"
[[ "${conn_host}" == "localhost" ]] && conn_host="127.0.0.1"

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
migrations="${ROOT}/adapters/persistence-relational/src/main/resources/db/migration"

cfg="$(mktemp)"
trap 'rm -f "${cfg}"' EXIT
chmod 600 "${cfg}"
# Single-quoted format: ${env.GITS_KEDB_PASSWORD} is written literally for
# Flyway to substitute at runtime; the other %s values are filled by printf.
printf 'flyway.url=jdbc:mysql://%s:%s/%s\nflyway.user=%s\nflyway.password=${env.GITS_KEDB_PASSWORD}\nflyway.schemas=%s\nflyway.locations=filesystem:%s\n' \
    "${conn_host}" "${GITS_KEDB_PORT}" "${GITS_KEDB_NAME}" \
    "${GITS_KEDB_USER}" "${GITS_KEDB_NAME}" "${migrations}" > "${cfg}"

"${ROOT}/mvnw" -f "${ROOT}/pom.xml" -pl adapters/persistence-relational \
    flyway:migrate -Dflyway.configFiles="${cfg}"
echo "db-init: PASS"
