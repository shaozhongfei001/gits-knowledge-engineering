# ADR-0008｜领域前缀与标识从 hzb 统一为 gits

状态：`ACCEPTED`

## 背景

工程包与合同源历史上沿用杭州银行工程包标识（`hzb` / `HZB-KNO-*` / `https://gientech.com/hzb/kno/`）。项目已更名为 GITS Knowledge Engineering（`groupId=com.gien.gits`），owner 于 2026-08-02 明确要求：所有设计侧 `hzb` 前缀与归属领域标识一律改为 `gits`（例如 `hzb:Customer` → `gits:Customer`）。

## 决策

在合同源、实现、配置与治理文档中统一领域标识：

| 类别 | 旧 | 新 |
|---|---|---|
| RDF/OWL 前缀 | `hzb:` | `gits:` |
| 命名空间 URI | `https://gientech.com/hzb/kno/` | `https://gientech.com/gits/kno/` |
| 合同/制品文件名 | `hzb-core.*` / `hzb-kno-api.*` | `gits-core.*` / `gits-kno-api.*` |
| 包/应用/事件 ID | `HZB-KNO-*` / `hzb.kno.*` | `GITS-KNO-*` / `gits.kno.*` |
| Spring 应用名 | `hzb-kno-api` / `hzb-kno-worker` | `gits-kno-api` / `gits-kno-worker` |
| 隔离 profile | `HZB-ORACLE-*` | `GITS-ORACLE-*` |

边界：

- 合同源变更后必须 `make generate && make check`，禁止手工改 `generated/`；
- 历史证据日志（`loops/*/evidence/*`）与二进制 bundle 不回写，以免破坏哈希与归档；
- 不发明新业务类；`gits:Customer` 仍仅出现在 spike 映射候选中，未自动升入核心运行本体。

## 后果

- 与旧工程包标识不兼容；引用旧 URI/前缀的外部消费方需显式迁移；
- OpenAPI `packageId` const、AsyncAPI channel address、Skill `skillId` 等合同常量一并变更；
- ADR-0006（Java 包名）与本 ADR 互补：前者管 Java package，本 ADR 管领域前缀与合同标识。

## 验证

- `make generate && make check` 通过（含 semantic-rule-gate）；
- 后端/前端测试与 OpenAPI 契约测试中的 `GITS-KNO-DEV-PACKAGE-V0.1` 一致；
- 源码与合同源中无残留 `hzb`/`HZB` 设计标识（证据日志除外）。
