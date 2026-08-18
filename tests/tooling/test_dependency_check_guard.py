from pathlib import Path
import json
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
GUARD = ROOT / "scripts" / "dependency-check-guard.py"


def run_guard(report_path):
    return subprocess.run(
        [sys.executable, str(GUARD), "--report", str(report_path)],
        cwd=str(ROOT), capture_output=True, text=True,
    )


def write_report(path, deps=None, error_count=0, report_date="2026-08-18T00:00:00Z"):
    path.write_text(json.dumps({
        "projectInfo": {"reportDate": report_date},
        "scanInfo": {"errorCount": error_count},
        "dependencies": deps or [],
    }), encoding="utf-8")


def dep_with_vuln(pkg, cve, base_score):
    return {
        "packages": [{"id": pkg}],
        "vulnerabilities": [{"name": cve, "cvssv3": {"baseScore": base_score}}],
    }


class DependencyCheckGuardTest(unittest.TestCase):
    """dependency-check-guard 自测（Owner 决策 2.3 要求的 10 路径）。"""

    def test_01_report_missing_fails(self):
        with tempfile.TemporaryDirectory() as td:
            result = run_guard(Path(td) / "missing.json")
            self.assertNotEqual(0, result.returncode)

    def test_02_empty_report_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            p.write_text("", encoding="utf-8")
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)

    def test_03_invalid_json_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            p.write_text("{not valid json", encoding="utf-8")
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)

    def test_04_dependencies_not_list_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            p.write_text(json.dumps({"dependencies": "nope"}), encoding="utf-8")
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)

    def test_05_missing_report_date_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            p.write_text(json.dumps({"projectInfo": {}, "scanInfo": {}, "dependencies": [{"packages": [{"id": "x"}]}]}), encoding="utf-8")
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)

    def test_06_error_count_present_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            write_report(p, deps=[{"packages": [{"id": "x"}]}], error_count=1)
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)

    def test_07_blocking_cvss_fails(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            write_report(p, deps=[dep_with_vuln("pkg:test/a@1.0", "CVE-2026-X", 7.5)])
            result = run_guard(p)
            self.assertNotEqual(0, result.returncode)
            self.assertIn("blocking", result.stderr)

    def test_08_valid_report_no_blocking_passes(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            write_report(p, deps=[{"packages": [{"id": "pkg:test/a@1.0"}]}])
            result = run_guard(p)
            self.assertEqual(0, result.returncode)
            self.assertIn("PASS", result.stdout)

    def test_09_below_threshold_advisory_not_blocking(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            write_report(p, deps=[dep_with_vuln("pkg:test/a@1.0", "CVE-2026-Y", 5.1)])
            result = run_guard(p)
            self.assertEqual(0, result.returncode, msg=result.stderr)

    def test_10_empty_deps_legitimate_module_passes(self):
        # 无外部依赖模块的合法空报告不 FAIL（区别于扫描失败，errorCount 捕获）
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "report.json"
            write_report(p, deps=[])
            result = run_guard(p)
            self.assertEqual(0, result.returncode, msg=result.stderr)


if __name__ == "__main__":
    unittest.main()
