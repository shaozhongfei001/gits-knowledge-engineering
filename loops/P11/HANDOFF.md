# P11 HANDOFF — 智能升级与事件驱动

## 概述

P11 引入了 4 项重大架构变更，将系统从"模板/正则模拟"升级为"LLM增强 + DMN决策 + 事件驱动 + CRM集成"。

## 架构决策影响

### 1. LLM Client (G1/G5/G6)
- **端口**: `com.gien.gits.engagement.port.LlmClient`
- **实现**: `MockLlmClient`(返回结构化JSON) / `RealLlmClient`(调用外部API)
- **配置**: `engagement.llm.mode=mock|real`，默认 `mock`
- **影响范围**: SemanticPatternExtractionStrategy, OutreachScriptService, MeetingScriptService, 4个ReportStrategy
- **关键约束**: 所有 LLM 调用点必须有 fallback，系统在 `mode=mock` 时仍可完整运行
- **⚠️ 注意**: `RealLlmClient` 需要配置 `LLM_API_KEY`，生产环境必须设置

### 2. DMN 决策引擎 (G3)
- **端口**: `com.gien.gits.ontology.port.ClaimReconciliationPort`
- **实现**: `DmnClaimReconciliationAdapter`(轻量XML解析) / `FallbackClaimReconciliationAdapter`(手写逻辑)
- **DMN文件**: `apps/api/src/main/resources/claim-reconciliation.dmn`
- **决策表**: 3条规则 — conflict→CONFLICT_REQUIRES_HUMAN_REVIEW, match+evidence→VERIFIED_FACT, else→CANDIDATE_CLAIM
- **⚠️ 注意**: 未使用 KIE DMN 运行时(与 Spring Boot 3.x jakarta 冲突)，改用 Jackson XML 解析

### 3. 领域事件 (G2)
- **端口**: `com.gien.gits.ontology.port.DomainEventPublisher`
- **实现**: `SpringEventPublisher`(包装 `ApplicationEventPublisher`)
- **事件类型**: `DomainEventType` 常量 — CLAIM_RECONCILED, CONTROLLED_ACTION_REQUESTED 等
- **消费端**: Worker 模块 `WorkerEventHandler` 处理异步事件
- **CloudEvent**: `com.gien.gits.worker.event.CloudEvent` record

### 4. CRM 回写 (G4)
- **端口**: `com.gien.gits.humanaction.port.CrmWritebackChannel`
- **实现**: `HttpCrmWritebackChannel`(REST) / `LoggingCrmWritebackChannel`(仅日志)
- **配置**: `engagement.crm.mode=logging|http`，默认 `logging`
- **⚠️ 注意**: `mode=http` 需要配置 `CRM_BASE_URL` 和 `CRM_AUTH_TOKEN`

## 新增依赖

| 依赖 | 用途 | 模块 |
|------|------|------|
| jackson-dataformat-xml | DMN XML 解析 | apps/api |
| micrometer-tracing-bridge-brave | 链路追踪 | apps/api |
| zipkin-reporter-brave | Zipkin 上报 | apps/api |
| logstash-logback-encoder | JSON 结构化日志 | apps/api |

## 配置项速查

```yaml
engagement:
  llm:
    mode: mock          # mock|real
    base-url: https://api.openai.com
    api-key: ""         # LLM_API_KEY
    model: gpt-4o-mini
  crm:
    mode: logging       # logging|http
    writeback-url: ""   # CRM_BASE_URL
    auth-token: ""      # CRM_AUTH_TOKEN
```

## 测试覆盖

- DmnClaimReconciliationAdapterTest: 12 tests (8种输入组合 + DMN/Fallback一致性 + 指标 + 规则加载)
- HttpCrmWritebackChannelTest: WireMock 集成测试
- SpringEventPublisherTest: 事件发布验证
- MockLlmClientTest: 结构化JSON返回验证
- OutreachScriptServiceTest / MeetingScriptServiceTest: LLM fallback 验证
