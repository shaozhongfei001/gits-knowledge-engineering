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

# ── 常量区：P20 合同约定的受控值 ────────────────────────────────────────────────
APPROVED_SCENARIOS = {"PRE_VISIT_PREPARATION", "FACT_RECONCILIATION_30M"}  # 仅允许这两个已批准场景
ALLOWED_MODES = {"shadow"}  # 仅允许 shadow 模式
ROUTE_POLICY_ID = "RP-CORP-RM-001"  # 根路由策略 ID
NOT_IN_P20_REF = "AC-NOT-IN-P20"  # 路由规则中标识"不在 P20 范围"的合同引用占位
PERMISSION_PENDING = {"", "PENDING"}  # 权限未决标记（空或 PENDING）

# deterministicFields 与黄金计划比对（PLAN-001/PLAN-002）
DETERMINISTIC_FIELDS = ["routeMode", "selectedAssets", "semanticQueries", "ruleChecks", "skills", "context"]

# 黄金计划文件（scenario -> goldenRef）：用于比对确定性字段
GOLDEN_BY_SCENARIO = {
    "PRE_VISIT_PREPARATION": "specs/knowledge-architecture/examples/AP-PREVISIT-GOLDEN.json",
    "FACT_RECONCILIATION_30M": "specs/knowledge-architecture/examples/AP-FACT-RECON-GOLDEN.json",
}


def fail(message: str) -> None:
    """抛出失败信号（统一走 ValueError，由 main 捕获并输出 FAIL）。"""
    raise ValueError(message)


def read_json(path: Path) -> dict:
    """读取并解析一个 JSON 文件（fail-closed：非对象根一律拒绝）。"""
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"{path}: invalid JSON: {exc}")
    if not isinstance(value, dict):
        fail(f"{path}: root object required")
    return value


def read_frontmatter(path: Path) -> dict:
    """读取 Markdown 前导元数据文件中的 JSON（支持两种布局）。

    - 单行内联：`---{"json"}---`
    - 标准块：`---\\n{"json"}\\n---`
    fail-closed：任何布局无法识别或 JSON 非法均拒绝。
    """
    text = path.read_text(encoding="utf-8-sig")  # 去掉可能的 BOM
    lines = text.splitlines()
    if not lines:
        fail(f"{path}: empty file")

    # ── 布局一：单行内联 `---{"json"}---` ──────────────────────────
    if lines[0].strip().startswith("---{"):
        raw = lines[0].strip()[3:]  # 去掉开头的 `---`
        if raw.endswith("---"):     # 去掉可能尾随的 `---`
            raw = raw[:-3]
    # ── 布局二：标准 frontmatter `---\n{json}\n---` ─────────────────
    else:
        if lines[0].strip() != "---":
            fail(f"{path}: opening frontmatter delimiter required")
        try:
            # 找到第二个 `---` 作为 JSON 结束边界
            closing = next(index for index in range(1, len(lines)) if lines[index].strip() == "---")
        except StopIteration:
            fail(f"{path}: closing frontmatter delimiter required")
        raw = "\n".join(lines[1:closing]).strip()

    # 把提取出的 JSON 子串解析为对象（fail-closed）
    try:
        value = json.loads(raw)
    except json.JSONDecodeError as exc:
        fail(f"{path}: frontmatter must be JSON: {exc}")
    if not isinstance(value, dict):
        fail(f"{path}: frontmatter object required")
    return value


def plan_hash(route_mode: str, selected_assets: list[dict], contract: dict) -> str:
    """计算确定性 planHash。

    仅拼接黄金可比对的确定性字段（routeMode/资产/查询/规则/技能/上下文），
    保证相同输入总能产生相同哈希（可重放）。资产按 (sequence, assetId) 排序保证顺序确定。
    """
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
    """两场景 shadow E2E 执行器：加载合同、解析 route、生成计划、比对黄金。"""

    def __init__(self, root: Path) -> None:
        # 解析仓库根并加载 P20 合同数据（路由策略/激活合同/资产清单），预构建索引。
        self.root = root.resolve()
        self.ka = root / "specs" / "knowledge-architecture"
        self.route_policy = read_json(self.ka / "routes" / f"{ROUTE_POLICY_ID}.json")
        # 以 contractId 为键索引全部激活合同，便于 O(1) 查找。
        self.activations = {c["contractId"]: c for c in self._load_all_activations()}
        # 以 assetId 为键索引全部资产清单。
        self.assets = {a["assetId"]: a for a in self._load_all_assets()}

    def _load_all_activations(self) -> list[dict]:
        """加载 activations/ 目录下全部 JSON 合同（排序保证顺序确定）。"""
        result = []
        for path in sorted((self.ka / "activations").glob("*.json")):
            result.append(read_json(path))
        return result

    def _load_all_assets(self) -> list[dict]:
        """加载 assets/ 下全部 Markdown 前导资产清单（递归、排序保证确定）。"""
        result = []
        for path in sorted((self.ka / "assets").rglob("*.md")):
            result.append(read_frontmatter(path))
        return result

    def _resolve_route(self, task_type: str) -> dict:
        """从路由策略解析任务对应的唯一合法 route（fail-closed）。

        步骤：
        1. 校验策略 defaultDecision 必须为 DENY_UNMAPPED_TASK（未映射默认拒绝语义）；
        2. 筛出 taskType 匹配的所有规则；
        3. 无匹配 → 未映射，拒绝；
        4. 按 priority 升序取最高优先权规则；若存在同优先级并列（歧义）→ 拒绝（不随机选）。
        """
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
            # 同优先级并列 → route 歧义，禁止随机选择（fail-closed）
            fail(f"route conflict/ambiguity for {task_type} at priority {top_priority} (fail-closed)")
        return candidates[0]

    def _plan_for(self, task_type: str, customer_id: str, permission: str) -> dict:
        """为单个场景生成可重放的 shadow ActivationPlan（fail-closed）。

        完整管线：权限校验 → route 解析 → 合同加载 → route/合同一致性 → 资产解析 → 计划组装。
        任何一步失败都抛出 ValueError（fail-closed），绝不返回部分计划。
        """
        # ── 1. 权限护栏：权限未决一律拒绝 ───────────────────────────
        if permission in PERMISSION_PENDING:
            fail("permission decision missing or pending (fail-closed)")

        # ── 2. 路由解析：取得唯一合法 route ─────────────────────────
        rule = self._resolve_route(task_type)
        ref = rule.get("activationContractRef")
        # 范围校验：合同引用为 AC-NOT-IN-P20 说明任务在 P20 之外。
        if ref == NOT_IN_P20_REF:
            fail(f"task {task_type} maps to contract outside P20 scope (DENY_NOT_IN_P20)")

        # ── 3. 激活合同加载 ─────────────────────────────────────────
        contract = self.activations.get(ref)
        if contract is None:
            fail(f"activation contract {ref} not resolvable (fail-closed)")
        # 引用完整性：route 与合同的 taskType/routeMode 必须一致（防合同漂移）。
        if contract["taskType"] != task_type or contract["routeMode"] != rule["mode"]:
            fail(f"route policy and activation contract disagree for {task_type} (DENY_CONTRACT_MISMATCH)")

        # ── 4. 资产解析：合同声明的激活资产必须全部已登记 ───────────
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

        # ── 5. 计划组装：构造可重放的 ActivationPlan ────────────────
        context = contract.get("context", {})
        return {
            "schemaVersion": "1.0.0",
            "planId": f"AP-{task_type}-{customer_id}-SHADOW",   # 确定性派生计划 ID
            "taskId": f"TASK-{task_type}-{customer_id}-SHADOW",  # 确定性派生任务 ID
            "taskType": task_type,
            "routeMode": rule["mode"],
            "selectedAssets": selected_assets,
            "semanticQueries": contract.get("semanticQueries", []),
            "ruleChecks": contract.get("ruleChecks", []),
            "skills": contract.get("skills", []),
            "context": {"maxTokens": context.get("maxTokens"), "trimPolicy": context.get("trimPolicy")},
            "permissionDecisionId": permission,
            "planHash": plan_hash(rule["mode"], selected_assets, contract),  # 可重放哈希
        }

    def _assert_matches_golden(self, plan: dict, scenario: str) -> None:
        """把计划与黄金计划的确定性字段逐一比对，不一致即 fail-closed。"""
        golden = read_json(self.root / GOLDEN_BY_SCENARIO[scenario])
        for field in DETERMINISTIC_FIELDS:
            if plan.get(field) != golden.get(field):
                fail(f"scenario {scenario}: deterministic field {field} differs from golden")

    def run(self, scenarios: list[str]) -> dict:
        """执行全部场景并汇总 shadow 证据。

        证据固定声明 formal_output_changed=False（shadow 只读，不改变正式输出）。
        """
        evidence = {"mode": "shadow", "scenarios": [], "formal_output_changed": False}
        for scenario in sorted(scenarios):
            plan = self._plan_for(scenario, "CUST-001", "PD-SHADOW-ALLOW")
            self._assert_matches_golden(plan, scenario)  # 每个场景都必须与黄金一致
            evidence["scenarios"].append({"scenario": scenario, "plan": plan})
        return evidence


def main() -> int:
    """CLI 入口：解析参数、执行 shadow E2E、写出证据，返回退出码（0=PASS，2=FAIL）。"""
    parser = argparse.ArgumentParser(description="P20 two-scenario shadow E2E (fail-closed)")
    parser.add_argument("--mode", required=True)  # 必须为 shadow
    parser.add_argument("--scenario", action="append", required=True)  # 可多次
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--out", type=Path, default=None, help="shadow evidence output dir (default: <root>/loops/P20-wiki-ontology-fusion/evidence/shadow-e2e)")
    args = parser.parse_args()

    try:
        # ── 1. 模式护栏：非 shadow 拒绝 ──────────────────────────────
        if args.mode not in ALLOWED_MODES:
            fail(f"unsupported mode {args.mode}; P20 allows only shadow (fail-closed)")

        # ── 2. 场景护栏：必须且仅两个已批准场景 ──────────────────────
        scenarios = list(args.scenario)
        if not scenarios or set(scenarios) != APPROVED_SCENARIOS:
            fail(f"exactly the two approved scenarios are required, got {sorted(scenarios)}")
        if len(scenarios) != len(set(scenarios)):
            fail("duplicate scenario (fail-closed)")

        # ── 3. 执行 shadow E2E 并写出证据 ────────────────────────────
        e2e = ShadowE2E(args.root.resolve())
        evidence = e2e.run(scenarios)

        # 证据只写入 Loop 证据目录（不触碰任何正式/生产输出）
        out_dir = (args.out or args.root / "loops" / "P20-wiki-ontology-fusion" / "evidence" / "shadow-e2e").resolve()
        out_dir.mkdir(parents=True, exist_ok=True)
        out_file = out_dir / "shadow-e2e-evidence.json"
        out_file.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")

        print("shadow-e2e: PASS")
        print(f"shadow-e2e: evidence written to {out_file}")
        print(f"shadow-e2e: scenarios={sorted(s['scenario'] for s in evidence['scenarios'])} formal_output_changed=False")
        return 0
    except ValueError as exc:
        # 所有 fail-closed 失败都汇聚到这里，输出 FAIL 与原因。
        print(f"shadow-e2e: FAIL: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
