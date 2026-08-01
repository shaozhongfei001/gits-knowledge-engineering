#!/usr/bin/env python3
"""Execute one controlled gate, archive output, and atomically update evidence."""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile


ROOT = Path(__file__).resolve().parents[1]


def atomic_json(path: Path, value: dict) -> None:
    fd, raw = tempfile.mkstemp(prefix=f".{path.name}.", dir=path.parent)
    tmp = Path(raw)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            json.dump(value, stream, ensure_ascii=False, indent=2)
            stream.write("\n")
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(tmp, path)
    finally:
        tmp.unlink(missing_ok=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loop", required=True)
    parser.add_argument("--gate", required=True)
    parser.add_argument("--actor", required=True)
    parser.add_argument("--actor-role", choices=("implementation", "independent_qa"), required=True)
    args = parser.parse_args()
    loop = ROOT / "loops" / args.loop
    try:
        spec = json.loads((loop / "LOOP.yaml").read_text(encoding="utf-8"))
        evidence_path = loop / "EVIDENCE.json"
        evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
        gate = next((item for item in spec["gates"] if item["id"] == args.gate), None)
        if not gate:
            raise ValueError(f"unknown gate: {args.gate}")
        if gate.get("owner_kind") != args.actor_role:
            raise ValueError("actor role does not own this gate")
        state = json.loads((loop / "STATE.json").read_text(encoding="utf-8"))
        if state.get("status") != "in_progress":
            raise ValueError("gates may execute only while loop status is in_progress")
        if args.actor_role == "implementation" and args.actor != state.get("implementation_actor"):
            raise ValueError("implementation actor must hold the loop baton")
        if args.actor_role == "independent_qa" and args.actor == state.get("implementation_actor"):
            raise ValueError("implementation actor cannot self-sign independent QA")

        iter_log = loop / "memory/waves/W0-ITER.md"
        attempts = iter_log.read_text(encoding="utf-8").count(f"Gate: `{args.gate}`")
        max_attempts = int(spec.get("repair_policy", {}).get("max_attempts_per_gate", 0))
        if max_attempts < 1 or attempts >= max_attempts:
            raise ValueError(f"gate attempt budget exhausted: {attempts}/{max_attempts}")

        now = dt.datetime.now(dt.timezone.utc)
        stamp = now.strftime("%Y%m%dT%H%M%SZ")
        log = loop / "evidence" / f"{args.gate}-{stamp}.log"
        log.parent.mkdir(parents=True, exist_ok=True)
        result = subprocess.run(gate["command"], cwd=ROOT, shell=True, executable="/bin/bash", text=True, capture_output=True, check=False)
        output = f"COMMAND={gate['command']}\nEXIT_CODE={result.returncode}\nSTDOUT\n{result.stdout}\nSTDERR\n{result.stderr}"
        log.write_text(output, encoding="utf-8")
        relative_log = log.relative_to(ROOT).as_posix()
        output_hash = hashlib.sha256(log.read_bytes()).hexdigest()
        evidence["gates"][args.gate] = {
            "status": "pass" if result.returncode == 0 else "fail",
            "command": gate["command"],
            "exit_code": result.returncode,
            "actor": args.actor,
            "actor_role": args.actor_role,
            "executed_at": now.isoformat(),
            "evidence_file": relative_log,
            "output_sha256": output_hash,
        }
        atomic_json(evidence_path, evidence)

        attempts += 1
        with iter_log.open("a", encoding="utf-8") as stream:
            stream.write(f"\n## Attempt {attempts}｜{stamp}\n\n- Gate: `{args.gate}`\n- Command: `{gate['command']}`\n- Exit: `{result.returncode}`\n- Evidence: `{relative_log}`\n- SHA256: `{output_hash}`\n")
        if result.returncode != 0:
            with (loop / "FAILURES.md").open("a", encoding="utf-8") as stream:
                stream.write(f"\n## {stamp}｜{args.gate}\n\n- Command: `{gate['command']}`\n- Exit: `{result.returncode}`\n- Evidence: `{relative_log}`\n- Classification: `PENDING_ROOT_CAUSE`\n- Next: diagnose, record root cause, fix, rerun the original gate.\n")
        print(output)
        return result.returncode
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"record-gate: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
