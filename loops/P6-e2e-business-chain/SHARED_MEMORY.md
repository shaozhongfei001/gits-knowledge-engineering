# P6-e2e-business-chain — Shared Memory

## Task Summary

| Task | Description | Status |
|------|-------------|--------|
| T1 | V002 Flyway migration: Interaction 14-field table | DONE |
| T2 | JDBC Repositories (Interaction, Claim, OperatingCase) | DONE |
| T3 | api/pom.xml: persistence-relational dependency | DONE |
| T4 | application.yaml: H2 + Flyway configuration | DONE |
| T5 | Spring Boot startup integration test | DONE |
| T6 | CustomerJourneyController (open/query/advance) | DONE |
| T7 | InteractionController (CRUD) | DONE |
| T8 | ClaimController (CRUD + status advance) | DONE |
| T9 | CustomerJourneyOrchestrator → @Service | DONE |
| T10 | FullChainE2eIT (HTTP→H2→M17→M22) | DONE |

## Key Decisions

- H2 in-memory database for dev/test; MySQL profile available via `application-mysql.yaml`
- Interaction uses enriched 14-field schema (V002 migration)
- REST API follows CTR-API-001 contract pattern
- CustomerJourneyOrchestrator injected as Spring @Service bean

## Verification

- `make check`: PASS
- `./mvnw test`: 101 tests, 0 failures
- FullChainE2eIT: 6 tests PASS (HTTP→H2→full lifecycle)
- PersistenceIntegrationTest: 5 tests PASS (JDBC CRUD + Flyway)
