# ADR-0011: Hexagonal Architecture (Ports & Adapters)

## Status

Accepted

## Context

The GITS Knowledge Engineering system spans multiple bounded contexts: operational ontology, semantic runtime, engagement scenarios, evaluation, and human-action orchestration. Each context has distinct domain logic that must remain independent of infrastructure choices (databases, messaging, external APIs).

Key concerns:
- **Testability**: Domain logic must be testable without infrastructure (databases, HTTP servers, message brokers).
- **Substitutability**: External systems (CRM, LLM providers, DMN engines) must be swappable without modifying domain logic.
- **Modularity**: Each module should expose a clean API surface and hide implementation details.
- **Evolution**: New adapters (e.g., a different LLM provider or database) should be addable without changing domain code.

## Decision

Adopt **Hexagonal Architecture** (also known as Ports & Adapters) for all domain modules:

- **Domain core** (in `modules/`) contains business logic, domain models, and port interfaces. It has zero dependency on infrastructure frameworks.
- **Ports** are interfaces defined in the domain layer (`port/` sub-packages). There are two kinds:
  - **Inbound ports** (driving): Service interfaces called by external actors (controllers, CLI, event listeners).
  - **Outbound ports** (driven): Repository and channel interfaces the domain needs from infrastructure.
- **Adapters** (in `adapters/` and `apps/`) implement port interfaces:
  - **Inbound adapters**: REST controllers, event listeners, scheduled tasks.
  - **Outbound adapters**: JDBC repositories, HTTP clients, file system accessors.
- **Dependency direction**: Adapters depend on domain ports; domain never depends on adapters.

Concrete examples in this codebase:
- `OperatingCaseRepository` (port) → `JdbcOperatingCaseRepository` (adapter)
- `ClaimReconciliationPort` (port) → `DmnClaimReconciliationAdapter` / `FallbackClaimReconciliationAdapter` (adapters)
- `LlmClient` (port) → `MockLlmClient` / `RealLlmClient` (adapters)
- `CrmWritebackChannel` (port) → `HttpCrmWritebackChannel` / `LoggingCrmWritebackChannel` (adapters)

## Consequences

### Positive

- **Domain purity**: Business logic is framework-agnostic and independently testable.
- **Swappability**: Adapters can be replaced (e.g., mock LLM → real LLM) via configuration without touching domain code.
- **Clear boundaries**: Module boundaries are explicit through port interfaces; no hidden infrastructure coupling.
- **Parallel development**: Domain and adapter teams can work independently once port contracts are agreed.

### Negative

- **Interface proliferation**: Every external dependency requires a port interface, increasing code volume.
- **Mapping overhead**: Data must be mapped between domain models and adapter-specific formats (e.g., domain record → JDBC row).
- **Learning curve**: Developers must understand the port/adapter pattern and resist the temptation to inject infrastructure directly into domain services.

### Mitigations

- Keep port interfaces minimal — only methods actually needed by the domain.
- Use Java records for immutable data transfer to reduce mapping boilerplate.
- Spring's component scanning and `@Profile`/`@ConditionalOnProperty` simplify adapter switching.
