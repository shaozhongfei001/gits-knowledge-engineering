#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python3 "${ROOT}/scripts/contract_pipeline.py" check
exec python3 "${ROOT}/scripts/validate_knowledge_architecture.py" --root "${ROOT}"
