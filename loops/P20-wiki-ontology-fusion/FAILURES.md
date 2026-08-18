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

---

## FAILURE_ID: BASE-P20-G0-003（P1a 后级联 OWASP 基线发现，需新 Owner 授权）

- **GATE**: backend_test（OWASP dependency-check）
- **COMMAND**: `make backend-test`
- **RESULT**: FAIL（P1a nanoid 已修复；但修复 nanoid 后暴露出更多被先前 abort 掩盖的基线发现）
- **P1A_NANOID=RESOLVED**：
  - 根 `package.json` 加 `overrides: {"nanoid": "^5.1.16"}`（与 `docx@8.5.0` 的 `nanoid ^5.0.4` 兼容，已证明）；
  - 用官方 registry 更新 lockfile → `nanoid@5.1.16`（Tencent 镜像仅到 5.1.9）；
  - `pom.xml` OWASP `failOnError=false`（OSS Index 401 外部服务不可达时不硬失败；`failBuildOnCVSS=7` 仍阻断真实 ≥7.0 漏洞，未弱化）；
  - `dependency-check-suppressions.xml`：修复损坏的无效 `justification` 子元素；并登记 CVE-2026-0994 假阳性豁免（该 CVE 仅影响 Python `google.protobuf.json_format.ParseDict()`，OWASP 误匹配到 Java `protobuf-java`）。
- **级联发现（被先前 nanoid abort 掩盖）——漏洞归属修正（依 Owner 决策 + Oracle 公告）**：
  - `persistence-relational`: `mysql-connector-j@9.7.0`（Oracle 公告受影响区间 9.7.0–9.7.1）
    - `CVE-2026-60586`(7.7) → **CONFIRMED_AFFECTED（Connector/J）**
    - `CVE-2026-60623`(7.1) → **CONFIRMED_AFFECTED（Connector/J）**
    - `CVE-2026-60192`(8.1)/`60193`(8.5)/`60317`(7.4) → **SCANNER_COMPONENT_MISMATCH**（Oracle 公告为 Connector/Net，非 Connector/J；无证据证明 Java Connector/J 受影响，除非另有权威证据）
  - `persistence-relational`: `log4j-api@2.24.3` → CVE-2026-34478/479/480/481（6.3~6.9，<7.0；**EXPOSURE_PENDING**，需核验运行时组件是否实际存在）
  - 可能还有更多模块在继续扫描后暴露。
- **CLASSIFICATION**: PRE_EXISTING_BASELINE（级联，非 P20 改动引入）
- **NEXT_ACTION**: Owner 授权级联安全治理（APPROVE_P20_CASCADE_SECURITY_REMEDIATION_LIMITED）：升级 Connector/J 到不在影响区的最小稳定版本；Log4j 暴露面核验并按规则处置；修复扫描 fail-open；记录级联发现。
- **OWNER_DECISION**: APPROVE_P20_CASCADE_SECURITY_REMEDIATION_LIMITED（2026-08-18）
- **RECORDED_AT**: 2026-08-18

---

## FAILURE_ID: BASE-P20-G0-004（P1c 授权内处置 + gits-kno-api 级联新发现）

- **GATE**: backend_test（OWASP dependency-check）
- **COMMAND**: `make backend-test`
- **RESULT**: FAIL（P1c 授权内项已处置；但暴露 gits-kno-api 级联新发现，超出授权范围，需新 Owner 决策）

### P1c 授权内处置结果（已完成）
- **P1C_MYSQL_CONNECTOR=TEMPORARY_MITIGATION_ACCEPTED**（依 Owner 决策2修正）
  - `OLD_VERSION=9.7.0`、`CURRENT_VERSION=9.6.0`
  - `MITIGATION_REASON=9.7.0–9.7.1 affected and no patched 9.7.x available（Maven Central 无 9.7.1+）`
  - `LONG_TERM_STATE=UPSTREAM_PATCH_PENDING`
  - **退出条件**：当 Oracle 发布不受影响且与项目兼容的新稳定 Connector/J 版本时，应重新评估并从 9.6.0 升级，不长期停留在归档版本。已登记为后续依赖维护事项（不重新打开 P1c）。
- **P1C_LOG4J=RESOLVED（组件不匹配）**：核验依赖树——项目仅有 `log4j-to-slf4j` + `log4j-api`，**无 `log4j-core`**、无 log4j2 配置文件、无 Rfc5424Layout/XmlLayout/Socket/Syslog appender。log4j-core 相关 CVE（CVE-2026-34477/478/479/480/481、CVE-2025-68161）因 log4j-core 不存在而**不可利用**，按组件不匹配窄抑制。
- **CVE-2026-49844（log4j-api，真实但 <7.0）**：CVSS 5.9(NVD)/6.3(CVSS4)，**ADVISORY_BELOW_THRESHOLD**；利用需 JsonTemplateLayout/MapMessage.asJson（本项目未用）。**不抑制**（真实 log4j-api 漏洞），记录为 advisory。
- **SCANNER_FAIL_CLOSED**：`failOnError=true` 恢复（执行/数据源错误 → FAIL）；OSS Index analyzer 用正确参数 `ossindexAnalyzerEnabled=false` 禁用（可选外部数据源 401 不可用，依 Owner 决策选项3）；新增 `scripts/dependency-check-guard.py` 完整性校验（报告非空/依赖>0/NVD 时间可识别/无 fatal/阻断漏洞）。

### 级联新发现（超出授权范围，需新 Owner 决策）
以下位于 `gits-kno-api` 运行时依赖，**真实且 ≥7.0**，**不在** `persistence-relational`/Log4j 授权范围，亦非 P20 引入：
- `tomcat-embed-core@10.1.55`：CVE-2026-55276/53434/59083/59084（**CVSS 9.1**）、CVE-2026-66299/53404（7.3/7.5）——**真实高严重度，需升级嵌入式 Tomcat（影响 API 运行时，超出授权）**
- `micrometer-registry-prometheus@1.15.12`：CVE-2026-42154（**7.5**）——真实，需升级（Spring Boot BOM 管理，超出授权）
- `swagger-ui@5.17.14`（DOMPurify JS 3.1.4）：CVE-2026-65898（5.1，<7.0 advisory）
- **CVE-2026-49844（log4j-api）**：advisory（<7.0，真实，未抑制）

- **CLASSIFICATION**: PRE_EXISTING_BASELINE（级联，非 P20 改动引入）
- **NEXT_ACTION**: 向 Owner 申请对 `gits-kno-api` 运行时依赖（tomcat-embed-core、micrometer-registry-prometheus）的安全治理授权；本批已完成授权范围内 P1c。
- **RECORDED_AT**: 2026-08-18

---

## FAILURE_ID: BASE-P20-G0-005（P1d/P1b 完成，backend_test 转绿）

- **GATE**: backend_test（OWASP + JaCoCo）
- **COMMAND**: `make backend-test`
- **RESULT**: **PASS（EXIT=0）**，`mvn verify` BUILD SUCCESS + dependency-check-guard 全报告 PASS
- **P1D_TOMCAT=RESOLVED**：`tomcat-embed-* 10.1.55 → 10.1.57`（Maven Central 最新可用 10.1.x；10.1.58 未发布）。core/el/websocket 统一 10.1.57，无 Tomcat 11，Spring Boot 3.5.16 主/次版本不变。5/6 Tomcat CVE 清除；CVE-2026-66299（examples/WebSocket chat）因嵌入式 Tomcat 无 examples、无 WebSocket 端点按组件不匹配窄抑制。
- **P1D_MICROMETER=SCANNER_COMPONENT_MISMATCH_EVIDENCED**：项目仅用 Prometheus Java client（`prometheus-metrics-core@1.3.10`）暴露 `/actuator/prometheus`；无 Prometheus server、无 `/api/v1/read` remote-read。CVE-2026-42154 精确窄抑制。
- **SWAGGER_UI_EXPOSURE=DEV_ONLY**：`application-prod.yaml` 已配置 `springdoc.api-docs.enabled=false`（生产禁用）；DOMPurify CVEs 均 <7.0，窄抑制（advisory）。
- **P1B_APPS_API_LINE_COVERAGE=0.80+（实际 80.4%）**：新增报告策略测试（12）+ V11ScenarioDataLoader 数据/空路径测试（20）+ 追加分支，JaCoCo `All coverage checks have been met`。
- **SCANNER_FAIL_CLOSED=PASS**：`failOnError=true`；OSS Index analyzer 用 `ossindexAnalyzerEnabled=false` 禁用；`dependency-check-guard.py` 校验 JSON 报告（非空/无 errorCount/无 ≥7.0 阻断；无依赖模块合法空报告不误判）。
- **CLASSIFICATION**: CLOSED（级联基线安全治理完成；所有 P1 条件满足）
- **OWNER_DECISION**: APPROVE_P20_API_RUNTIME_SECURITY_REMEDIATION_LIMITED
- **CLOSED_AT**: 2026-08-18
- **CLOSED_BY**: feature_pilot
- **RECORDED_AT**: 2026-08-18
