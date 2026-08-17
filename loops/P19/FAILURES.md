# P19 失败/缺陷记录

> 遵守"失败先记录 FAILURES.md，再修复"纪律

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
