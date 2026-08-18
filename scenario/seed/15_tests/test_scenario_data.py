import json, subprocess, sys, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

class ScenarioDataTest(unittest.TestCase):
    def test_validator(self):
        p=subprocess.run([sys.executable,str(ROOT/"15_tests/validate_package.py")],capture_output=True,text=True)
        self.assertEqual(p.returncode,0,p.stdout+p.stderr)
        self.assertIn('"status": "PASS"',p.stdout)

    def test_no_go_rules_exist(self):
        text=(ROOT/"00_governance/no_go_rules.yaml").read_text(encoding="utf-8")
        self.assertIn("3000万",text)
        self.assertIn("CRM写回",text)
        self.assertIn("未经客户授权不得录音",text)

    def test_v1_legacy_preserved(self):
        self.assertTrue((ROOT/"99_legacy_v1.0_reference/00_MASTER_INDEX.md").exists())

if __name__=="__main__":
    unittest.main()
