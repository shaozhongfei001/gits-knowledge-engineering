# P20｜Failures（append-only）

任何Gate失败必须先记录时间、Gate、原命令、退出码、证据文件、初步分类和下一动作，再修改合同或实现。修复后追加根因、变更Commit和原命令重跑结果，不覆盖原记录。

---

## FAILURE_ID: ENV-P20-G0-001

- **GATE**: semantic_rule_gate
- **COMMAND**: `make check` / `make tooling-test`
- **RESULT**: FAIL
- **ERROR**: `LinkML: No module named 'yaml'`
- **CLASSIFICATION**: ENVIRONMENT_DEPENDENCY
- **ROOT_CAUSE**: PyYAML 未安装到 `gits-kno-p20-venv`
- **NEXT_ACTION**: 安装 PyYAML 并复跑原 Gate
- **GATE_DECISION**: BLOCKED（环境依赖阻断，非 P20 合同失败，不需要回滚）
- **BLOCKER**: PYTHON_DEPENDENCY_MISSING
- **MISSING_PACKAGE**: PyYAML
- **PASSED_GATES**: 合同生成、知识架构校验、Loop Guard、秘密扫描、枚举检查、SHACL、Schema、DMN、框架测试（tooling-test 实际 13/14 通过）
- **RECORDED_AT**: 2026-08-18

### 关闭记录（CLOSURE）

```text
ROOT_CAUSE=PyYAML tooling dependency unavailable in active P20 virtual environment
REMEDIATION=dependency installed and declared in repository tooling dependencies (requirements-tooling.txt)
RETEST_RESULT=PASS
CLOSURE_STATE=CLOSED
CLASSIFICATION=environment/reproducibility (not a LinkML contract failure)
```

- **复跑命令**（使用 `gits-kno-p20-venv` 内 Python）：
  - `./gits-kno-p20-venv/bin/python scripts/semantic_rule_gate.py` → `LinkML: PASS`，`EXIT=0`
  - `make PYTHON=./gits-kno-p20-venv/bin/python tooling-test` → 14/14 OK，`EXIT=0`
  - `make PYTHON=./gits-kno-p20-venv/bin/python check` → `PASS`（含 secret-scan PASS with 62 advisories），`EXIT=0`
- **修复动作**：
  1. 新增 `requirements-tooling.txt`，声明 `PyYAML>=6.0`（及 semantic gate SHACL 依赖 `rdflib>=6.2`），沿用仓库 `requirements*.txt` 依赖管理方式。
  2. 创建 `gits-kno-p20-venv`，`pip install -r requirements-tooling.txt`，验证新环境可导入 `yaml` 并运行 semantic rule gate。
  3. `gits-kno-p20-venv/` 加入 `.gitignore`，避免误提交虚拟环境。
  4. 修复 `scripts/secret_scan.py` 的 git worktree 检测（worktree 的 `.git` 是指针文件而非目录，此前导致 `rglob` 全盘扫描 venv 产生误报阻塞）；现通过 `git ls-files --exclude-standard` 尊重 `.gitignore`。
- **变更文件**：`requirements-tooling.txt`（新增）、`.gitignore`、`scripts/secret_scan.py`
- **CLOSED_AT**: 2026-08-18
- **CLOSED_BY**: feature_pilot (tech_lead)

---

## FAILURE_ID: BASE-P20-G0-002（基线 backend-test 阻断，非本批回归）

- **GATE**: backend_test（`make backend-test`）
- **COMMAND**: `make backend-test`
- **RESULT**: FAIL
- **ERROR**: 两处基线条件，与本批 P20 Java 改动无关：
  1. OWASP dependency-check：`package-lock.json?nanoid@5.1.9`（GHSA-28wg-ghj8-5hjv，CVSS 5.9 ≥ 7.0 阈值）——来自前端 npm 依赖；
  2. `apps/api` JaCoCo 行覆盖率 0.69 < 0.80。
- **CLASSIFICATION**: PRE_EXISTING_BASELINE
- **ROOT_CAUSE**: 仓库基线既有 OWASP npm 依赖告警与 apps/api 覆盖率未达门槛；本批仅新增独立模块（knowledge-architecture / knowledge-filesystem），未触碰 `frontend/` 或 `apps/api`。
- **EVIDENCE**:
  - `mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am verify` → BUILD SUCCESS（49 测试通过）
  - 全反应堆其它模块均 SUCCESS
- **NEXT_ACTION**: 由基线治理单独处理（npm 依赖升级/豁免 + apps/api 覆盖率补充）；不属于 P20 shadow slice 范围。
- **RECORDED_AT**: 2026-08-18
