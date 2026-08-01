from pathlib import Path
import subprocess
import sys
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts" / "semantic_rule_gate.py"


class SemanticRuleGateTest(unittest.TestCase):
    def test_gate_passes_on_repo_artifacts(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT)],
            cwd=str(ROOT),
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, msg=result.stderr)
        self.assertIn("semantic-rule-gate: PASS", result.stdout)


if __name__ == "__main__":
    unittest.main()
