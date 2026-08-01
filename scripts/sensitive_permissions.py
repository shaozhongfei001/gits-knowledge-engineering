#!/usr/bin/env python3
"""Validate restrictive permissions on local bank-sensitive runtime directories."""

from __future__ import annotations

import argparse
from pathlib import Path
import stat
import sys


SENSITIVE = ("source_private", "reports/private", "artifacts/private", "oracle_source", "oracle_snapshots")


def check(root: Path) -> list[str]:
    findings = []
    for relative in SENSITIVE:
        base = root / relative
        if not base.exists():
            continue
        if not base.is_dir():
            findings.append(f"{relative}: expected directory")
            continue
        if stat.S_IMODE(base.stat().st_mode) & 0o077:
            findings.append(f"{relative}: directory must be mode 700 or stricter")
        for path in base.rglob("*"):
            mode = stat.S_IMODE(path.stat().st_mode)
            if path.is_dir() and mode & 0o077:
                findings.append(f"{path.relative_to(root)}: directory must be mode 700 or stricter")
            if path.is_file() and mode & 0o077:
                findings.append(f"{path.relative_to(root)}: file must be mode 600 or stricter")
    return findings


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, required=True)
    args = parser.parse_args()
    findings = check(args.root.resolve())
    if findings:
        print("\n".join(findings), file=sys.stderr)
        return 2
    print("sensitive-permissions: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
