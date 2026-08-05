# ADR-0002: 六边形架构模块边界划分

## Status

Accepted

## Context

GITS 知识工程项目采用六边形架构（Ports & Adapters），需要明确 `modules/`、`adapters/`、`apps/` 三层之间的职责边界和依赖规则。系统包含 7 个领域模块、3 个适配器模块和 2 个应用模块，模块间的依赖关系直接影响编译隔离、可测试性和团队并行开发能力。

关键关注点：

- **领域隔离**：领域模块不得依赖基础设施框架（Spring、JDBC、Jena等），确保业务逻辑可独立测试。
- **依赖方向**：依赖必须从外向内（apps → modules ← adapters），禁止反向依赖或跨层直接引用。
- **模块间通信**：领域模块之间不得直接依赖，仅通过端口接口或领域事件进行松耦合交互。
- **适配器归属**：技术适配器（JDBC、Jena、Oracle等）独立于领域模块，通过实现端口接口注入。

## Decision

### 三层职责定义

1. **`modules/`（领域层）**：承载核心业务逻辑和领域模型，定义端口接口
   - 每个模块的 `port/` 包定义入站/出站接口
   - 领域服务（`*Service`）编排业务流程，仅依赖端口接口
   - 零基础设施框架依赖（无 Spring 注解、无 JDBC、无 HTTP 客户端）
   - 模块间依赖仅限 `operational-ontology` 作为共享内核

2. **`adapters/`（适配层）**：实现端口接口，对接外部技术设施
   - `persistence-relational`：JDBC 实现，覆盖所有 Repository 端口
   - `oracle-source`：Oracle 只读数据源适配，实现 `OracleSourcePort`
   - `semantic-jena`：Apache Jena 语义引擎适配，实现 `SemanticRepositoryPort`
   - 适配器通过 Spring `@Component` + `@Profile`/`@ConditionalOnProperty` 实现可切换

3. **`apps/`（编排层）**：组装和编排，提供入口
   - `api`：REST API 应用，包含 Controller、配置、安全、健康检查
   - `worker`：异步事件消费者应用
   - 应用层可包含不适合独立适配器的轻量实现（如 `MockLlmClient`、`RealLlmClient`、`HttpCrmWritebackChannel`）

### 依赖规则

```
apps/ ──→ modules/ (编译依赖)
adapters/ ──→ modules/ (编译依赖，实现端口)
modules/ ──→ modules/ (仅限 operational-ontology 作为共享内核)
modules/ ✗→ adapters/ (禁止)
modules/ ✗→ apps/ (禁止)
adapters/ ✗→ apps/ (禁止)
```

### 模块间依赖关系

| 模块 | 依赖 |
|------|------|
| scenario-customer-journey | operational-ontology |
| scenario-hermes | operational-ontology |
| context-evidence | operational-ontology |
| evaluation | operational-ontology |
| human-action | operational-ontology |
| semantic-runtime | （独立语义层，无运营层依赖） |
| operational-ontology | （共享内核，无其他模块依赖） |

## Consequences

### Positive

- **编译隔离**：领域模块无基础设施依赖，可独立编译和单元测试，无需启动 Spring 上下文。
- **可替换性**：适配器通过配置切换（如 `MockLlmClient` ↔ `RealLlmClient`），不修改领域代码。
- **团队并行**：端口接口一旦确定，领域开发和适配器开发可并行进行。
- **边界清晰**：Maven 模块结构强制执行依赖方向，违规在编译期即可发现。

### Negative

- **接口膨胀**：每个外部依赖需要定义端口接口，增加代码量。
- **映射开销**：领域模型与适配器数据格式之间需要映射（如 domain record → JDBC row）。
- **共享内核风险**：`operational-ontology` 作为共享内核被 5 个模块依赖，其变更影响面大。

### Mitigations

- 端口接口保持最小化——仅定义领域实际需要的方法。
- 使用 Java record 减少数据传输映射样板代码。
- `operational-ontology` 的变更须通过 CCB 评审，确保向后兼容。
- Spring `@Profile`/`@ConditionalOnProperty` 简化适配器切换配置。
