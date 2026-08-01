from pathlib import Path
import importlib.util
import json
import shutil
import subprocess
import sys
import time
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("baton", ROOT / "scripts/baton.py")
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class BatonTest(unittest.TestCase):
    def test_transfer_updates_control_set_together(self):
        loop_id = f"TST-baton-{int(time.time() * 1000)}"
        destination = ROOT / "loops" / loop_id
        try:
            result = subprocess.run(
                [sys.executable, str(ROOT / "scripts/new_loop.py"), "--loop-id", loop_id, "--holder", "test_actor"],
                cwd=ROOT,
                text=True,
                capture_output=True,
                check=False,
            )
            self.assertEqual(0, result.returncode, result.stdout + result.stderr)
            MODULE.transfer(loop_id, "test_actor", "independent_qa", "ready_for_independent_qa", "test_actor")
            board = json.loads((destination / "memory/ROLE_BOARD.yaml").read_text(encoding="utf-8"))
            state = json.loads((destination / "STATE.json").read_text(encoding="utf-8"))
            self.assertEqual("independent_qa", board["baton"]["holder"])
            self.assertEqual("ready_for_independent_qa", state["status"])
            self.assertIn("`independent_qa`", (destination / "memory/NEXT_SESSION.md").read_text(encoding="utf-8"))
            self.assertIn('"state": "COMMITTED"', (destination / "memory/.baton-journal.json").read_text(encoding="utf-8"))
        finally:
            shutil.rmtree(destination, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
