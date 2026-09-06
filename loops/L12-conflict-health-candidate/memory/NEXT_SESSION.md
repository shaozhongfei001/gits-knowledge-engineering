# NEXT_SESSION · L12 → 独立 QA / L13

> 2026-09-06 · Baton: `tech_lead` → `independent_qa`

## 做了什么

冲突检测（1 个真实冲突，内建 50万 vs 100万）+ 七字段体检（3 个 HARD 阻断，
interpretationReady=false）+ 候选卡编译（不复用 legacy）。门禁 22/22。

## L13 必须知道

1. 冲突 `CNF-PRODCM001-757c6c3b`（minAccountBalance）**待 Owner 裁决**，
   L13 的决策模板已指向它。
2. 体检 3 个 HARD 阻断（identity.productCode / minAccountBalance / compliance.regulatoryBasis）
   在 Owner 裁决前，`interpretationReady` 恒为 false → **Release 无法 PUBLISHED**。
3. 只有 MONEY/RATE 做自动冲突判定；ENUM/TEXT 标 `needsHumanReview` 交人。

## 提交清单（禁止 git add .）

KERT 新增：
```
tools/l12_detect_conflicts.py
tools/check_l12_conflicts.py
02_work/conflicts/  02_work/reports/  02_work/candidate-cards/
```
GITS 新增：`loops/L12-conflict-health-candidate/`、`specs/product-knowledge/check_all.py` (M)
