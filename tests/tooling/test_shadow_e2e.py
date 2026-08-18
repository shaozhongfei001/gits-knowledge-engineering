from pathlib import Path
import json
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts" / "run_p20_shadow_e2e.py"
SCENARIOS = ["--scenario", "PRE_VISIT_PREPARATION", "--scenario", "FACT_RECONCILIATION_30M"]


def run_e2e(extra_args=None, out_dir=None):
    args = [sys.executable, str(SCRIPT), "--mode", "shadow", *SCENARIOS]
    if out_dir is not None:
        args += ["--out", str(out_dir)]
    if extra_args:
        args += extra_args
    return subprocess.run(args, cwd=str(ROOT), capture_output=True, text=True)


class ShadowE2ETest(unittest.TestCase):
    def test_two_scenarios_pass_in_shadow_mode(self):
        result = run_e2e()
        self.assertEqual(0, result.returncode, msg=result.stderr)
        self.assertIn("shadow-e2e: PASS", result.stdout)
        self.assertIn("formal_output_changed=False", result.stdout)

    def test_production_mode_is_fail_closed(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "production", *SCENARIOS],
            cwd=str(ROOT), capture_output=True, text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("fail-closed", result.stderr)

    def test_third_scenario_is_rejected(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "shadow", *SCENARIOS,
             "--scenario", "MARKET_SIGNAL_DISCOVERY"],
            cwd=str(ROOT), capture_output=True, text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("two approved scenarios", result.stderr)

    # ── P3 深化：可重放性与确定性 ────────────────────────────────────

    def test_repeat_run_is_identical(self):
        """重复运行两次：输出证据完全一致（planHash 可重放、确定性输出）。"""
        with tempfile.TemporaryDirectory() as td1, tempfile.TemporaryDirectory() as td2:
            r1 = run_e2e(out_dir=td1)
            r2 = run_e2e(out_dir=td2)
            self.assertEqual(0, r1.returncode, msg=r1.stderr)
            self.assertEqual(0, r2.returncode, msg=r2.stderr)
            ev1 = json.loads((Path(td1) / "shadow-e2e-evidence.json").read_text(encoding="utf-8"))
            ev2 = json.loads((Path(td2) / "shadow-e2e-evidence.json").read_text(encoding="utf-8"))
            # 两次运行的计划内容完全一致（含 planHash）
            self.assertEqual(ev1, ev2)
            # planHash 可重放：两次哈希相同
            for s1, s2 in zip(ev1["scenarios"], ev2["scenarios"]):
                self.assertEqual(s1["plan"]["planHash"], s2["plan"]["planHash"])
            # formal_output_changed 必须为 False（未改变正式输出）
            self.assertFalse(ev1["formal_output_changed"])

    def test_plan_hash_is_replayable(self):
        """同一场景多次运行 planHash 稳定（确定性可重放）。"""
        with tempfile.TemporaryDirectory() as td:
            r1 = run_e2e(out_dir=td)
            r2 = run_e2e(out_dir=td)
            self.assertEqual(0, r1.returncode)
            self.assertEqual(0, r2.returncode)
            ev = json.loads((Path(td) / "shadow-e2e-evidence.json").read_text(encoding="utf-8"))
            plan = next(s for s in ev["scenarios"] if s["scenario"] == "PRE_VISIT_PREPARATION")["plan"]
            # planHash 为非空 16 位十六进制
            self.assertEqual(16, len(plan["planHash"]))
            self.assertTrue(all(c in "0123456789abcdef" for c in plan["planHash"]))

    def test_empty_scenario_is_rejected(self):
        """无任何场景 → 拒绝（fail-closed）。"""
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "shadow"],
            cwd=str(ROOT), capture_output=True, text=True,
        )
        self.assertNotEqual(0, result.returncode)

    def test_duplicate_scenario_is_rejected(self):
        """重复场景 → 拒绝（fail-closed）。"""
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "shadow", "--scenario", "PRE_VISIT_PREPARATION",
             "--scenario", "PRE_VISIT_PREPARATION", "--scenario", "FACT_RECONCILIATION_30M"],
            cwd=str(ROOT), capture_output=True, text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("duplicate", result.stderr)

    def test_unsupported_mode_is_rejected(self):
        """不支持的模式（如 fusion）→ 拒绝（fail-closed）。"""
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "fusion", *SCENARIOS],
            cwd=str(ROOT), capture_output=True, text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("shadow", result.stderr)


if __name__ == "__main__":
    unittest.main()
