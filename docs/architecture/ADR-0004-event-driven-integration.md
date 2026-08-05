# ADR-0004: 事件驱动集成模式

## Status

Accepted

## Context

GITS 知识工程项目包含多个领域模块（operational-ontology、scenario-hermes、human-action等），模块间需要协作完成业务流程（如客户洞察生成后触发CRM回写、访前报告完成后通知旅程编排）。直接方法调用会导致模块间紧耦合，违反六边形架构的依赖方向规则。

关键关注点：

- **模块解耦**：领域模块之间不得直接依赖，需要松耦合的通信机制。
- **可观测性**：关键业务操作需要留痕，支持审计和故障排查。
- **异步处理**：某些操作（如CRM回写、LLM调用）耗时较长，适合异步执行。
- **进程内优先**：首期部署为单体应用（api + worker），无需引入分布式消息中间件的复杂度。

## Decision

采用**事件驱动集成模式**，使用领域事件（Domain Event）作为模块间通信机制：

1. **领域事件定义**：使用 `CloudEvent` record 作为统一事件载体
   ```java
   public record CloudEvent(
       String id, String source, String type,
       String dataContentType, Object data
   ) {}
   ```

2. **事件发布端口**：在领域模块中定义 `DomainEventPublisher` 出站端口
   ```java
   public interface DomainEventPublisher {
       void publish(CloudEvent event);
   }
   ```

3. **进程内实现**：`SpringEventPublisher` 适配器包装 Spring `ApplicationEventPublisher`
   - 首期采用 Spring ApplicationEvent 机制，零额外中间件
   - 事件在同一个 JVM 内传播，保证一致性
   - 通过 `@EventListener` / `@TransactionalEventListener` 订阅

4. **事件类型**：复用 `DomainEventType` 已有常量，关键事件包括：
   - `EngagementOrchestrator`：`startJourney`/`postvisit`/`newEvidence` 后发布事件
   - `KycInsightService`：`claimCandidateRecorded` 后发布事件
   - `CrmWritebackService`：`controlledActionRequested` 后发布事件

5. **事件合同**：通过 `CTR-EVENT-001`（`domain-events.asyncapi.json`）定义事件格式规范

## Consequences

### Positive

- **模块解耦**：事件发布者无需知道订阅者，模块间仅通过事件类型隐式关联。
- **可扩展性**：新增订阅者无需修改发布者代码，符合开闭原则。
- **可审计性**：每个领域事件携带完整上下文（id、source、type、data），支持审计追踪。
- **渐进演进**：进程内 Spring Event 可平滑迁移到分布式消息中间件（Kafka/RabbitMQ），仅替换 `DomainEventPublisher` 适配器。
- **Spring 原生**：ApplicationEvent 是 Spring 框架一等公民，无需额外依赖。

### Negative

- **调试复杂度**：事件流是隐式的，调用链不如直接方法调用直观。
- **事务边界**：`@TransactionalEventListener` 需注意事务传播行为，事件处理失败可能导致数据不一致。
- **事件顺序**：Spring ApplicationEvent 默认同步执行，异步执行需 `@Async` 配置，顺序保证需额外设计。
- **无持久化**：进程内事件不持久化，应用重启后未处理事件丢失。

### Mitigations

- 事件类型和订阅关系在 `CTR-EVENT-001` 合同中显式记录，便于追踪。
- 关键操作使用 `@TransactionalEventListener(phase = AFTER_COMMIT)` 确保事务提交后再处理。
- 后续如需事件持久化或分布式传播，仅需替换 `SpringEventPublisher` 为 Kafka/RabbitMQ 适配器，领域代码无需变更。
- 事件处理失败时记录错误日志并触发告警，支持人工介入。
