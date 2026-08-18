# ADR-0015｜Wiki-first知识架构与本体运行融合

状态：`PROPOSED_FOR_P20_OWNER_REVIEW`

## Context

现有架构采用可编译语义合同与运营对象双核，但缺少面向LLM的知识地图、资产发现和按任务动态激活机制。场景Service当前直接选择数据和知识来源，扩展新场景时容易把资产依赖固化在代码与Prompt中。

## Decision

采用“一入口、双路径、一计划、一闭环”：

1. 所有Agent任务先读取ROOT/DOMAIN/TASK三级Knowledge Map；
2. Route Policy决定Wiki-first、本体优先或混合模式；
3. Wiki-first负责知识发现、非结构化检索、SOP和报告组织；
4. Ontology-first负责实体、关系、状态、语义边界和规则约束；
5. 两条路径必须汇合为统一ActivationPlan；
6. Skill和Agent只能消费经权限及证据校验的ContextPackage；
7. OpenWiki是知识地图投影和阅读入口，不是权威源、权限引擎或本体运行时。

## Consequences

### Positive

- 保留现有本体投资；
- 避免所有知识都建成本体；
- 避免所有场景都退化为文档RAG；
- 新场景通过地图、Manifest和Activation Contract扩展；
- 资产依赖、路由和上下文选择可审计、可回放。

### Negative

- 增加多合同和版本治理；
- 需要维护地图、Manifest、本体和Skill之间的交叉引用；
- Shadow阶段存在双运行成本；
- 需要新增统一ActivationPlan和ContextPackage。

## Controls

- 未映射任务默认拒绝；
- 未登记资产无法激活；
- 权限未决不返回上下文；
- 禁止任意SPARQL和任意文件扫描；
- 生产切换必须经过独立QA和Owner门禁。
