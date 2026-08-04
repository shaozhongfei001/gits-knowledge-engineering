# ADR-0012: DMN Engine Selection — Lightweight XML Parsing vs KIE

## Status

Accepted

## Context

The claim reconciliation module requires a decision engine to evaluate DMN (Decision Model and Notation) decision tables. The `claim-reconciliation.dmn` file defines rules for reconciling claims based on conflict detection, authoritative match, and evidence completeness.

Two approaches were considered:

1. **KIE DMN Runtime** (`org.kie:kie-dmn-core`): The reference DMN implementation from the Drools/jBPM ecosystem. Full DMN 1.2+ compliance, built-in type system, decision table evaluation, and extensive tooling.

2. **Lightweight XML Parsing** (`jackson-dataformat-xml`): Parse the DMN XML file directly, extract decision rules, and evaluate them with hand-written Java logic. Only supports the specific decision table format used in this project.

Key constraints:
- Spring Boot 3.x uses the Jakarta EE namespace (`jakarta.*`), while KIE DMN Core depends on `javax.*` (Java EE), causing classpath conflicts.
- The project only uses a single simple decision table (hit policy: UNIQUE, 3 input columns, 4 rules).
- The DMN file is maintained manually and follows a predictable structure.

## Decision

Use **Lightweight XML Parsing** with `jackson-dataformat-xml`.

The `DmnClaimReconciliationAdapter` parses the DMN XML, extracts the decision table rules, and evaluates them with straightforward Java condition matching. A `FallbackClaimReconciliationAdapter` provides the same logic as hardcoded Java for environments where XML parsing is unavailable.

## Consequences

### Positive

- **No Jakarta/Javax conflict**: Avoids the KIE DMN Core dependency on `javax.*` packages, which conflicts with Spring Boot 3.x's `jakarta.*` namespace.
- **Minimal footprint**: `jackson-dataformat-xml` is already a transitive dependency; no additional heavy runtime.
- **Simplicity**: For a single decision table with 4 rules, hand-written evaluation is easier to debug and reason about.
- **Fast startup**: No KIE knowledge base compilation overhead.

### Negative

- **Not DMN-compliant**: The implementation only handles the specific DMN format used in this project; it is not a general DMN runtime.
- **Manual maintenance**: If the DMN file format changes (e.g., new hit policy, compound conditions), the parser must be updated.
- **No DMN tooling**: Cannot leverage KIE's validation, testing, or visualization tools.

### Mitigations

- The `FallbackClaimReconciliationAdapter` ensures the reconciliation logic works even without the DMN file.
- The DMN file structure is versioned and changes are gated by code review.
- If the project later requires full DMN compliance (multiple decision tables, complex hit policies), a migration to KIE can be reconsidered once KIE releases a Jakarta-compatible version.
