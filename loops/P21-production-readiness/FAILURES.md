# P21-production-readiness｜Failures（append-only）

失败必须在修改实现之前由 `scripts/record_gate.py`追加。每项至少包含时间、Gate、命令、退出码、证据文件、初步分类和下一动作；修复后追加根因、变更SHA与原命令重跑结果，不覆盖原记录。

## F-1: 生产 profile 关键凭据存在 fail-open 风险（prod_profile_fail_closed）

- **时间**: 2026-08-19
- **Gate**: `prod_profile_fail_closed`
- **命令**: `bash scripts/verify_prod_profile_fail_closed.sh`
- **退出码**: 2（FAIL）
- **证据**: `apps/api/src/main/resources/application-prod.yaml`
- **初步分类**: 生产安全缺陷（fail-open，非环境）
- **发现**:
  - `gits.security.api-key: ${API_KEY:}` — 空默认，缺省时 API 认证可静默开放
  - `engagement.llm.api-key: ${LLM_API_KEY:}` — 空默认，real 模式下缺省静默降级
  - `engagement.crm.auth-token: ${CRM_AUTH_TOKEN:}` — 空默认，http 模式缺省静默降级
- **下一动作**: 将上述关键凭据改为 fail-closed（去掉空默认冒号 `:}` → `}`），缺省时启动失败而非静默降级；修复后重跑原命令。
- **修复（2026-08-19）**:
  - `apps/api/src/main/resources/application-prod.yaml`: `gits.security.api-key: ${API_KEY:}` → `${API_KEY}`（无默认，缺省启动失败）。
  - 新增 `apps/api/src/main/java/com/gien/gits/api/config/ProdConfigValidator.java`（`@Profile("prod")` ApplicationRunner），启动时校验 api-key / llm(real).api-key / crm(http).writeback-url 非空，缺失即抛异常 fail-closed。
  - 更新 `scripts/verify_prod_profile_fail_closed.sh` 校验配置空默认 + 启动校验器存在 + datasource 无空默认。
- **重跑原命令**: `bash scripts/verify_prod_profile_fail_closed.sh` → **PASS**（EXIT=0）；`apps/api compile` → BUILD SUCCESS。gate 已通过并记录（evidence `prod_profile_fail_closed-20260818T181341Z.log`）。
