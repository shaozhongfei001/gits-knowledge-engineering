#!/usr/bin/env python3
"""Journaled, lock-protected Baton transfer across all shared-memory control files."""

from __future__ import annotations

import argparse
import datetime as dt
import fcntl
import json
import os
from pathlib import Path
import re
import tempfile
import sys


ROOT = Path(__file__).resolve().parents[1]


def atomic_text(path: Path, text: str) -> None:
    fd, raw = tempfile.mkstemp(prefix=f".{path.name}.", dir=path.parent)
    tmp = Path(raw)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            stream.write(text)
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(tmp, path)
    finally:
        tmp.unlink(missing_ok=True)


def transfer(loop_id: str, from_holder: str, to_holder: str, new_status: str, actor: str) -> None:
    loop = ROOT / "loops" / loop_id
    paths = {
        "state": loop / "STATE.json",
        "board": loop / "memory/ROLE_BOARD.yaml",
        "next": loop / "memory/NEXT_SESSION.md",
        "shared": loop / "SHARED_MEMORY.md",
        "orchestrator": loop / "memory/ORCHESTRATOR.md",
    }
    if any(not path.is_file() for path in paths.values()):
        raise ValueError("baton control set is incomplete")
    lock_path = loop / "memory/.baton.lock"
    lock_path.touch(mode=0o600, exist_ok=True)
    with lock_path.open("r+", encoding="utf-8") as lock:
        fcntl.flock(lock.fileno(), fcntl.LOCK_EX)
        before = {key: path.read_text(encoding="utf-8") for key, path in paths.items()}
        board = json.loads(before["board"])
        if board.get("baton", {}).get("holder") != from_holder:
            raise ValueError("current Baton holder does not match --from-holder")
        state = json.loads(before["state"])
        now = dt.datetime.now(dt.timezone.utc).isoformat()
        board["baton"]["holder"] = to_holder
        board["updated_by"] = actor
        board["updated_at"] = now
        board["board_status"] = new_status.upper()
        state["status"] = new_status
        state["current_phase"] = "independent_qa" if new_status == "ready_for_independent_qa" else new_status
        state["updated_at"] = now
        next_text = re.sub(r"(\| \*\*Updated\*\* \| `)[^`]+(` \|)", rf"\g<1>{now}\g<2>", before["next"])
        next_text = re.sub(r"(\| \*\*holder\*\* \| `)[^`]+(` \|)", rf"\g<1>{to_holder}\g<2>", next_text)
        shared_text = re.sub(r"(\| status \| )[^|]+( \|)", rf"\g<1>{new_status}\g<2>", before["shared"])
        shared_text = re.sub(r"(\| baton_holder \| `)[^`]+(` \|)", rf"\g<1>{to_holder}\g<2>", shared_text)
        shared_text = re.sub(r"(\| updated_at \| `)[^`]+(` \|)", rf"\g<1>{now}\g<2>", shared_text)
        orchestrator = before["orchestrator"] + f"\n## Baton transfer｜{now}\n\n- From: `{from_holder}`\n- To: `{to_holder}`\n- State: `{new_status}`\n- Actor: `{actor}`\n"
        after = {
            "state": json.dumps(state, ensure_ascii=False, indent=2) + "\n",
            "board": json.dumps(board, ensure_ascii=False, indent=2) + "\n",
            "next": next_text,
            "shared": shared_text,
            "orchestrator": orchestrator,
        }
        journal = loop / "memory/.baton-journal.json"
        atomic_text(journal, json.dumps({"state": "PREPARED", "from": from_holder, "to": to_holder, "at": now}, indent=2) + "\n")
        written = []
        try:
            for key in ("state", "board", "next", "shared", "orchestrator"):
                atomic_text(paths[key], after[key])
                written.append(key)
            atomic_text(journal, json.dumps({"state": "COMMITTED", "from": from_holder, "to": to_holder, "at": now}, indent=2) + "\n")
        except Exception:
            for key in reversed(written):
                atomic_text(paths[key], before[key])
            atomic_text(journal, json.dumps({"state": "ROLLED_BACK", "from": from_holder, "to": to_holder, "at": now}, indent=2) + "\n")
            raise


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loop", required=True)
    parser.add_argument("--from-holder", required=True)
    parser.add_argument("--to-holder", required=True)
    parser.add_argument("--state", required=True)
    parser.add_argument("--actor", required=True)
    args = parser.parse_args()
    try:
        transfer(args.loop, args.from_holder, args.to_holder, args.state, args.actor)
        print("baton-transfer: PASS")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"baton-transfer: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
