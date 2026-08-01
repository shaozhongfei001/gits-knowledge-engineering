#!/usr/bin/env python3
"""Fail-closed repository scan for credentials, private keys and personal absolute paths."""

from __future__ import annotations

import argparse
from pathlib import Path
import re
import subprocess
import sys


SKIP_PARTS = {".git", "node_modules", "target", "dist", "backups", "__pycache__"}
SKIP_FILES = {"secret_scan.py"}
TEXT_SUFFIXES = {"", ".md", ".txt", ".yaml", ".yml", ".json", ".py", ".sh", ".java", ".ts", ".vue", ".xml", ".ttl", ".dmn", ".properties", ".sql"}
CREDENTIAL = re.compile(r"(?i)\b(password|passwd|secret|api[_-]?key|access[_-]?token)\b\s*[:=]\s*[\"']?([^\s\"']+)")
HOME_PATH = re.compile(r"/(?:home|Users)/[A-Za-z0-9._-]+/")
PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----"
PLACEHOLDER_VALUES = {"changeme", "change_me", "placeholder", "example", "not_set", "none", "null"}


def files_for(root: Path):
    if (root / ".git").is_dir():
        result = subprocess.run(["git", "ls-files", "-co", "--exclude-standard"], cwd=root, text=True, capture_output=True, check=False)
        if result.returncode != 0:
            raise ValueError("git ls-files failed")
        candidates = [root / line for line in result.stdout.splitlines() if line]
    else:
        candidates = list(root.rglob("*"))
    for path in candidates:
        if not path.is_file() or any(part in SKIP_PARTS for part in path.relative_to(root).parts) or path.name in SKIP_FILES:
            continue
        if path.suffix.lower() in TEXT_SUFFIXES:
            yield path


def scan_root(root: Path) -> list[dict]:
    findings = []
    for path in files_for(root):
        relative = path.relative_to(root).as_posix()
        if path.name == ".env" or (path.name.startswith(".env.") and path.name != ".env.example"):
            findings.append({"path": relative, "line": 0, "type": "PROHIBITED_ENV_FILE"})
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for number, line in enumerate(text.splitlines(), 1):
            if PRIVATE_KEY in line:
                findings.append({"path": relative, "line": number, "type": "PRIVATE_KEY"})
            if HOME_PATH.search(line):
                findings.append({"path": relative, "line": number, "type": "PERSONAL_ABSOLUTE_PATH"})
            match = CREDENTIAL.search(line)
            if match:
                value = match.group(2).strip().lower()
                if not (value in PLACEHOLDER_VALUES or value.startswith("${") or value.startswith("<") or value.startswith("{{")):
                    findings.append({"path": relative, "line": number, "type": "POSSIBLE_CREDENTIAL", "key": match.group(1)})
    return findings


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, required=True)
    args = parser.parse_args()
    try:
        findings = scan_root(args.root.resolve())
    except (OSError, ValueError) as exc:
        print(f"secret-scan: FAIL: {exc}", file=sys.stderr)
        return 2
    if findings:
        for item in findings:
            print(item)
        print(f"secret-scan: FAIL: {len(findings)} finding(s)", file=sys.stderr)
        return 2
    print("secret-scan: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
