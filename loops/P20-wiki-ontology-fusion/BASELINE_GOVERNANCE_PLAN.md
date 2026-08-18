# P20 基线治理计划（待 Owner 授权）

```text
STATUS=NEEDS_OWNER_AUTHORIZATION
SCOPE_OUTSIDE=超出 P20 shadow 授权范围（涉及 frontend/ 与 apps/api）
RATIONALE=backend_test 为 P20 正式 Loop Gate；loop_guard 强制 ready_for_independent_qa 需 all gates pass；
        基线治理是独立 QA attestation 的前置。
CREATED_AT=2026-08-18
BY=tech_lead（不自行授权；需 Owner 明确批准后执行）
```

## 阻断 1：OWASP npm nanoid（`backend_test` / dependency-check）

- **现象**: `make backend-test` 中 dependency-check 报 `package-lock.json?nanoid@5.1.9`（GHSA-28wg-ghj8-5hjv，CVSS 5.9 ≥ 7.0 阻断阈值）。
- **来源**: 根 `package-lock.json`，`docx@^8.5.0` 的传递依赖 `nanoid: ^5.0.4`（非直接声明，且不在 `frontend/package-lock.json`）。
- **选项**（需 Owner 选择其一）:
  1. **升级 `docx`** 到使用已修补 nanoid 的版本（更新根 `package.json` + `package-lock.json`）。
  2. **npm `overrides` 覆盖** nanoid 至已修补版本（在根 `package.json` 加 `overrides: {"nanoid": ">=5.x-patched"}` 并 `npm install`）。
  3. **加入 suppression**（不推荐：仅临时豁免，security 门禁仍不绿）。
- **影响面**: 根 `package-lock.json` / `package.json`；不改业务代码。

## 阻断 2：apps/api JaCoCo 行覆盖率 0.69 < 0.80

- **现象**: `apps/api` 的 JaCoCo 行覆盖率门槛 0.80，当前 0.69。
- **说明**: 已运行测试 283 个（4 skipped），`SemanticPatternExtractionStrategyTest` 0 测试。覆盖率缺口在现有 `apps/api` 代码，**非 P20 新增模块**。
- **选项**（需 Owner 授权）:
  1. 为 `apps/api` 现有未覆盖类补充单元测试（提升至 ≥0.80）。
  2. 调整 JaCoCo 门槛（不推荐：降低检查等级，违反"不得降低安全检查等级"）。
- **影响面**: `apps/api` 测试代码 + `pom.xml`（如需）。

## 授权请求

```text
REQUEST=Owner 授权执行 baseline governance（两个阻断项之一或全部）
DECISION_NEEDED=
  - 阻断1：选择 docx 升级 / npm overrides / 豁免 方案
  - 阻断2：是否授权补充 apps/api 测试
GATE=backend_test → 全绿后 P20 可转 ready_for_independent_qa → 独立 QA attest
```

> 注意：本计划仅记录处置选项，不构成执行授权。Tech Lead 不得在未经 Owner 批准时修改
> `frontend/`、根 `package-lock.json` 或 `apps/api` 这些超出 P20 shadow 范围的文件。
