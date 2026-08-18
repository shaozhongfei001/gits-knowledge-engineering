# ADR-0017｜OpenWiki知识投影边界

状态：`PROPOSED_FOR_P20_OWNER_REVIEW`

## Context

OpenWiki适合Markdown/OKF知识地图、人机共读和Agent导航，但不具备银行项目所需的权威源、细粒度权限、本体运行、DMN、血缘和AgentOps完整能力。

## Decision

OpenWiki仅作为可替换的外部投影和阅读入口：

```text
Authoritative specs/maps/assets
→ controlled compile/publish
→ OpenWiki projection
```

OpenWiki不得：

- 保存原始KYC和交易明细；
- 成为运营对象权威状态库；
- 直接决定权限；
- 直接执行任意工具；
- 将页面链接图等同于业务知识图谱；
- 绕过Activation Contract调用资产。

## Consequences

P20可以先用Git文件系统验证知识地图，不因OpenWiki集成阻塞核心架构。未来更换Confluence、GitBook或其他Wiki不会影响ActivationPlan和Skill/Agent运行时。
