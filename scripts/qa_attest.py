#!/usr/bin/env python3
"""Record an independent QA decision; rejects implementation self-signature."""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
from pathlib import Path
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loop", required=True)
    parser.add_argument("--actor", required=True)
    parser.add_argument("--session", required=True)
    parser.add_argument("--decision", choices=("pass", "reject"), required=True)
    parser.add_argument("--evidence", type=Path, required=True)
    args = parser.parse_args()
    loop = ROOT / "loops" / args.loop
    try:
        state_path = loop / "STATE.json"
        state = json.loads(state_path.read_text(encoding="utf-8"))
        if state.get("status") != "ready_for_independent_qa":
            raise ValueError("loop is not ready for independent QA")
        if args.actor == state.get("implementation_actor"):
            raise ValueError("implementation actor cannot sign independent QA")
        evidence_file = args.evidence.resolve()
        if not evidence_file.is_file():
            raise ValueError("QA evidence file is missing")
        try:
            relative_evidence = evidence_file.relative_to(ROOT).as_posix()
        except ValueError as exc:
            raise ValueError("QA evidence must be stored inside the repository") from exc
        check = subprocess.run([sys.executable, str(ROOT / "scripts/loop_guard.py"), "--loop", args.loop, "--evidence-only"], cwd=ROOT, check=False)
        if check.returncode != 0:
            raise ValueError("implementation evidence is invalid")
        board_path = loop / "EVIDENCE.json"
        board = json.loads(board_path.read_text(encoding="utf-8"))
        evidence_hash = hashlib.sha256(evidence_file.read_bytes()).hexdigest()
        board["independent_qa"] = {"status": args.decision, "actor": args.actor, "session": args.session, "attested_at": dt.datetime.now(dt.timezone.utc).isoformat(), "evidence_file": relative_evidence, "evidence_sha256": evidence_hash}
        board_path.write_text(json.dumps(board, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        state["qa_actor"] = args.actor
        state["status"] = "qa_pass" if args.decision == "pass" else "blocked"
        state["blocked"] = args.decision == "reject"
        state["blocking_reason"] = None if args.decision == "pass" else "INDEPENDENT_QA_REJECTED"
        state["updated_at"] = dt.datetime.now(dt.timezone.utc).isoformat()
        state_path.write_text(json.dumps(state, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"qa-attest: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
