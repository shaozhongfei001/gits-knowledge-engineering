# P6 ORCHESTRATOR Notes

## Strategy

P6 is implementation-focused: all T1-T10 code was written in a single session. Gates are verification-only.

## Key Decisions

- Use H2 in-memory database (no external DB dependency for dev).
- MySQL profile available via `application-mysql.yaml`.
- Flyway V002 migration extends Interaction table to enriched 14-field schema.
- Spring Boot REST API exposes CustomerJourney, Interaction, Claim endpoints.
- Full-chain E2E test validates HTTP → H2 → M17→M22 complete lifecycle.
