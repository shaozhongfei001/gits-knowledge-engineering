from pathlib import Path
import importlib.util
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("readonly_guard", ROOT / "tools/quarantine/oracle/readonly_guard.py")
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class ReadOnlyGuardTest(unittest.TestCase):
    def test_guard_self_test_covers_accept_and_fail_closed(self):
        MODULE.self_test()


if __name__ == "__main__":
    unittest.main()
