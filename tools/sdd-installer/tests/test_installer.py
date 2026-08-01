from __future__ import annotations

import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]
INSTALLER = ROOT / "installer.py"


class InstallerTest(unittest.TestCase):
    def run_installer(self, payload: Path, target: Path, action: str, expected: int = 0):
        result = subprocess.run(
            [sys.executable, str(INSTALLER), "--payload", str(payload), "--target", str(target), action],
            text=True,
            capture_output=True,
            check=False,
        )
        self.assertEqual(expected, result.returncode, result.stdout + result.stderr)
        return result

    def test_plan_apply_idempotency_update_and_rollback(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            payload, target = root / "payload", root / "target"
            payload.mkdir()
            target.mkdir()
            (payload / "config.txt").write_text("v1\n", encoding="utf-8")

            self.run_installer(payload, target, "--plan")
            self.assertFalse((target / "config.txt").exists())
            first = self.run_installer(payload, target, "--apply")
            first_manifest = json.loads(first.stdout)["manifest"]
            self.assertEqual("v1\n", (target / "config.txt").read_text(encoding="utf-8"))

            second = self.run_installer(payload, target, "--apply")
            second_manifest = json.loads(second.stdout)["manifest"]
            actions = {item["action"] for item in second_manifest["files"]}
            self.assertEqual({"UNCHANGED"}, actions)
            self.assertTrue(second_manifest["noop"])
            self.assertEqual(first_manifest["install_id"], second_manifest["install_id"])

            (payload / "config.txt").write_text("v2\n", encoding="utf-8")
            self.run_installer(payload, target, "--apply")
            self.assertEqual("v2\n", (target / "config.txt").read_text(encoding="utf-8"))
            self.run_installer(payload, target, "--rollback")
            self.assertEqual("v1\n", (target / "config.txt").read_text(encoding="utf-8"))
            restored = json.loads((target / ".hzb-sdd/install-manifest.json").read_text(encoding="utf-8"))
            self.assertEqual(first_manifest["install_id"], restored["install_id"])

    def test_conflict_is_fail_closed_and_non_mutating(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            payload, target = root / "payload", root / "target"
            payload.mkdir()
            target.mkdir()
            (payload / "config.txt").write_text("framework\n", encoding="utf-8")
            (target / "config.txt").write_text("user\n", encoding="utf-8")
            self.run_installer(payload, target, "--apply", expected=3)
            self.assertEqual("user\n", (target / "config.txt").read_text(encoding="utf-8"))
            self.assertFalse((target / ".hzb-sdd/install-manifest.json").exists())

    def test_symlink_in_payload_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            payload, target = root / "payload", root / "target"
            payload.mkdir()
            target.mkdir()
            outside = root / "outside.txt"
            outside.write_text("x", encoding="utf-8")
            (payload / "link.txt").symlink_to(outside)
            self.run_installer(payload, target, "--plan", expected=2)


if __name__ == "__main__":
    unittest.main()
