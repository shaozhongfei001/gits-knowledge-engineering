# ADR-0005: LLM集成策略与降级模式

## Status

Accepted

## Context

GITS 知识工程项目的客户经营场景（scenario-hermes）需要集成大语言模型（LLM）能力，用于访前报告生成、访后分析、外呼脚本撰写等任务。LLM 调用具有以下特征：

- **外部依赖**：LLM 服务由第三方提供，可用性和延迟不可控。
- **成本敏感**：每次调用产生费用，需控制调用频率和 token 消耗。
- **结果不确定**：LLM 输出具有随机性，可能不符合预期格式。
- **开发环境限制**：本地开发环境可能无法访问外部 LLM 服务。

关键关注点：

- **可替换性**：LLM 提供商可能变更，领域逻辑不应绑定特定实现。
- **降级能力**：LLM 不可用时，系统应能降级到模板/正则逻辑继续运行。
- **开发友好**：本地开发应能脱离外部 LLM 服务运行。
- **配置切换**：Mock 和 Real 实现应通过配置切换，无需修改代码。

## Decision

采用**端口-适配器模式 + 双实现 + 熔断降级**策略：

1. **LlmClient 端口接口**：在 `scenario-hermes` 模块定义出站端口
   ```java
   public interface LlmClient {
       String complete(String systemPrompt, String userPrompt);
   }
   ```

2. **双实现**：
   - `MockLlmClient`（`apps/api` 中）：返回结构化 JSON 模板，用于开发/测试环境
   - `RealLlmClient`（`apps/api` 中）：调用外部 LLM API，用于 staging/生产环境

3. **配置切换**：通过 `EngagementConfig` 中 `engagement.llm.mode` 配置项控制
   - `mock`：激活 `MockLlmClient`
   - `real`：激活 `RealLlmClient`
   - Spring `@Profile`/`@ConditionalOnProperty` 实现自动切换

4. **降级策略**：
   - `LlmClient.complete()` 调用失败时抛出 `LlmClientException`
   - 调用方（Service 层）捕获异常后 fallback 到原有模板/正则逻辑
   - 降级逻辑在领域服务中实现，不依赖 LlmClient 本身

5. **健康检查**：`LlmHealthIndicator` 集成 Spring Boot Actuator，暴露 LLM 连接状态

## Consequences

### Positive

- **零耦合**：领域服务仅依赖 `LlmClient` 端口接口，不绑定任何 LLM 提供商。
- **开发效率**：`MockLlmClient` 使本地开发无需外部 LLM 服务，提升开发体验。
- **生产韧性**：降级策略确保 LLM 不可用时系统仍可正常运行，只是输出质量降低。
- **平滑迁移**：更换 LLM 提供商仅需新增适配器，领域代码零修改。
- **可观测**：`LlmHealthIndicator` 提供 LLM 服务健康状态，支持运维监控。

### Negative

- **降级质量差异**：模板/正则逻辑的输出质量低于 LLM 生成，用户体验可能不一致。
- **Mock 数据局限**：`MockLlmClient` 返回固定模板，无法模拟 LLM 的多样性和边界情况。
- **端口归属**：`LlmClient` 定义在 `scenario-hermes` 模块，如其他场景复用需跨模块依赖（见 HLD-FREEZE.md INC-004）。
- **配置复杂度**：需维护 `engagement.llm.mode` 配置项在不同环境中的一致性。

### Mitigations

- 降级时在响应中标注"模板生成"标识，让用户知晓输出来源。
- `MockLlmClient` 可扩展为支持多模板和参数化，提升测试覆盖率。
- 后续迭代可将 `LlmClient` 提取为公共端口模块，解决跨模块复用问题。
- 配置项通过环境变量注入，遵循 12-factor 原则，避免硬编码。
