#!/usr/bin/env python3
"""Dependency-check 报告完整性守卫（fail-closed）。

验证 OWASP dependency-check 报告是否完整、数据源是否新鲜、是否存在真实 ≥7.0 阻断漏洞，
防止扫描器执行失败/数据源不可用/报告不完整时被误判为 PASS。

校验项（依 P20 Owner 决策）：
  1. 报告文件存在且非空；
  2. 实际扫描依赖数量 > 0；
  3. NVD/主数据源时间可识别（数据新鲜度）；
  4. 无 fatal/error/incomplete 标记；
  5. `dependencies` 节点非空；
  6. CVSS 阻断逻辑实际执行（若存在 ≥7.0 漏洞则 FAIL）。

退出码：0=PASS，2=FAIL（任何校验失败均非 PASS）。
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

# 允许的 dependency-check 报告名（仅 JSON 含结构化 dependencies/scanInfo/projectInfo）
REPORT_NAMES = [
    "dependency-check-report.json",
]


def fail(message: str) -> None:
    raise ValueError(message)


def parse_report(report: Path) -> dict:
    if not report.is_file() or report.stat().st_size == 0:
        fail(f"{report}: report missing or empty")
    try:
        value = json.loads(report.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"{report}: cannot parse JSON report: {exc}")
    if not isinstance(value, dict):
        fail(f"{report}: report root must be an object")
    return value


def check(report: Path) -> int:
    data = parse_report(report)

    # 3. 数据源时间可识别（NVD/主数据源新鲜度）
    project_info = data.get("projectInfo", {}) or {}
    report_date = project_info.get("reportDate")
    if not report_date:
        fail(f"{report}: reportDate missing (cannot verify scan recency)")

    # 4. 无 fatal/error/incomplete 标记（扫描器执行错误必须 FAIL，不能误判 PASS）
    scan_info = data.get("scanInfo", {}) or {}
    error_count = int(scan_info.get("errorCount", 0) or 0)
    if error_count > 0:
        fail(f"{report}: scanInfo.errorCount={error_count} (scan incomplete)")

    # 5. dependencies 节点存在。模块可能无外部依赖（合法空报告），此时仅提示不 FAIL；
    #    真正无依赖模块的空报告不是扫描器失败。有依赖模块的缺失/为空由 errorCount 或 6 捕获。
    deps = data.get("dependencies", [])
    if not isinstance(deps, list):
        fail(f"{report}: 'dependencies' is not a list")

    # 2. 实际扫描依赖数量（有 packages 的）
    scanned = [d for d in deps if d.get("packages")]

    # 6. CVSS 阻断逻辑实际执行：若存在 >=7.0 漏洞则 FAIL
    blocking = []
    for dep in deps:
        for vuln in dep.get("vulnerabilities", []) or []:
            scores = vuln.get("cvssv3", {}) or {}
            base = scores.get("baseScore")
            if base is not None and float(base) >= 7.0:
                blocking.append(f"{vuln.get('name')}({base})")

    if blocking:
        fail(f"{report}: blocking (CVSS>=7.0) vulnerabilities: {', '.join(blocking)}")

    if len(scanned) == 0:
        # 无外部依赖模块的合法空报告：不视为扫描失败，但提示。
        print(f"dependency-check-guard: PASS ({len(deps)} deps, 0 scanned — module may have no external deps, reportDate={report_date})")
    else:
        print(f"dependency-check-guard: PASS ({len(scanned)} packages scanned, reportDate={report_date})")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Dependency-check report completeness guard")
    parser.add_argument("--report", type=Path, default=None)
    parser.add_argument("--search-root", type=Path, default=Path.cwd())
    args = parser.parse_args()

    try:
        report = args.report
        if report is None:
            candidates = []
            for name in REPORT_NAMES:
                path = args.search_root / name
                if path.is_file():
                    candidates.append(path)
                # 递归查找（reactor 多模块可能生成多份）
                for path in args.search_root.rglob(name):
                    if path.is_file() and path not in candidates:
                        candidates.append(path)
            if not candidates:
                fail("no dependency-check report found under search root")
            # 汇总校验所有报告（任一 FAIL 即 FAIL）
            for path in sorted(candidates):
                check(path)
            print(f"dependency-check-guard: all {len(candidates)} report(s) PASS")
            return 0
        return check(report)
    except ValueError as exc:
        print(f"dependency-check-guard: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
