import os
import stat
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts" / "bootstrap-check.sh"


def make_stub(path: Path, body: str) -> None:
    path.write_text("#!/usr/bin/env bash\n" + body, encoding="utf-8")
    path.chmod(path.stat().st_mode | stat.S_IEXEC | stat.S_IREAD)


def run_bootstrap(bin_dir: Path, cwd: Path, env_extra: dict | None = None) -> subprocess.CompletedProcess:
    env = os.environ.copy()
    env["PATH"] = f"{bin_dir}:{env.get('PATH', '')}"
    # Keep the real script readable but force it to use our PATH-first stubs.
    if env_extra:
        env.update(env_extra)
    return subprocess.run(
        ["bash", str(SCRIPT)],
        cwd=cwd,
        env=env,
        capture_output=True,
        text=True,
    )


class BootstrapWrapperTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(__file__).resolve().parent / "_bootstrap_tmp"
        self.tmp.mkdir(exist_ok=True)
        self.bin_dir = self.tmp / "bin"
        self.bin_dir.mkdir(exist_ok=True)
        self.work = self.tmp / "work"
        self.work.mkdir(exist_ok=True)

        # Stubs for tools bootstrap only invokes by name (command -v) or lightly.
        make_stub(self.bin_dir / "node", 'echo "v25.0.0"')
        make_stub(self.bin_dir / "npm", 'echo "11.6.2"')
        make_stub(self.bin_dir / "python3", 'echo "3.12"')
        make_stub(self.bin_dir / "git", 'echo "git 2.40"')
        make_stub(self.bin_dir / "rg", 'echo "ripgrep 13"')

    def tearDown(self):
        import shutil
        shutil.rmtree(self.tmp, ignore_errors=True)

    def _java_stub(self, major: str) -> None:
        make_stub(
            self.bin_dir / "java",
            f'echo "openjdk version \\"{major}.0.11\\" 2026-04-21" 1>&2',
        )
        make_stub(self.bin_dir / "javac", f'echo "javac {major}.0.11"')

    def _mvnw_stub(self, version: str = "3.9.12", executable: bool = True, fail: bool = False) -> Path:
        mvnw = self.work / "mvnw"
        if fail:
            make_stub(mvnw, 'echo "download failed" 1>&2; exit 1')
        else:
            make_stub(mvnw, f'echo "Apache Maven {version} (848fbb4bf2d427b72bdb2471c22fced7ebd9a7a1)"; echo "Maven home: /tmp"')
        if not executable:
            mvnw.chmod(mvnw.stat().st_mode & ~stat.S_IEXEC)
        return mvnw

    def test_wrapper_present_new_maven_passes(self):
        self._java_stub("21")
        self._mvnw_stub("3.9.12")
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(0, r.returncode, r.stdout + r.stderr)
        self.assertIn("bootstrap-check: PASS", r.stdout)
        self.assertIn("Maven Wrapper 3.9.12", r.stdout)
        self.assertNotIn("mvn ->", r.stdout)  # must not rely on system mvn

    def test_missing_wrapper_fails(self):
        self._java_stub("21")
        # no mvnw created
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(2, r.returncode, r.stdout + r.stderr)
        self.assertIn("Maven Wrapper missing", r.stdout)
        self.assertIn("bootstrap-check: FAIL", r.stdout)

    def test_non_executable_wrapper_fails(self):
        self._java_stub("21")
        self._mvnw_stub("3.9.12", executable=False)
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(2, r.returncode, r.stdout + r.stderr)
        self.assertIn("not executable", r.stdout)

    def test_old_wrapper_version_fails(self):
        self._java_stub("21")
        self._mvnw_stub("3.6.3")
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(2, r.returncode, r.stdout + r.stderr)
        self.assertIn("3.9+ required", r.stdout)

    def test_wrapper_download_failure_fails_closed(self):
        self._java_stub("21")
        self._mvnw_stub(fail=True)
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(2, r.returncode, r.stdout + r.stderr)
        self.assertIn("failed to report version", r.stdout)

    def test_old_java_fails(self):
        self._java_stub("11")
        self._mvnw_stub("3.9.12")
        r = run_bootstrap(self.bin_dir, self.work)
        self.assertEqual(2, r.returncode, r.stdout + r.stderr)
        self.assertIn("Java 21 required", r.stdout)


if __name__ == "__main__":
    unittest.main()
