# L13 FAILURES

> Loop: `L13-release-interpretation-api` · 建立 2026-09-06

---

## F-L10-01 · MAJOR · **RESOLVED（本 Loop 修复）**

**标题**：`make check` / `make generate` 失败，报 `CTR-PR-API-001: exactly one normalized target required`

**来源**：L00 首次真实执行发现，早于 L10 施工。L13 需要 `make generate` 产出解读端点制品，
该缺陷成为**直接阻塞**，故在本 Loop 定位并修复。

**根因（两层）**：
1. **合同登记缺陷**：15 条 CANDIDATE 合同（`CTR-PR-*` 6 条 + `CTR-PK-*` 8 条 + `CTR-PK-TAX-001`）
   在 `specs/CONTRACT_INDEX.yaml` 中 `generated: []`，而生成管线要求
   `kind ∈ {openapi, openapi_paths, asyncapi, json_schema, source_contract_instance}` 必须
   有且仅有 1 个 normalized 目标。此前从未有人真正跑通 `make generate`，故一直未被发现。
2. **管线能力缺口**：`CTR-PK-TAX-001` 的 `kind=yaml_taxonomy` 不在支持列表内。

**修复**：
- 15 条合同按 `generated/<specs 子目录>/<文件名>` 约定补齐目标；
- `contract_pipeline.py` 增加 `yaml_taxonomy` 分支（YAML 加载 + `domains`/`families` 校验 + 写目标）；
- `CTR-PK-INT-001` 的 OpenAPI 版本由 3.0.3 升为 3.1.1（管线要求）。

**验证**：
```
contract-generate: PASS
generated/openapi/gits-kno-api.normalized.json：paths 53 → 54（含新解读端点）
```

**遗留**：`make check` 的 `contract-check` 已 PASS，但整体仍在下一步失败（见 F-L13-01）。

---

## F-L13-01 · BLOCKER · OPEN（既有，非本 Loop 引入）

**标题**：`make check` 在 `knowledge-architecture-check` 失败：`SP-15.json: unknown asset dependency ASSET-KNOW-PRODUCT-RULES`

**证据**：`specs/knowledge-architecture/skills/SP-15.json` 与
`activations/AC-PRODUCT-RECOMMEND-001.json` 均引用 `ASSET-KNOW-PRODUCT-RULES`，
但 `specs/knowledge-architecture/assets/` 下无该资产定义（目录下无任何 json 定义文件）。

**是否本 Loop 引入**：否。该依赖早于本计划存在；此前 `make check` 在 `contract-check`
阶段即失败，从未执行到本步骤，故首次暴露。

**处置**：**不擅自修补** —— 资产注册表属 `knowledge_architecture_owner` 域，
擅自新增资产定义会改变 P22/P23 的资产图与激活链路，超出 L10–L13 scope。
登记为 BLOCKER，交 Owner / 独立 QA 裁决：
  - 选项 A：注册 `ASSET-KNOW-PRODUCT-RULES` 资产（需定义其规范与来源）；
  - 选项 B：从 SP-15 依赖清单移除（若规则包改由 KERT Release 承载）。

---

## O-L13-01 · OBSERVATION · OPEN

**标题**：解读 API 当前对 PROD-CM-001 返回 404 而非 200

**说明**：Release 为 DRAFT（未获 Owner 签署），按 INV-CNF-02 / 红线，
未发布知识**不得**呈现，故 404 `PRODUCT_KNOWLEDGE_NOT_PUBLISHED` 是**正确行为**，非缺陷。

**解除路径**：Owner 签署决策 → `python3 tools/l13_publish_release.py --decisions <file>`
→ Release 转 PUBLISHED → API 返回 200。
注意：即使发布，`interpretationReady` 仍受 3 个 HARD 阻断约束（L12），
需 Owner 先裁决冲突与 UNKNOWN 字段方可变为 true。
