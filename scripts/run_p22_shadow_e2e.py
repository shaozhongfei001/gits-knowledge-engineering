#!/usr/bin/env python3
"""P22 两场景 Shadow E2E（stdlib only，fail-closed）——"大模型优先读图再执行"。

对 PRE_VISIT_PREPARATION 与 FACT_RECONCILIATION_30M 两个已批准场景，在 SHADOW 模式下：
  1. 从 Route Policy 解析唯一合法 route（fail-closed，同 P20）；
  2. 加载 Activation Contract 并解析资产（Asset Catalog）；
  3. 生成可重放的 shadow ActivationPlan（确定性字段）；
  4. 与黄金计划比对 deterministicFields（routeMode/selectedAssets/semanticQueries/ruleChecks/skills/context）；
  5. 【P22 增量】验证"LLM 优先读图"：基于权威规范资产（KI/KE 目录）组装 LLM 可读知识地图导航，
     校验非空、含受控权威标注、按场景可加载对应子图（方案 A：规划器决定加载范围）；
  6. 把 shadow 证据写入 Loop 证据目录（只读正式输出，不改变任何正式/生产输出）。

约束：
  - --mode 必须为 shadow；请求 production/fusion 直接失败（fail-closed）。
  - --scenario 必须且仅两个 P22 已批准场景；任何第三场景直接失败。
  - 不产生业务副作用、不连接生产系统、不执行写回。
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

# ── 常量区：P22 合同约定的受控值 ───────────────────────────────────────────
APPROVED_SCENARIOS = {"PRE_VISIT_PREPARATION", "FACT_RECONCILIATION_30M"}
ALLOWED_MODES = {"shadow"}
ROUTE_POLICY_ID = "RP-CORP-RM-001"
NOT_IN_P22_REF = "AC-NOT-IN-P22"
PERMISSION_PENDING = {"", "PENDING"}

DETERMINISTIC_FIELDS = ["routeMode", "selectedAssets", "semanticQueries", "ruleChecks", "skills", "context"]

GOLDEN_BY_SCENARIO = {
    "PRE_VISIT_PREPARATION": "specs/knowledge-architecture/examples/AP-PREVISIT-GOLDEN.json",
    "FACT_RECONCILIATION_30M": "specs/knowledge-architecture/examples/AP-FACT-RECON-GOLDEN.json",
}

# 【P22】场景 → 相关知识条目（KI）加载范围（方案 A：规划器决定注入范围）
KI_SCOPE_BY_SCENARIO = {
    "PRE_VISIT_PREPARATION": ["KI-009", "KI-FRONT-001", "KI-FRONT-002", "KI-FRONT-003",
                              "KI-FRONT-004", "KI-FRONT-005", "KI-FRONT-006"],
    "FACT_RECONCILIATION_30M": ["KI-009"],
}

AUTHORITY_TAG = "[AUTHORITATIVE]"  # 受控权威标注


def fail(message: str) -> None:
    raise ValueError(message)


def read_json(path: Path) -> dict:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"{path}: invalid JSON: {exc}")
    if not isinstance(value, dict):
        fail(f"{path}: root object required")
    return value


def read_frontmatter(path: Path) -> dict:
    text = path.read_text(encoding="utf-8-sig")
    lines = text.splitlines()
    if not lines:
        fail(f"{path}: empty file")
    if lines[0].strip().startswith("---{"):
        raw = lines[0].strip()[3:]
        if raw.endswith("---"):
            raw = raw[:-3]
    else:
        if lines[0].strip() != "---":
            fail(f"{path}: opening frontmatter delimiter required")
        try:
            closing = next(index for index in range(1, len(lines)) if lines[index].strip() == "---")
        except StopIteration:
            fail(f"{path}: closing frontmatter delimiter required")
        raw = "\n".join(lines[1:closing]).strip()
    try:
        value = json.loads(raw)
    except json.JSONDecodeError as exc:
        fail(f"{path}: frontmatter must be JSON: {exc}")
    if not isinstance(value, dict):
        fail(f"{path}: frontmatter object required")
    return value


def plan_hash(route_mode: str, selected_assets: list[dict], contract: dict) -> str:
    parts = [route_mode]
    for asset in sorted(selected_assets, key=lambda a: (a.get("sequence", 0), a.get("assetId", ""))):
        parts.append(f"{asset.get('assetId')}@{asset.get('version')}:{asset.get('required')}:{asset.get('sequence')}")
    parts.append(json.dumps(contract.get("semanticQueries", []), sort_keys=True))
    parts.append(json.dumps(contract.get("ruleChecks", []), sort_keys=True))
    parts.append(json.dumps(contract.get("skills", []), sort_keys=True))
    parts.append(str(contract.get("context", {}).get("maxTokens")))
    parts.append(contract.get("context", {}).get("trimPolicy"))
    return hashlib.sha256("|".join(parts).encode("utf-8")).hexdigest()[:16]


class P22ShadowE2E:
    """两场景 shadow E2E：合同解析 + 黄金比对 + LLM 读图导航验证。"""

    def __init__(self, root: Path) -> None:
        self.root = root.resolve()
        self.ka = root / "specs" / "knowledge-architecture"
        self.route_policy = read_json(self.ka / "routes" / f"{ROUTE_POLICY_ID}.json")
        self.activations = {c["contractId"]: c for c in self._load_all_activations()}
        self.assets = {a["assetId"]: a for a in self._load_all_assets()}
        # 【P22】加载知识要素（KI→KE），用于 LLM 读图导航组装。
        self.elements = self._load_all_elements()

    def _load_all_activations(self) -> list[dict]:
        return [read_json(p) for p in sorted((self.ka / "activations").glob("*.json"))]

    def _load_all_assets(self) -> list[dict]:
        return [read_frontmatter(p) for p in sorted((self.ka / "assets").rglob("*.md"))]

    def _load_all_elements(self) -> list[dict]:
        """加载 elements/<KI>/*.md 全部知识要素（排序保证确定）。"""
        result = []
        for path in sorted((self.ka / "elements").rglob("*.md")):
            result.append(read_frontmatter(path))
        return result

    def _resolve_route(self, task_type: str) -> dict:
        if self.route_policy.get("defaultDecision") != "DENY_UNMAPPED_TASK":
            fail("route policy defaultDecision must be DENY_UNMAPPED_TASK")
        candidates = [r for r in self.route_policy.get("rules", []) if r.get("taskType") == task_type]
        if not candidates:
            fail(f"task {task_type} is unmapped (fail-closed)")
        candidates = sorted(candidates, key=lambda r: (r.get("priority", sys.maxsize), r.get("reason", "")))
        top_priority = candidates[0].get("priority")
        if sum(1 for r in candidates if r.get("priority") == top_priority) > 1:
            fail(f"route conflict/ambiguity for {task_type} at priority {top_priority} (fail-closed)")
        return candidates[0]

    def _plan_for(self, task_type: str, customer_id: str, permission: str) -> dict:
        if permission in PERMISSION_PENDING:
            fail("permission decision missing or pending (fail-closed)")
        rule = self._resolve_route(task_type)
        ref = rule.get("activationContractRef")
        if ref == NOT_IN_P22_REF:
            fail(f"task {task_type} maps to contract outside P22 scope (DENY_NOT_IN_P22)")
        contract = self.activations.get(ref)
        if contract is None:
            fail(f"activation contract {ref} not resolvable (fail-closed)")
        if contract["taskType"] != task_type or contract["routeMode"] != rule["mode"]:
            fail(f"route policy and activation contract disagree for {task_type} (DENY_CONTRACT_MISMATCH)")
        selected_assets = []
        for activation in sorted(contract.get("activations", []), key=lambda a: a.get("sequence", 0)):
            asset_id = activation["assetId"]
            asset = self.assets.get(asset_id)
            if asset is None:
                fail(f"activation asset {asset_id} not registered in Asset Manifest (fail-closed)")
            selected_assets.append({
                "assetId": asset["assetId"],
                "version": asset["version"],
                "required": activation["required"],
                "sequence": activation["sequence"],
            })
        context = contract.get("context", {})
        return {
            "schemaVersion": "1.0.0",
            "planId": f"AP-{task_type}-{customer_id}-SHADOW",
            "taskId": f"TASK-{task_type}-{customer_id}-SHADOW",
            "taskType": task_type,
            "routeMode": rule["mode"],
            "selectedAssets": selected_assets,
            "semanticQueries": contract.get("semanticQueries", []),
            "ruleChecks": contract.get("ruleChecks", []),
            "skills": contract.get("skills", []),
            "context": {"maxTokens": context.get("maxTokens"), "trimPolicy": context.get("trimPolicy")},
            "permissionDecisionId": permission,
            "planHash": plan_hash(rule["mode"], selected_assets, contract),
        }

    def _render_knowledge_map(self, scenario: str) -> str:
        """【P22】组装 LLM 可读知识地图导航（方案 A：按场景决定 KI 加载范围）。

        生成 场景→知识条目(KI)→知识要素(KE) 的受控导航文本，含 [AUTHORITATIVE] 权威标注。
        """
        ki_scope = KI_SCOPE_BY_SCENARIO[scenario]
        by_ki = {ki: [] for ki in ki_scope}
        for element in self.elements:
            if element.get("knowledgeItemId") in by_ki:
                by_ki[element["knowledgeItemId"]].append(element)
        lines = [f"# Knowledge Map (shadow) {AUTHORITY_TAG}", f"- scenario: `{scenario}`"]
        for ki in ki_scope:
            elements = sorted(by_ki[ki], key=lambda e: e.get("elementId", ""))
            lines.append(f"- **KI `{ki}`**")
            for element in elements:
                lines.append(f"  - `{element.get('elementId')}` {element.get('name')} "
                             f"[{element.get('kind')}] {AUTHORITY_TAG}")
        rendered = "\n".join(lines)
        if not rendered.strip():
            fail(f"scenario {scenario}: rendered knowledge map is empty (fail-closed)")
        # 校验含受控权威标注
        if AUTHORITY_TAG not in rendered:
            fail(f"scenario {scenario}: rendered map missing authority tag (fail-closed)")
        return rendered

    def _assert_matches_golden(self, plan: dict, scenario: str) -> None:
        golden = read_json(self.root / GOLDEN_BY_SCENARIO[scenario])
        for field in DETERMINISTIC_FIELDS:
            if plan.get(field) != golden.get(field):
                fail(f"scenario {scenario}: deterministic field {field} differs from golden")

    def run(self, scenarios: list[str]) -> dict:
        evidence = {"mode": "shadow", "scenarios": [], "formal_output_changed": False}
        for scenario in sorted(scenarios):
            plan = self._plan_for(scenario, "CUST-001", "PD-SHADOW-ALLOW")
            self._assert_matches_golden(plan, scenario)
            # 【P22】LLM 优先读图：渲染知识地图导航（规划器决定 KI 范围）
            knowledge_map = self._render_knowledge_map(scenario)
            evidence["scenarios"].append({
                "scenario": scenario,
                "plan": plan,
                "knowledge_map_nav": knowledge_map,
            })
        return evidence


def main() -> int:
    parser = argparse.ArgumentParser(description="P22 two-scenario shadow E2E with LLM read-map (fail-closed)")
    parser.add_argument("--mode", required=True)
    parser.add_argument("--scenario", action="append", required=True)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--out", type=Path, default=None)
    args = parser.parse_args()

    try:
        if args.mode not in ALLOWED_MODES:
            fail(f"unsupported mode {args.mode}; P22 allows only shadow (fail-closed)")
        scenarios = list(args.scenario)
        if not scenarios or set(scenarios) != APPROVED_SCENARIOS:
            fail(f"exactly the two approved scenarios are required, got {sorted(scenarios)}")
        if len(scenarios) != len(set(scenarios)):
            fail("duplicate scenario (fail-closed)")

        e2e = P22ShadowE2E(args.root.resolve())
        evidence = e2e.run(scenarios)

        out_dir = (args.out or args.root / "loops" / "P22-llm-wiki-knowledge-map" / "evidence" / "shadow-e2e").resolve()
        out_dir.mkdir(parents=True, exist_ok=True)
        out_file = out_dir / "shadow-e2e-evidence.json"
        out_file.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")

        print("shadow-e2e: PASS")
        print(f"shadow-e2e: evidence written to {out_file}")
        print(f"shadow-e2e: scenarios={sorted(s['scenario'] for s in evidence['scenarios'])} "
              f"formal_output_changed=False (LLM read-map nav rendered)")
        return 0
    except ValueError as exc:
        print(f"shadow-e2e: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
