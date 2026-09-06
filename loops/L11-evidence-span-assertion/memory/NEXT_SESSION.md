# NEXT_SESSION · L11 → 独立 QA / L12

> 更新：2026-09-06（L11 施工会话收工）
> 计划：`PI-ARCH-IMPLEMENTATION-01` · 阶段 B
> Baton holder：`tech_lead` → 下一：`independent_qa`（L11 复核）‖ `tech_lead`（L12 施工，可并行）

---

## 1. L11 做了什么

| 包 | 产出 |
|---|---|
| W1 | 合同核对：EVS-002 / ASM-001 / FLD-001 足以承载，**无合同变更** |
| W2 | 51 Fragment → **29 EvidenceSpan**（INV-EVS-05 资格过滤） |
| W3 | 七字段 **14 条断言**（11 CANDIDATE / 3 UNKNOWN），normalizedValue 全 null |
| W4 | `check_l11_spans.py` **528/528**；红测有效；接入 check_all（8→9 门禁） |
| W5 | 五件套 + EVIDENCE_MANIFEST |

统一门禁 **9/9 PASS · 929 项**；后端 **955/0/0/4** 无回归。

---

## 2. L12 施工必须知道的事实

1. **`minAccountBalance` 有 4 条 CANDIDATE，其中内建冲突**：
   `SRC-CM-001` 第十条 50 万元 vs `SRC-CM-003` 第六条 100 万元。
   L12 的 ConflictCase 检测**应命中此处**（`VALUE_MISMATCH`）。若未命中，说明检测逻辑有问题。
2. **三条 fail-closed 路径已实现**（继承勿改）：
   claimType 资格 / 权威级 / Owner 未裁决 → 不参与或 UNKNOWN。
3. **断言状态只有 CANDIDATE 与 UNKNOWN** —— 没有 SUPPORTED/REVIEWED，
   因为 Owner 复核在 L13，DEMO 派生断言也不得自升。
4. **产品主体是 `PROD-CM-001`**，来自 taxonomy 第 294 行，**不是** 13 张 legacy card。
   L12 候选卡编译不得回读 legacy card 文件。
5. 关键词分类表在 `tools/l11_extract_evidence_spans.py` 的 `CLAIM_KEYWORDS` /
   `FIELD_KEYWORDS`。若 Owner/QA 认为其属于"实质规则推断"，应先裁决再调整，
   **不得**为提升覆盖率私自放宽资格校验（那等于绕过 INV-EVS-05）。

---

## 3. 待决

| 项 | 谁 |
|---|---|
| O-L11-01 `identity.productCode` UNKNOWN 是否可接受（或调整源 allowedClaimTypes） | Owner / 独立 QA |
| O-L11-02 `clauseVerified=false` ⇒ 到不了 RECOMMENDATION_READY（红线必然） | 知悉即可 |
| O-L10-01 RLS-001 缺 `provenanceState`（L13 前必补） | Contract Owner |
| F-L10-01 `make check` FAIL（CTR-PR-API-001） | integration_contract_owner |
| F-L00-07 真实材料 27/28 | Product/Risk Owner |

---

## 4. 显式提交清单（禁止 git add .）

### GITS 仓
```
specs/product-knowledge/check_all.py                    (M)
loops/L11-evidence-span-assertion/                      (新增，整目录)
```

### KERT 仓
```
examples/product-recommendation-assets/tools/l11_extract_evidence_spans.py   (新增)
examples/product-recommendation-assets/tools/check_l11_spans.py              (新增)
examples/product-recommendation-assets/02_work/evidence-spans/               (新增，4 json)
examples/product-recommendation-assets/02_work/assertions/                   (新增，1 json)
```

建议提交信息：
```
feat(product-knowledge): L11 EvidenceSpan 抽取与 FieldAssertion 编译

- 51 Fragment → 29 EvidenceSpan（INV-EVS-05 资格过滤，usage=AUTHORITATIVE）
- 七字段 14 条断言：11 CANDIDATE / 3 UNKNOWN，normalizedValue 全 null（INV-ASM-07）
- 三条 fail-closed：claimType 资格 / 权威级 / Owner 未裁决
- 门禁 528/528，统一门禁 9/9（929 项），后端 955 无回归
- 冲突不合并：minAccountBalance 4 条并存，留给 L12

Loop: L11
Gates: 3/3 PASS · DEMO 派生断言不升 SUPPORTED
```

哈希锚点：`loops/L11-evidence-span-assertion/EVIDENCE_MANIFEST.json`

---

## 5. 下一步（L12）

- 冲突检测 → `ConflictCase`（预期命中 `minAccountBalance` 的 50 万 vs 100 万）
- 字段体检报告（七字段覆盖 + UNKNOWN 清单 + 阻断原因）
- 候选卡编译（**不复用 13 张 legacy card**，从断言编译）
