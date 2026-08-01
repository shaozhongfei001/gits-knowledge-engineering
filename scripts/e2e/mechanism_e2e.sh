#!/usr/bin/env bash
# Mechanism-level agent E2E gate (loop P2-knowledge-engineering-build, wave W7).
#
# Exercises the contract chain with mocked externals (no AIOS/CRM/IAM/Oracle),
# emits a run-manifest artifact, and verifies the gate. This advances
# REAL_AGENT_E2E from NOT_STARTED to MECHANISM_PASS only — it is NOT a
# real-interface E2E and NOT a QA pass.
#
# Fail-closed: any non-zero exit or missing manifest => FAIL (exit 2).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

EVIDENCE_DIR="loops/P2-knowledge-engineering-build/evidence"
mkdir -p "$EVIDENCE_DIR"

UTC="$(date -u +%Y%m%dT%H%M%SZ)"
DEFAULT_OUT="apps/api/target/e2e-mechanism-run-manifest.json"
EVIDENCE_OUT="$EVIDENCE_DIR/mechanism_e2e_run_manifest_${UTC}.json"

echo "mechanism-e2e: building and running MechanismE2eIT"

if [ ! -x "./mvnw" ]; then
  echo "mechanism-e2e: FAIL (./mvnw missing or not executable)"
  exit 2
fi

set +e
./mvnw -pl apps/api -am -q --batch-mode --no-transfer-progress test \
  -Dtest=MechanismE2eIT \
  -Dsurefire.failIfNoSpecifiedTests=false
MVN_EXIT=$?
set -e

if [ "$MVN_EXIT" -ne 0 ]; then
  echo "mechanism-e2e: FAIL (maven exit=$MVN_EXIT)"
  exit 2
fi

if [ ! -f "$DEFAULT_OUT" ]; then
  echo "mechanism-e2e: FAIL (run-manifest not found at $DEFAULT_OUT)"
  exit 2
fi

cp "$DEFAULT_OUT" "$EVIDENCE_OUT"

if [ ! -f "$EVIDENCE_OUT" ]; then
  echo "mechanism-e2e: FAIL (could not stage manifest to $EVIDENCE_OUT)"
  exit 2
fi

SHA="$(sha256sum "$EVIDENCE_OUT" | awk '{print $1}')"
echo "mechanism-e2e: PASS"
echo "mechanism-e2e: manifest path => $EVIDENCE_OUT"
echo "mechanism-e2e: manifest sha256 => $SHA"
exit 0
