#!/usr/bin/env python3
"""P20 两场景 Shadow E2E（stdlib only，fail-closed）。

对 PRE_VISIT_PREPARATION 与 FACT_RECONCILIATION_30M 两个已批准场景，在 SHADOW 模式下：
  1. 从 Route Policy 解析唯一合法 route；
  2. 加载 Activation Contract 并解析资产（Asset Catalog）；
  3. 生成可重放的 shadow ActivationPlan（确定性字段）；
  4. 与黄金计划比对 deterministicFields（routeMode/selectedAssets/semanticQueries/ruleChecks/skills/context）；
  5. 把 shadow 证据写入 Loop 证据目录（只读正式输出，不改变任何正式/生产输出）。

约束：
  - --mode 必须为 shadow；请求 production/fusion 直接失败（fail-closed）。
  - --scenario 必须是且仅是两个 P20 已批准场景；任何第三场景直接失败。
  - 不产生业务副作用、不连接生产系统、不执行写回。
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

APPROVED_SCENARIOS = {"PRE_VISIT_PREPARATION", "FACT_RECONCILIATION_30M"}
ALLOWED_MODES = {"shadow"}
ROUTE_POLICY_ID = "RP-CORP-RM-001"
NOT_IN_P20_REF = "AC-NOT-IN-P20"
PERMISSION_PENDING = {"", "PENDING"}

# deterministicFields 与黄金计划比对（PLAN-001/PLAN-002）
DETERMINISTIC_FIELDS = ["routeMode", "selectedAssets", "semanticQueries", "ruleChecks", "skills", "context"]

# 黄金计划文件（scenario -> goldenRef）
GOLDEN_BY_SCENARIO = {
    "PRE_VISIT_PREPARATION": "specs/knowledge-architecture/examples/AP-PREVISIT-GOLDEN.json",
    "FACT_RECONCILIATION_30M": "specs/knowledge-architecture/examples/AP-FACT-RECON-GOLDEN.json",
}


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
    # 支持 ---{json}---（单行内联）或 ---\n{json}\n---（标准 frontmatter）
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
    """确定性 planHash：仅含黄金可比对的确定性字段。"""
    parts = [route_mode]
    for asset in sorted(selected_assets, key=lambda a: (a.get("sequence", 0), a.get("assetId", ""))):
        parts.append(f"{asset.get('assetId')}@{asset.get('version')}:{asset.get('required')}:{asset.get('sequence')}")
    parts.append(json.dumps(contract.get("semanticQueries", []), sort_keys=True))
    parts.append(json.dumps(contract.get("ruleChecks", []), sort_keys=True))
    parts.append(json.dumps(contract.get("skills", []), sort_keys=True))
    parts.append(str(contract.get("context", {}).get("maxTokens")))
    parts.append(contract.get("context", {}).get("trimPolicy"))
    return hashlib.sha256("|".join(parts).encode("utf-8")).hexdigest()[:16]


class ShadowE2E:
    def __init__(self, root: Path) -> None:
        self.root = root.resolve()
        self.ka = root / "specs" / "knowledge-architecture"
        self.route_policy = read_json(self.ka / "routes" / f"{ROUTE_POLICY_ID}.json")
        self.activations = {c["contractId"]: c for c in self._load_all_activations()}
        self.assets = {a["assetId"]: a for a in self._load_all_assets()}

    def _load_all_activations(self) -> list[dict]:
        result = []
        for path in sorted((self.ka / "activations").glob("*.json")):
            result.append(read_json(path))
        return result

    def _load_all_assets(self) -> list[dict]:
        result = []
        for path in sorted((self.ka / "assets").rglob("*.md")):
            result.append(read_frontmatter(path))
        return result

    def _resolve_route(self, task_type: str) -> dict:
        default_decision = self.route_policy.get("defaultDecision")
        if default_decision != "DENY_UNMAPPED_TASK":
            fail("route policy defaultDecision must be DENY_UNMAPPED_TASK")
        candidates = [r for r in self.route_policy.get("rules", []) if r.get("taskType") == task_type]
        if not candidates:
            fail(f"task {task_type} is unmapped (fail-closed)")
        # 确定性：按 priority 升序取最低优先级（最高优先权）；冲突则拒绝
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
        if ref == NOT_IN_P20_REF:
            fail(f"task {task_type} maps to contract outside P20 scope (DENY_NOT_IN_P20)")
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
            evidence["scenarios"].append({"scenario": scenario, "plan": plan})
        return evidence


def main() -> int:
    parser = argparse.ArgumentParser(description="P20 two-scenario shadow E2E (fail-closed)")
    parser.add_argument("--mode", required=True)
    parser.add_argument("--scenario", action="append", required=True)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--out", type=Path, default=None, help="shadow evidence output dir (default: <root>/loops/P20-wiki-ontology-fusion/evidence/shadow-e2e)")
    args = parser.parse_args()

    try:
        if args.mode not in ALLOWED_MODES:
            fail(f"unsupported mode {args.mode}; P20 allows only shadow (fail-closed)")
        scenarios = list(args.scenario)
        if not scenarios or set(scenarios) != APPROVED_SCENARIOS:
            fail(f"exactly the two approved scenarios are required, got {sorted(scenarios)}")
        if len(scenarios) != len(set(scenarios)):
            fail("duplicate scenario (fail-closed)")

        e2e = ShadowE2E(args.root.resolve())
        evidence = e2e.run(scenarios)

        out_dir = (args.out or args.root / "loops" / "P20-wiki-ontology-fusion" / "evidence" / "shadow-e2e").resolve()
        out_dir.mkdir(parents=True, exist_ok=True)
        out_file = out_dir / "shadow-e2e-evidence.json"
        out_file.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")

        print("shadow-e2e: PASS")
        print(f"shadow-e2e: evidence written to {out_file}")
        print(f"shadow-e2e: scenarios={sorted(s['scenario'] for s in evidence['scenarios'])} formal_output_changed=False")
        return 0
    except ValueError as exc:
        print(f"shadow-e2e: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
