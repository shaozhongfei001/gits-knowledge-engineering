#!/usr/bin/env python3
"""
enum_consistency_check.py — 三层契约漂移防护网

针对根因"改 Java 枚举却不同步 seed 数据"导致的运行时错误（如 B1 GateType 400）。

策略：精确解析核心门禁/状态枚举（这些枚举的常量会被直接硬编码到 SQL seed 数据），
然后扫描 V*.sql 中所有大写蛇形字面量，若该字面量看起来属于"已受控枚举家族"
（与受控枚举共享语义前缀，如 D01_ 门禁前缀），但不在任何合法值中，则报错。

避免全量误报：仅对受控枚举的"值前缀"匹配的字面量做严格校验，其他大写字符串
（货币、UUID 风格代码、自由代码）不做判断，以免重蹈 secret-scan 3729 误报覆辙。

用法:
    python3 scripts/enum_consistency_check.py [--root .] [--quiet]

退出码: 0=一致, 1=不一致, 2=内部错误
"""
import argparse
import re
import sys
from pathlib import Path

# 受控枚举：常量会被硬编码进 SQL seed 的核心门禁/状态枚举
# (枚举类名, 类所在 java 文件, 该枚举常量值前缀特征)
CONTROLLED_ENUMS = [
    ("GateType", "GateType.java", None),                # D01_/F01_/B01_ 门禁前缀
    ("ReconciliationStatus", "ReconciliationStatus.java", None),
    ("HumanGateStatus", "HumanGate.java", None),
    ("GateDecision", "HumanGate.java", None),
]

# SQL 中合法但非枚举的大写字面量（放行，不报错）
ALLOWED_UPPER = {
    "NULL", "CURRENT_TIMESTAMP", "CURRENT_DATE", "FALSE", "TRUE",
}


def parse_enum(file_text: str, enum_name: str) -> list:
    """解析指定枚举类的全部常量值，支持顶层/嵌套、单行/多行。"""
    # 找到 `enum <name> { ... }` 的完整块（允许嵌套），取第一个右花括号闭合
    # 逐个搜索该枚举名出现的位置
    results = []
    for m in re.finditer(r"\benum\s+" + re.escape(enum_name) + r"\s*\{", file_text):
        start = m.end()
        depth = 1
        i = start
        while i < len(file_text) and depth > 0:
            c = file_text[i]
            if c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
            i += 1
        body = file_text[start:i - 1]
        # 收集顶层常量（第一个分号或非缩进常量之前）
        values = []
        # 去掉方法/字段定义，取常量段（到第一个 '(' 或 ';' 前的纯常量）
        for line in body.split("\n"):
            line = line.strip()
            # 跳过注释
            if line.startswith("//") or line.startswith("*") or line.startswith("/*"):
                continue
            cm = re.match(r"^([A-Z][A-Z0-9_]*)\s*[;,\)]", line)
            if cm:
                values.append(cm.group(1))
            elif re.match(r"^([A-Z][A-Z0-9_]*)\s*$", line):
                values.append(re.match(r"^([A-Z][A-Z0-9_]*)\s*$", line).group(1))
            # 遇到其他内容(方法/字段)则停止收集当前枚举体
            elif values:
                break
        if values:
            results.append(values)
    return results


def collect_controlled_enum_values(root: Path) -> dict:
    """收集受控枚举的合法值集合。key=枚举名, value=set(常量值)。"""
    result = {}
    for enum_name, java_file, _prefix in CONTROLLED_ENUMS:
        # 在整个 modules 下搜索该文件名
        targets = list(root.joinpath("modules").rglob(java_file))
        targets += list(root.joinpath("adapters").rglob(java_file))
        found_values = set()
        for tf in targets:
            if "/target/" in str(tf):
                continue
            text = tf.read_text(encoding="utf-8", errors="ignore")
            for vals in parse_enum(text, enum_name):
                found_values.update(vals)
        if found_values:
            result[enum_name] = found_values
    return result


def scan_seed(root: Path) -> list:
    """扫描 V*.sql，返回 (文件, 字面量, 疑似枚举族) 三元组列表。"""
    sql_dir = root / "adapters" / "persistence-relational" / "src" / "main" / "resources" / "db" / "migration" / "h2"
    if not sql_dir.exists():
        return None
    findings = []
    # 提取所有枚举值，用于判断字面量是否属于某受控枚举族
    for sql_file in sorted(sql_dir.glob("V*.sql")):
        text = sql_file.read_text(encoding="utf-8", errors="ignore")
        lines = [ln.split("--")[0] for ln in text.split("\n")]  # 去行注释
        content = "\n".join(lines)
        for lit in re.findall(r"'([^']*)'", content):
            lit = lit.strip()
            if not re.fullmatch(r"[A-Z][A-Z0-9_]{3,}", lit):
                continue
            if lit in ALLOWED_UPPER:
                continue
            # 判断属于哪个受控枚举族（通过前缀特征或全名匹配）
            findings.append((sql_file.name, lit))
    return findings


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--quiet", action="store_true")
    args = ap.parse_args()
    root = Path(args.root).resolve()

    enum_vals = collect_controlled_enum_values(root)
    if not enum_vals:
        if not args.quiet:
            print("enum-consistency: FAIL: 未解析到受控枚举", file=sys.stderr)
        return 2

    # 构建所有受控枚举合法值全集 + 各前缀
    all_legal = set()
    for v in enum_vals.values():
        all_legal.update(v)

    # 为受控枚举构建"族前缀"（如 GateType 用 字母+数字 前缀关联门禁码）
    # GateType 特征：A01_ B01_ C01_ D01_ E01_ F01_ 等 → 族前缀 = "A0_/B0_/C0_/D0_/E0_/F0_"
    family_prefixes = set()
    for en, vals in enum_vals.items():
        if en == "GateType":
            for v in vals:
                # 取 "D01_PRODUCT_RECOMMEND" → "D0" 族（门禁大类）
                family_prefixes.add(v[:2] + "_")
                family_prefixes.add(v[:3] + "_")

    findings = scan_seed(root)
    if findings is None:
        if not args.quiet:
            print("enum-consistency: FAIL: 未找到 h2 migration 目录", file=sys.stderr)
        return 2

    violations = []
    for fname, lit in findings:
        # 门禁族字面量（如 A02_XXX）必须精确匹配 GateType 值
        if any(lit.startswith(p) for p in family_prefixes) and lit not in all_legal:
            violations.append((fname, lit))
        # 受控枚举的完整值名（如 REJECTED, APPROVED 若属于 HumanGateStatus/GateDecision 等）
        elif lit in enum_vals.get("GateDecision", set()) or lit in enum_vals.get("HumanGateStatus", set()) \
                or lit in enum_vals.get("ReconciliationStatus", set()):
            pass  # 合法
        # 其他字面量不判断（避免误报）

    if violations:
        if not args.quiet:
            print("enum-consistency: FAIL")
            for fname, lit in violations[:40]:
                print(f"  {fname}: 非法受控枚举值 '{lit}'")
            if len(violations) > 40:
                print(f"  ... 共 {len(violations)} 处")
        return 1

    seed_count = len(list(root.joinpath("adapters", "persistence-relational", "src", "main", "resources", "db", "migration", "h2").glob("V*.sql")))
    print(f"enum-consistency: PASS — {len(enum_vals)} 族受控枚举, {seed_count} 个 seed 文件")
    return 0


if __name__ == "__main__":
    sys.exit(main())
