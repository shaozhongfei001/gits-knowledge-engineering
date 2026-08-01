from pathlib import Path
import json
import shutil
import subprocess
import sys
import time
import unittest


ROOT = Path(__file__).resolve().parents[2]


class LoopGuardTest(unittest.TestCase):
    def test_dummy_command_is_rejected(self):
        loop_id = f"TST-guard-{int(time.time() * 1000)}"
        destination = ROOT / "loops" / loop_id
        try:
            created = subprocess.run(
                [sys.executable, str(ROOT / "scripts/new_loop.py"), "--loop-id", loop_id, "--holder", "test_actor"],
                cwd=ROOT,
                text=True,
                capture_output=True,
                check=False,
            )
            self.assertEqual(0, created.returncode, created.stdout + created.stderr)
            spec_path = destination / "LOOP.yaml"
            spec = json.loads(spec_path.read_text(encoding="utf-8"))
            spec["gates"][0]["command"] = "echo pass"
            spec_path.write_text(json.dumps(spec, indent=2) + "\n", encoding="utf-8")
            result = subprocess.run(
                [sys.executable, str(ROOT / "scripts/loop_guard.py"), "--loop", loop_id],
                cwd=ROOT,
                text=True,
                capture_output=True,
                check=False,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertIn("dummy command prohibited", result.stderr)
        finally:
            shutil.rmtree(destination, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
