#!/usr/bin/env python3
"""Fail-closed, plan/apply/rollback installer for the GITS SDD payload."""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import shutil
import stat
import sys
import tempfile
import uuid


FRAMEWORK_ID = "GITS-SDD-FRAMEWORK-V0.2"
FRAMEWORK_VERSION = "0.2.0"
STATE_DIR = ".gits-sdd"
MANIFEST_NAME = "install-manifest.json"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def atomic_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, raw_tmp = tempfile.mkstemp(prefix=f".{path.name}.", dir=path.parent)
    tmp = Path(raw_tmp)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            json.dump(value, stream, ensure_ascii=False, indent=2, sort_keys=True)
            stream.write("\n")
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(tmp, path)
    finally:
        tmp.unlink(missing_ok=True)


def safe_relative(path: Path) -> str:
    raw = path.as_posix()
    if path.is_absolute() or ".." in path.parts or raw in {"", "."}:
        raise ValueError(f"unsafe payload path: {raw}")
    return raw


def scan_payload(payload: Path) -> list[dict]:
    if not payload.is_dir():
        raise ValueError(f"payload directory missing: {payload}")
    files: list[dict] = []
    for path in sorted(payload.rglob("*")):
        if path.is_symlink():
            raise ValueError(f"symbolic links are prohibited in payload: {path}")
        if not path.is_file():
            continue
        relative = safe_relative(path.relative_to(payload))
        files.append(
            {
                "path": relative,
                "source": path,
                "source_sha256": sha256(path),
                "mode": stat.S_IMODE(path.stat().st_mode),
            }
        )
    if not files:
        raise ValueError("payload is empty")
    return files


def load_manifest(target: Path) -> dict | None:
    path = target / STATE_DIR / MANIFEST_NAME
    if not path.exists():
        return None
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError(f"invalid install manifest: {path}: {exc}") from exc
    if manifest.get("framework_id") != FRAMEWORK_ID:
        raise ValueError(f"foreign install manifest: {path}")
    return manifest


def build_plan(payload: Path, target: Path) -> tuple[list[dict], dict | None]:
    previous = load_manifest(target)
    installed = {item["path"]: item for item in (previous or {}).get("files", [])}
    plan: list[dict] = []
    for source in scan_payload(payload):
        destination = target / source["path"]
        action = "CREATE"
        current_sha = None
        reason = "destination is absent"
        if destination.exists() and not destination.is_file():
            action, reason = "CONFLICT", "destination exists and is not a regular file"
        elif destination.is_file():
            current_sha = sha256(destination)
            if current_sha == source["source_sha256"]:
                action, reason = "UNCHANGED", "destination already matches payload"
            elif source["path"] in installed and current_sha == installed[source["path"]].get("source_sha256"):
                action, reason = "UPDATE", "destination matches the previously installed version"
            else:
                action, reason = "CONFLICT", "destination has user or untracked changes"
        plan.append({**source, "destination": destination, "current_sha256": current_sha, "action": action, "reason": reason})
    return plan, previous


def printable_plan(plan: list[dict]) -> list[dict]:
    return [{key: item[key] for key in ("path", "action", "reason", "source_sha256", "current_sha256")} for item in plan]


def restore_partial(changed: list[dict]) -> None:
    for item in reversed(changed):
        destination: Path = item["destination"]
        backup: Path | None = item.get("backup")
        if backup and backup.exists():
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(backup, destination)
        elif item.get("created"):
            destination.unlink(missing_ok=True)


def apply(payload: Path, target: Path) -> dict:
    plan, previous = build_plan(payload, target)
    conflicts = [item for item in plan if item["action"] == "CONFLICT"]
    if conflicts:
        print(json.dumps({"status": "CONFLICT", "plan": printable_plan(plan)}, ensure_ascii=False, indent=2))
        raise SystemExit(3)
    if previous and all(item["action"] == "UNCHANGED" for item in plan):
        return {
            **previous,
            "noop": True,
            "files": [
                {
                    "path": item["path"],
                    "source_sha256": item["source_sha256"],
                    "previous_sha256": item["current_sha256"],
                    "action": "UNCHANGED",
                    "mode": oct(item["mode"]),
                }
                for item in plan
            ],
        }

    install_id = dt.datetime.now(dt.timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + uuid.uuid4().hex[:8]
    state = target / STATE_DIR
    backup_dir = state / "backups" / install_id
    changed: list[dict] = []
    os.umask(0o077)
    try:
        if previous:
            atomic_json(backup_dir / "install-manifest.before.json", previous)
        for item in plan:
            if item["action"] == "UNCHANGED":
                continue
            destination: Path = item["destination"]
            destination.parent.mkdir(parents=True, exist_ok=True)
            backup = None
            if destination.exists():
                backup = backup_dir / "files" / item["path"]
                backup.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(destination, backup)
            fd, raw_tmp = tempfile.mkstemp(prefix=f".{destination.name}.", dir=destination.parent)
            tmp = Path(raw_tmp)
            os.close(fd)
            try:
                shutil.copyfile(item["source"], tmp)
                os.chmod(tmp, item["mode"])
                os.replace(tmp, destination)
            finally:
                tmp.unlink(missing_ok=True)
            changed.append({"destination": destination, "backup": backup, "created": backup is None})

        manifest = {
            "framework_id": FRAMEWORK_ID,
            "framework_version": FRAMEWORK_VERSION,
            "install_id": install_id,
            "installed_at": dt.datetime.now(dt.timezone.utc).isoformat(),
            "previous_install_id": (previous or {}).get("install_id"),
            "backup_directory": str(backup_dir.relative_to(target)),
            "files": [
                {
                    "path": item["path"],
                    "source_sha256": item["source_sha256"],
                    "previous_sha256": item["current_sha256"],
                    "action": item["action"],
                    "mode": oct(item["mode"]),
                }
                for item in plan
            ],
        }
        atomic_json(state / MANIFEST_NAME, manifest)
    except Exception:
        restore_partial(changed)
        raise
    return manifest


def rollback(target: Path, force: bool) -> dict:
    manifest = load_manifest(target)
    if not manifest:
        raise ValueError("nothing to rollback")
    backup_dir = target / manifest["backup_directory"]
    conflicts = []
    for item in manifest["files"]:
        if item["action"] == "UNCHANGED":
            continue
        destination = target / item["path"]
        current = sha256(destination) if destination.is_file() else None
        if current != item["source_sha256"]:
            conflicts.append({"path": item["path"], "expected": item["source_sha256"], "current": current})
    if conflicts and not force:
        print(json.dumps({"status": "ROLLBACK_CONFLICT", "conflicts": conflicts}, ensure_ascii=False, indent=2))
        raise SystemExit(4)

    for item in reversed(manifest["files"]):
        if item["action"] == "UNCHANGED":
            continue
        destination = target / item["path"]
        backup = backup_dir / "files" / item["path"]
        if backup.is_file():
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(backup, destination)
        else:
            destination.unlink(missing_ok=True)

    previous_manifest = backup_dir / "install-manifest.before.json"
    current_manifest = target / STATE_DIR / MANIFEST_NAME
    if previous_manifest.is_file():
        shutil.copy2(previous_manifest, current_manifest)
    else:
        current_manifest.unlink(missing_ok=True)
    return {"status": "ROLLED_BACK", "install_id": manifest["install_id"], "force": force}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--payload", type=Path, default=Path(__file__).with_name("payload"))
    parser.add_argument("--target", type=Path, required=True)
    action = parser.add_mutually_exclusive_group(required=True)
    action.add_argument("--plan", action="store_true")
    action.add_argument("--apply", action="store_true")
    action.add_argument("--rollback", action="store_true")
    parser.add_argument("--force", action="store_true", help="allow rollback over post-install changes")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    target = args.target.resolve()
    if not target.is_dir():
        print(f"ERROR: target directory does not exist: {target}", file=sys.stderr)
        return 2
    try:
        if args.plan:
            plan, _ = build_plan(args.payload.resolve(), target)
            print(json.dumps({"status": "PLAN", "plan": printable_plan(plan)}, ensure_ascii=False, indent=2))
            return 3 if any(item["action"] == "CONFLICT" for item in plan) else 0
        if args.apply:
            print(json.dumps({"status": "APPLIED", "manifest": apply(args.payload.resolve(), target)}, ensure_ascii=False, indent=2))
            return 0
        print(json.dumps(rollback(target, args.force), ensure_ascii=False, indent=2))
        return 0
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
