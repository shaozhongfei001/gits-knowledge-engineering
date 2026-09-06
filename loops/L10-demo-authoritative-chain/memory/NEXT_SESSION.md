# NEXT_SESSION · L10 → 独立 QA / L11

> 更新：2026-09-06（L10 施工会话收工）
> 计划：`PI-ARCH-IMPLEMENTATION-01` · 阶段 B
> Baton holder：`tech_lead` → 下一：`independent_qa`（L10 复核）‖ `tech_lead`（L11 施工，可并行）

---

## 1. L10 做了什么

Owner 决议 **DECISION-20260906-01**（演示路径 C）授权后，完成演示制度文本入库与
SourceVersion / Fragment 施工。4 个工作包全部 ACCEPT。

| 包 | 内容 | 关键产出 |
|---|---|---|
| W1 | DEMO 语义入合同 | V023 `provenance_state` 增 `DEMO`（双方言）；INV-EVS-09/10/11；99/99（49 条） |
| W2 | 演示文本生成 | 3 份 `.DEMO.md`，条款 19/17/15 |
| W3 | 登记与门禁 | §5.8 DEMO 演示轨；源登记门禁 46 → 67/67；红测通过 |
| W4 | 源版本与片段 | 3 SourceVersion + 51 Fragment；L10 门禁 48/48；可复现 |
| W5 | 证据收敛 | EVIDENCE_MANIFEST + 五件套 |

统一门禁 **8/8 PASS · 401 项**（L03 基线 7/7 · 326 项）。

---

## 2. 关键设计决策（后续 Loop 必须遵守）

1. **物理隔离**：演示文件固定 `SRC-CM-00x.DEMO.md`；真实制度上传后落 `SRC-CM-00x.pdf/.md`。
   门禁校验"真实制度文件尚未上传（DEMO 不冒充真实）"。
2. **统计隔离**：DEMO 3 条**单列**，真实权威区仍 **28 条 / 缺口 27/28**。
   **F-L00-07 未解除** —— 演示文本不抵扣 Owner 上传义务。
3. **合同先行**：红线要求 DEMO 标记，而 V023 枚举无该值；先改合同再实现，
   不在实现层发明枚举。
4. **内建冲突**：`SRC-CM-001` 第十条 50 万 vs `SRC-CM-003` 第六条 100 万
   → `VALUE_MISMATCH`，**故意保留**供 L12 冲突检测实证，不得静默修正。

---

## 3. 未完成 / 待决

| 项 | 类型 | 谁 |
|---|---|---|
| F-L10-01 `make check` FAIL（CTR-PR-API-001） | 承继缺陷，早于施工 | integration_contract_owner |
| O-L10-01 RLS-001 缺 `provenanceState` 字段 | 合同缺口，**L13 前必补** | Contract Owner |
| O-L10-02 SourceVersion/Fragment 无 schema 合同 | 合同缺口，建议 L11 前裁决 | Contract Owner |
| F-L00-07 真实材料 27/28 | BLOCKER | Product/Risk Owner |
| QA_PASS | 只能独立 QA 记录 | Independent QA |

---

## 4. 显式提交清单（禁止 git add .）

### GITS 仓（`/home/szf/dev/gits-cbanking`）

修改文件：
```
specs/product-knowledge/evidence-span.schema.json
specs/product-knowledge/invariants.py
specs/product-knowledge/check_invariants.py
specs/product-knowledge/check_all.py
specs/product-knowledge/migration-candidates/_tables.tpl
specs/product-knowledge/migration-candidates/_part1.sql.tpl
specs/product-knowledge/migration-candidates/h2/V023__product_knowledge_objects.sql
specs/product-knowledge/migration-candidates/mysql/V023__product_knowledge_objects.sql
```

新增目录（整目录纳入）：
```
loops/L10-demo-authoritative-chain/
```

**不属本 Loop、不要一并提交**（会话前既有改动）：
```
loops/P30-gits-bank-experience-shell/FAILURES.md
loops/P30-gits-bank-experience-shell/LOOP.yaml
loops/P38-oracle-deprecation/memory/NEXT_SESSION.md
specs/openapi/gits-kno-api.openapi.json
specs/openapi/product-recommendation.openapi.json
specs/product-recommendation/README.md
```

### KERT 仓（`/home/szf/dev/Leibniz-KERT`）

新增：
```
examples/product-recommendation-assets/01_raw/_authoritative/SRC-CM-001.DEMO.md
examples/product-recommendation-assets/01_raw/_authoritative/SRC-CM-002.DEMO.md
examples/product-recommendation-assets/01_raw/_authoritative/SRC-CM-003.DEMO.md
examples/product-recommendation-assets/tools/l10_build_source_versions.py
examples/product-recommendation-assets/tools/check_l10_sources.py
examples/product-recommendation-assets/02_work/source-versions/    (4 json)
examples/product-recommendation-assets/02_work/fragments/          (3 json)
```

修改：
```
examples/product-recommendation-assets/01_raw/source-registry.md
examples/product-recommendation-assets/tools/check_registry_consistency.py
```

建议提交信息：
```
feat(product-knowledge): L10 演示制度文本入库与 SourceVersion/Fragment

- 依 DECISION-20260906-01 路径 C 生成 3 份 DEMO 演示制度（.DEMO.md 物理隔离）
- V023 双方言 provenance_state 增 DEMO；EVS-002 增 INV-EVS-09/10/11
- SourceVersion 3 / Fragment 51，SOURCE_DATE_EPOCH 下逐字节可复现
- 源登记门禁 67/67、L10 片段门禁 48/48、统一门禁 8/8（401 项）
- F-L00-07 未解除：真实权威材料缺口仍 27/28，DEMO 不抵扣

Loop: L10
Gates: 3/3 PASS · backend no-regression · DEMO provenance 三道防冒充闸
```

哈希锚点：`loops/L10-demo-authoritative-chain/EVIDENCE_MANIFEST.json`

---

## 5. 下一步（L11）

L11 范围（Owner 已授权）：
- 从 51 个 Fragment 抽取 **EvidenceSpan**（`usage=AUTHORITATIVE`，`evidenceId = EVS-{sid}-{sha256(quote)[:8]}`）
- 按 **CTR-PK-FLD-001 七字段**编译 FieldAssertion
- 前置：O-L10-02 是否需要 `CTR-PK-SVD-001/FRG-001` 合同的裁决（不阻塞，可用 V023 列为真值）

L11 开工前必读：`EVIDENCE.md`、`FAILURES.md`、本文件、`01_raw/source-registry.md` §5.8。
