#!/usr/bin/env python3
"""Conservative compatibility diff for generated JSON contracts."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys


def load(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def walk_schema(old: object, new: object, location: str, findings: list[dict]) -> None:
    if not isinstance(old, dict) or not isinstance(new, dict):
        return
    if old.get("type") != new.get("type"):
        findings.append({"grade": "BREAKING", "location": location, "change": "type_changed", "old": old.get("type"), "new": new.get("type")})
    old_properties = old.get("properties", {})
    new_properties = new.get("properties", {})
    for name in sorted(set(old_properties) - set(new_properties)):
        findings.append({"grade": "BREAKING", "location": f"{location}/properties/{name}", "change": "property_removed"})
    old_required = set(old.get("required", []))
    new_required = set(new.get("required", []))
    for name in sorted(new_required - old_required):
        findings.append({"grade": "BREAKING", "location": f"{location}/required/{name}", "change": "new_required_property"})
    if isinstance(old.get("enum"), list) and isinstance(new.get("enum"), list):
        removed = sorted(set(old["enum"]) - set(new["enum"]))
        if removed:
            findings.append({"grade": "BREAKING", "location": location, "change": "enum_values_removed", "removed": removed})
    if "const" in old and old.get("const") != new.get("const"):
        findings.append({"grade": "BREAKING", "location": location, "change": "const_changed", "old": old.get("const"), "new": new.get("const")})
    for name in sorted(set(old_properties) & set(new_properties)):
        walk_schema(old_properties[name], new_properties[name], f"{location}/properties/{name}", findings)
    old_defs = old.get("$defs", {})
    new_defs = new.get("$defs", {})
    for name in sorted(set(old_defs) - set(new_defs)):
        findings.append({"grade": "BREAKING", "location": f"{location}/$defs/{name}", "change": "definition_removed"})
    for name in sorted(set(old_defs) & set(new_defs)):
        walk_schema(old_defs[name], new_defs[name], f"{location}/$defs/{name}", findings)


def compare(baseline: Path, current: Path) -> list[dict]:
    old_files = {p.relative_to(baseline).as_posix(): p for p in baseline.rglob("*.json") if p.name != "manifest.json"}
    new_files = {p.relative_to(current).as_posix(): p for p in current.rglob("*.json") if p.name != "manifest.json"}
    findings = [{"grade": "BREAKING", "location": name, "change": "contract_file_removed"} for name in sorted(set(old_files) - set(new_files))]
    for name in sorted(set(old_files) & set(new_files)):
        walk_schema(load(old_files[name]), load(new_files[name]), name, findings)
    return findings


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--current", type=Path, required=True)
    args = parser.parse_args()
    if not args.baseline.is_dir() or not args.current.is_dir():
        print("contract-diff: FAIL: baseline and current directories are required", file=sys.stderr)
        return 2
    findings = compare(args.baseline, args.current)
    print(json.dumps({"breaking": len(findings), "findings": findings}, ensure_ascii=False, indent=2))
    return 5 if findings else 0


if __name__ == "__main__":
    raise SystemExit(main())
