# P20 独立 QA 交叉评审报告（只读）

```text
REVIEW_VERDICT=PASS_WITH_ISSUES
BLOCKERS=（无）
MAJOR=（无）
MINOR=5 项（详见下）
REVIEW_BY=independent_qa（独立评审 Agent，只读）
REVIEWED_AT=2026-08-18
REVIEW_SCOPE=shadow 基础设施（readers/planner/route evaluator/semantic query guard/context assembly/shadow e2e）
NOTE=独立交叉评审；qa_attest/QA_PASS 由独立 QA Actor 另行执行，本报告不签署 QA_PASS
```

## 评审结论摘要

- **合同合规**：PASS — record 字段与 6 项 schema 完全对齐，未发明合同未定义字段。
- **分层**：PASS — 严格 Port→Adapter→App；模块间仅依赖领域 record/Port，不依赖实现类。
- **Fail-closed**：PASS（有 MINOR）— 决策矩阵覆盖 ROUTE/SEC 用例；无部分计划、无吞异常、无堆栈泄露。
- **测试充分性**：PASS（有 MINOR）— 通过+全部拒绝路径覆盖；集成测试用真实合同数据比对黄金确定性字段。
- **越界审计**：PASS — 未启用 fusion、未迁移生产、未写回、未改现有行为、未手工编辑 generated/。
- **证据纪律**：PASS — DEV_SELF_CHECK 未误标 QA_PASS；FAILURES.md append-only。
- **路径安全**：PASS — 防路径越界/符号链接/分隔符注入。

## MINOR 项与处置

| # | 发现 | 处置 |
|---|---|---|
| MINOR-1 | planHash 跨实现（Java/Python）规范化不一致（黄金为占位符，未影响 golden 校验） | 记录；AC-4 需统一规范化算法（refinement） |
| MINOR-2 | Java RoutePolicy.findRule 对同优先级并列未 fail-closed | **已修复**（commit 78e5372：歧义返回空） |
| MINOR-3 | reader 必需字段校验未完全对齐 schema.required（真实数据均合法） | 记录为健壮性缺口（refinement） |
| MINOR-4 | Context 装配使用随机 UUID/now/hashCode，context 层不可重放 | 记录；context 层可重放非本 Loop 硬性要求 |
| MINOR-5 | Java IT 以字面量比对黄金字段而非读黄金 JSON | 记录；Python E2E 已承担读文件比对 |

## 阻断项（供协调）

- `ready_for_independent_qa` 状态需 **所有 Loop Gate 通过**（loop_guard 强制 all_pass）。
- 当前 `backend_test` 因 **基线** OWASP npm nanoid + apps/api JaCoCo 覆盖率 <0.80 失败（BASE-P20-G0-002）。
- 结论：**基线治理是独立 QA attestation 的前置**，需 Owner 授权后处理。
