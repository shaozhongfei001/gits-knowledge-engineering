# ADR-0016｜统一ActivationPlan作为双路径执行合同

状态：`PROPOSED_FOR_P20_OWNER_REVIEW`

## Context

如果Wiki-first和Ontology-first各自直接调用Skill，会产生两套权限、证据、上下文和运行轨迹逻辑，无法稳定比较和回放。

## Decision

任何资产调用、语义查询、规则校验和Skill调用必须先形成ActivationPlan。计划至少包含：

- 任务与routeMode；
- Knowledge Map、Route Policy、Activation Contract和Ontology版本；
- 资产ID、版本、顺序和必选性；
- 注册Semantic Query ID；
- 规则检查；
- Skill及版本；
- Context预算和裁剪策略；
- permissionDecisionId；
- planHash和plannerVersion。

计划校验失败时不执行任何资产激活。相同受控输入应产生相同的稳定字段。

## Consequences

- 可在Shadow模式比较新旧链路；
- 可以替换Mock RAG、Graph或资产目录而不改变上层任务合同；
- 每次运行多一个规划与校验步骤；
- 计划Schema兼容性成为正式治理对象。
