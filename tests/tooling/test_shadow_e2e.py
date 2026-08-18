from pathlib import Path
import subprocess
import sys
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts" / "run_p20_shadow_e2e.py"
SCENARIOS = ["--scenario", "PRE_VISIT_PREPARATION", "--scenario", "FACT_RECONCILIATION_30M"]


class ShadowE2ETest(unittest.TestCase):
    def test_two_scenarios_pass_in_shadow_mode(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "shadow", *SCENARIOS],
            cwd=str(ROOT),
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, msg=result.stderr)
        self.assertIn("shadow-e2e: PASS", result.stdout)
        self.assertIn("formal_output_changed=False", result.stdout)

    def test_production_mode_is_fail_closed(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "production", *SCENARIOS],
            cwd=str(ROOT),
            capture_output=True,
            text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("fail-closed", result.stderr)

    def test_third_scenario_is_rejected(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--mode", "shadow", *SCENARIOS,
             "--scenario", "MARKET_SIGNAL_DISCOVERY"],
            cwd=str(ROOT),
            capture_output=True,
            text=True,
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("two approved scenarios", result.stderr)


if __name__ == "__main__":
    unittest.main()
