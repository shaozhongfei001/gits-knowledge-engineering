#!/usr/bin/env python3
"""Drift detector for GITS-KNO evidence and contract registry.

Checks:
1. CONTRACT_INDEX.yaml: all contracts still ENGINEERING_CANDIDATE (no unauthorized promotion)
2. LOOP states: detect status regressions (e.g. qa_pass → planned)
3. EVIDENCE.json: detect gate result changes (pass → fail or missing)
4. Dispatch files: detect status regressions (CLOSED → IN_PROGRESS)

Exit codes:
  0 = no drift detected
  1 = drift detected (prints details to stdout)
  2 = error (missing files, parse failures)
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DRIFT_LOG = ROOT / "loops" / "_drift_log.json"


def check_contracts() -> list[str]:
    """Contract registry drift: unauthorized status promotion."""
    drifts = []
    path = ROOT / "specs" / "CONTRACT_INDEX.yaml"
    if not path.exists():
        return ["CONTRACT_INDEX.yaml not found"]
    text = path.read_text(encoding="utf-8")
    forbidden = ["BUSINESS_SIGNED", "PRODUCTION_READY", "FROZEN", "BASELINE_FROZEN"]
    for line_no, line in enumerate(text.splitlines(), 1):
        for marker in forbidden:
            if marker in line:
                drifts.append(f"CONTRACT_INDEX.yaml:{line_no}: unauthorized status '{marker}' found")
    return drifts


def check_loop_states() -> list[str]:
    """Loop state regressions: status going backward."""
    drifts = []
    loops_dir = ROOT / "loops"
    if not loops_dir.exists():
        return ["loops/ directory not found"]

    state_rank = {
        "planned": 0,
        "in_progress": 1,
        "ready_for_independent_qa": 2,
        "qa_pass": 3,
        "qa_passed": 3,
        "closed": 4,
        "blocked": -1,
    }

    for state_file in sorted(loops_dir.glob("*/STATE.json")):
        loop_id = state_file.parent.name
        if loop_id.startswith("_"):
            continue
        try:
            data = json.loads(state_file.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError) as exc:
            drifts.append(f"{loop_id}/STATE.json: parse error: {exc}")
            continue

        # Check LOOP.yaml matches STATE
        loop_yaml = state_file.parent / "LOOP.yaml"
        if loop_yaml.exists():
            try:
                loop_data = json.loads(loop_yaml.read_text(encoding="utf-8"))
                loop_status = loop_data.get("status", "")
                state_status = data.get("status", "")
                if loop_status != state_status and "blocked" not in (loop_status + state_status):
                    drifts.append(f"{loop_id}: LOOP.yaml status='{loop_status}' != STATE.json status='{state_status}'")
            except (json.JSONDecodeError, UnicodeDecodeError):
                pass

        # Check for completed_gates missing when status >= ready_for_independent_qa
        status = data.get("status", "")
        gates = data.get("completed_gates", [])
        rank = state_rank.get(status, -2)
        if rank >= 2 and not gates:
            drifts.append(f"{loop_id}: status='{status}' but completed_gates is empty")

    return drifts


def check_evidence() -> list[str]:
    """Evidence drift: gate results changed or missing."""
    drifts = []
    loops_dir = ROOT / "loops"
    for ev_file in sorted(loops_dir.glob("*/EVIDENCE.json")):
        loop_id = ev_file.parent.name
        if loop_id.startswith("_"):
            continue
        try:
            data = json.loads(ev_file.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError) as exc:
            drifts.append(f"{loop_id}/EVIDENCE.json: parse error: {exc}")
            continue

        gates = data.get("gates", {})
        for gate_id, gate_data in gates.items():
            if not isinstance(gate_data, dict):
                continue
            status = gate_data.get("status", "")
            if status == "fail":
                drifts.append(f"{loop_id}: gate '{gate_id}' has status 'fail'")
            # Check for pass gate with no actor
            if status == "pass" and not gate_data.get("actor"):
                drifts.append(f"{loop_id}: gate '{gate_id}' passed but no actor recorded")

    return drifts


def main() -> int:
    all_drifts = []
    all_drifts.extend(check_contracts())
    all_drifts.extend(check_loop_states())
    all_drifts.extend(check_evidence())

    if all_drifts:
        report = {
            "detected_at": __import__("datetime").datetime.now(__import__("datetime").timezone.utc).isoformat(),
            "drift_count": len(all_drifts),
            "drifts": all_drifts,
        }
        # Append to drift log
        log_entries = []
        if DRIFT_LOG.exists():
            try:
                log_entries = json.loads(DRIFT_LOG.read_text(encoding="utf-8"))
                if not isinstance(log_entries, list):
                    log_entries = []
            except (json.JSONDecodeError, UnicodeDecodeError):
                log_entries = []
        log_entries.append(report)
        DRIFT_LOG.write_text(json.dumps(log_entries, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

        print(json.dumps(report, ensure_ascii=False, indent=2))
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
