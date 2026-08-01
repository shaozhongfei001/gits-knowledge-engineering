#!/usr/bin/env python3
"""Apply allowed development-state transitions; QA states are deliberately excluded."""

from __future__ import annotations

import argparse
import datetime as dt
import json
from pathlib import Path
import subprocess
import sys

from baton import transfer


ROOT = Path(__file__).resolve().parents[1]
TRANSITIONS = {
    "planned": {"in_progress", "blocked"},
    "in_progress": {"ready_for_independent_qa", "blocked"},
    "blocked": {"in_progress"},
    "ready_for_independent_qa": {"in_progress", "blocked"},
}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loop", required=True)
    parser.add_argument("--state", choices=("in_progress", "ready_for_independent_qa", "blocked"), required=True)
    parser.add_argument("--actor", required=True)
    parser.add_argument("--reason")
    args = parser.parse_args()
    path = ROOT / "loops" / args.loop / "STATE.json"
    try:
        state = json.loads(path.read_text(encoding="utf-8"))
        if args.actor != state.get("implementation_actor"):
            raise ValueError("only the implementation actor may change development state")
        if args.state not in TRANSITIONS.get(state.get("status"), set()):
            raise ValueError(f"invalid transition {state.get('status')} -> {args.state}")
        if args.state == "ready_for_independent_qa":
            evidence = json.loads((ROOT / "loops" / args.loop / "EVIDENCE.json").read_text(encoding="utf-8"))
            incomplete = sorted(gate_id for gate_id, row in evidence.get("gates", {}).items() if row.get("status") != "pass")
            if incomplete:
                raise ValueError(f"all implementation gates must pass before QA handoff: {incomplete}")
            result = subprocess.run([sys.executable, str(ROOT / "scripts/loop_guard.py"), "--loop", args.loop, "--evidence-only"], cwd=ROOT, check=False)
            if result.returncode != 0:
                raise ValueError("evidence gate prevents promotion")
        if args.state == "blocked" and not args.reason:
            raise ValueError("blocked state requires --reason")
        if state.get("baseline_commit") == "PENDING_FIRST_COMMIT" and args.state != "blocked":
            raise ValueError("create the first controlled Git commit before starting the loop")
        if args.state == "ready_for_independent_qa":
            transfer(args.loop, args.actor, "independent_qa", args.state, args.actor)
            return 0
        state["status"] = args.state
        state["current_phase"] = "independent_qa" if args.state == "ready_for_independent_qa" else args.state
        state["blocked"] = args.state == "blocked"
        state["blocking_reason"] = args.reason if args.state == "blocked" else None
        state["updated_at"] = dt.datetime.now(dt.timezone.utc).isoformat()
        path.write_text(json.dumps(state, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"set-loop-state: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
