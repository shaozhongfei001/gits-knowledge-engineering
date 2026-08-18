# P19 失败/缺陷记录

> 遵守"失败先记录 FAILURES.md，再修复"纪律

## E-1: make verify 的 db-check 需外部 GITS_KEDB_PASSWORD（环境依赖）

**严重程度**: 环境约束 — `make verify` 的 `db-check` 需外部 MySQL `GITS_KEDB_PASSWORD`（生产 gits_ke 管理库检查）。当前无人值守环境无该凭据，故 db-check 无法执行。

**判定**: 非 P19 代码缺陷。P19 后端采用本地 H2 内存库（后端规范默认），不依赖 MySQL 管理库；`backend-test`（317 tests + 15 dependency-check 报告）与 `frontend-test`（vue-tsc + vitest 100 tests + vite build）均 PASS。db-check 为生产管理库的独立检查，需 Owner 在有凭据环境执行。

## F-4: 前端 vitest 失败 — fetchCustomerJourneys 测试断言与实现不一致（scope 外，预存）

**严重程度**: MEDIUM — 真实测试失败，拖垮 `make verify` 的 `frontend-test`（vitest），但**不在 P19 scope 内**。

**发现方式**: 独立 QA 执行 `make verify`（frontend-test → `npm run test`）时复现。

**根因**: `8bdf7dd`（V1.1 期间）将 `frontend/src/api/engagement.ts` 的 `fetchCustomerJourneys` 实现改为直接返回空数组 `[]`（后端无专用列表端点，注释说明），**但未同步更新测试断言** `frontend/src/api/__tests__/engagement.spec.ts:264-268`（仍断言 `mockGet` 被调用），导致 `fetchCustomerJourneys calls GET with customer id` 失败（并触发未处理 axios error 事件）。

**Scope 判定**: P19 LOOP scope 仅含 `frontend/vite.config.ts`（端口对齐），**不含** `frontend/src/api/engagement.ts` 或该测试文件。此失败为**跨 scope 预存契约漂移**，非 P19 引入，不在 P19 修复范围内。

**影响**: `make verify` 的 `frontend-test` 无法通过。P19 正式 gate `frontend_check`（`vue-tsc --noEmit && vite build`）已通过（类型/构建无碍），此 vitest 失败需由 Owner/独立前端工作项决定修复方向（更新测试断言匹配空数组实现，或为 `fetchCustomerJourneys` 提供真实端点）。

**建议**: 记录为 P19 scope 外 open item；不因该失败将 P19 判定为 BLOCK（P19 正式 4 gate 全过），但 `make verify` 全绿需待该前端测试修复。

## F-3: 外联脚本 channel null NPE (500)

**严重程度**: HIGH — 外联脚本端点在外联渠道缺失时返回 500 而非 400

**发现方式**: E2E 29 端点全量验证 [6/29]

**根因**: `EngagementJourneyController.generateOutreachScript` 直接 `OutreachChannel.valueOf(request.channel())`，当 `request.channel()` 为 null 时，`Enum.valueOf(null)` 抛 `NullPointerException`（"Name is null"），而 catch 仅捕获 `IllegalArgumentException`，导致 NPE 逃逸到 GlobalExceptionHandler 返回 500。

**修复**: 增加 channel null/blank 判空，返回 400。
```java
if (request.channel() == null || request.channel().isBlank()) {
    return ResponseEntity.badRequest().build();
}
```

**验证**: E2E [6/29] 从 500 变 200；后端 283 测试通过。

## 已核实为派工单过时项（非当前缺陷）

派工单 TECH_LEAD_DISPATCH_V11_DEMO_READY (2026-08-12) 中的以下项，经 2026-08-14 实证已解决：

| 派工单项 | 现状 |
|---------|------|
| B1 GateType 枚举 400 | 已解决 — Java/OpenAPI/SQL 三处一致 |
| B2 信号确认 500 | 已解决 — 后端 283 测试通过 |
| M2 KYC 缺口无种子 | 已解决 — V017 有 2 条 kyc_gap_profile 种子 |
| M3 访后 NPE | 已解决 — SemanticPatternExtractionStrategy 已有 null-guard |
| L1 吞异常堆栈 | 已解决 — application.yaml 已配 include-stacktrace: always |
| F-1 Mapper 内部类 | 已解决 — ReconciliationStatus 顶级枚举 + InstantTypeHandler |
| F-2 Instant TypeHandler | 已解决 — 编译/测试通过 |
