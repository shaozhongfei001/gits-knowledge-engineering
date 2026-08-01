#!/usr/bin/env python3
"""Create a fully materialized Loop; unresolved placeholders are never accepted."""

from __future__ import annotations

import argparse
import datetime as dt
import json
from pathlib import Path
import re
import shutil
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]
TEMPLATE = ROOT / "loops/_template"
LOOP_PATTERN = re.compile(r"^[A-Z][A-Z0-9]*-[a-z0-9][a-z0-9-]{1,62}$")
ACTOR_PATTERN = re.compile(r"^[a-z][a-z0-9_-]{2,63}$")


def git_head() -> str:
    result = subprocess.run(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True, capture_output=True, check=False)
    value = result.stdout.strip()
    return value if result.returncode == 0 and re.fullmatch(r"[0-9a-f]{40}", value) else "PENDING_FIRST_COMMIT"


def materialize(loop_id: str, holder: str, title: str, profile: str) -> Path:
    if not LOOP_PATTERN.fullmatch(loop_id):
        raise ValueError("loop-id must match ^[A-Z][A-Z0-9]*-[a-z0-9][a-z0-9-]{1,62}$")
    if not ACTOR_PATTERN.fullmatch(holder):
        raise ValueError("holder must be a stable lowercase actor ID")
    destination = ROOT / "loops" / loop_id
    if destination.exists():
        raise ValueError(f"loop already exists: {destination}")
    shutil.copytree(TEMPLATE, destination)
    replacements = {
        "{{LOOP_ID}}": loop_id,
        "{{HOLDER}}": holder,
        "{{TITLE}}": title,
        "{{BASELINE_COMMIT}}": git_head(),
        "{{ISO_TIME}}": dt.datetime.now(dt.timezone.utc).isoformat(),
    }
    for path in destination.rglob("*"):
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for before, after in replacements.items():
            text = text.replace(before, after)
        path.write_text(text, encoding="utf-8")

    if profile == "framework-dry-run":
        loop_path = destination / "LOOP.yaml"
        evidence_path = destination / "EVIDENCE.json"
        loop = json.loads(loop_path.read_text(encoding="utf-8"))
        extra = [
            {"id": "framework_test", "owner_kind": "implementation", "command": "make framework-test", "pass_condition": "Installer safety tests pass."},
            {"id": "tooling_test", "owner_kind": "implementation", "command": "make tooling-test", "pass_condition": "Contract, Loop, security and read-only guard tests pass."},
        ]
        loop["gates"] = extra + loop["gates"]
        loop["title"] = "GITS SDD Framework V0.2 Dry-run"
        loop_path.write_text(json.dumps(loop, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
        for gate in extra:
            evidence["gates"][gate["id"]] = {"status": "pending", "command": gate["command"], "exit_code": None, "actor": None, "actor_role": None, "executed_at": None, "evidence_file": None, "output_sha256": None}
        evidence_path.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    unresolved = []
    for path in destination.rglob("*"):
        if path.is_file() and "{{" in path.read_text(encoding="utf-8"):
            unresolved.append(path.relative_to(ROOT).as_posix())
    if unresolved:
        shutil.rmtree(destination)
        raise ValueError(f"unresolved template tokens: {unresolved}")
    return destination


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loop-id", required=True)
    parser.add_argument("--holder", required=True)
    parser.add_argument("--title", default="Controlled Engineering Batch")
    parser.add_argument("--profile", choices=("default", "framework-dry-run"), default="default")
    args = parser.parse_args()
    try:
        print(materialize(args.loop_id, args.holder, args.title, args.profile))
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"new-loop: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
