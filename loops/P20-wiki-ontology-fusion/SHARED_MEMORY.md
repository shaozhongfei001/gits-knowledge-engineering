# P20 Shared Memory

## Owner 决策（2026-08-18）

```text
OWNER_DECISION=APPROVE_P20_SHADOW_IMPLEMENTATION
P20_PARALLEL_WITH_P19=AUTHORIZED_BY_OWNER
SCOPE=P20 wiki-ontology fusion shadow implementation only
PRODUCTION_CUTOVER=NOT_AUTHORIZED
FUSION_CUTOVER=NOT_AUTHORIZED
PRODUCTION_WRITEBACK=NOT_AUTHORIZED
INDEPENDENT_QA=PENDING
PRODUCTION_READY=NO
FROZEN=NO
decision_source=Human Owner directive supplied through the execution prompt
```

## 不变量

- Knowledge Map是Agent根入口；
- Route Policy决定主路径；
- 所有资产必须登记；
- 双路径必须汇合为ActivationPlan；
- 权限未决默认拒绝；
- 只允许注册Semantic Query ID；
- Claim不等于Fact；Signal不等于Opportunity；
- P20默认SHADOW，不改变正式业务输出；
- 既有P19的Baton、证据和QA状态不得继承或修改；
- 开发不得自签独立QA；
- 计划生成不等于实际执行；production mode/writeback 必须被拒绝。
