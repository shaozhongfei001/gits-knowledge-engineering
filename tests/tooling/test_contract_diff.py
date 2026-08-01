from pathlib import Path
import importlib.util
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("contract_diff", ROOT / "scripts/contract_diff.py")
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class ContractDiffTest(unittest.TestCase):
    def test_removed_property_is_breaking(self):
        findings = []
        MODULE.walk_schema(
            {"type": "object", "properties": {"caseId": {"type": "string"}}},
            {"type": "object", "properties": {}},
            "schema",
            findings,
        )
        self.assertEqual("property_removed", findings[0]["change"])

    def test_new_optional_property_is_compatible(self):
        findings = []
        MODULE.walk_schema(
            {"type": "object", "properties": {}},
            {"type": "object", "properties": {"note": {"type": "string"}}},
            "schema",
            findings,
        )
        self.assertEqual([], findings)


if __name__ == "__main__":
    unittest.main()
