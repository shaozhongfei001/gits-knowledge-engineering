# ADR-0003: MySQL as Production Relational Database

## Status

Accepted

## Context

The GITS Knowledge Engineering system requires a production-grade relational database. The current situation:

- **H2** is used for development and testing, but is not suitable for production workloads (in-memory, no persistence guarantees, single-connection model).
- **Oracle** is used as a read-only source system for CRM data ingestion (see ADR-0007). It is not available as the operational database.
- **MySQL 8.x** is already partially configured in `application-mysql.yaml` and is the standard relational database in the target deployment environment.
- Flyway is used for schema migration and must work consistently across H2 (dev/test) and MySQL (production).
- Connection pooling is needed for production concurrency; HikariCP is the Spring Boot default.

## Decision

Use **MySQL 8.x** as the production relational database for the GITS Knowledge Engineering system.

## Consequences

### Positive

- **Production-ready**: MySQL 8.x provides ACID compliance, crash recovery, and proven scalability.
- **Already configured**: `application-mysql.yaml` exists with MySQL connection settings; minimal additional setup required.
- **H2 compatibility**: H2's MySQL-compatibility mode (`MODE=MySQL`) allows the same Flyway migrations to run in dev/test and production, reducing drift.
- **HikariCP integration**: Spring Boot's default connection pool (HikariCP) provides production-grade pooling with leak detection, timeout management, and metrics.
- **Environment variable configuration**: All sensitive credentials (host, port, database name, username, password) are externalized via environment variables, supporting 12-factor app principles.

### Negative

- **Migration compatibility burden**: Flyway migrations must be syntactically compatible with both MySQL and H2's MySQL-compatibility mode. MySQL-specific features (e.g., `FULLTEXT` indexes, stored procedures) require H2-compatible alternatives or separate migration scripts.
- **H2 divergence risk**: H2's MySQL-compatibility mode does not cover all MySQL behaviors (e.g., certain data types, locking semantics). Integration tests may not catch MySQL-specific issues.
- **Operational dependency**: Production requires MySQL infrastructure (server, backups, monitoring, credential rotation).

### Mitigations

- Maintain separate `db/migration/h2/` scripts for H2-incompatible migrations.
- Run full integration tests against MySQL in CI before release.
- Use HikariCP leak detection threshold to catch connection leaks early.
- Production credentials are injected via environment variables, never committed to source.
