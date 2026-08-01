from pathlib import Path
import importlib.util
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("secret_scan", ROOT / "scripts/secret_scan.py")
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class SecretScanTest(unittest.TestCase):
    def test_real_credential_and_personal_path_are_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            (root / "bad.txt").write_text("pass" + "word=real-value\n/path=/ho" + "me/person/private\n", encoding="utf-8")
            types = {item["type"] for item in MODULE.scan_root(root)}
            self.assertEqual({"POSSIBLE_CREDENTIAL", "PERSONAL_ABSOLUTE_PATH"}, types)

    def test_environment_placeholder_is_allowed(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            (root / ".env.example").write_text("PASSWORD=${GITS_KEDB_PASSWORD}\n", encoding="utf-8")
            self.assertEqual([], MODULE.scan_root(root))


if __name__ == "__main__":
    unittest.main()
