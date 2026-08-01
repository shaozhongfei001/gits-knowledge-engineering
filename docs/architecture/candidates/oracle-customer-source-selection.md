# Candidate Authoritative Source Selection — gits:Customer (CTR-MAP-001)

> **OWNER DECISION RECORDED (2026-08-02)** — data-mapping-owner confirmed
> `A_ZHCX_CUST_BASE` as the spike authoritative source. Ontology path: keep
> `CTR-MAP-001` as `spike_only`; do **not** introduce `gits:Customer` into the
> core operational ontology. See ADR-0009.
>
> Source Contract candidate instance:
> `docs/architecture/candidates/src-edwcrm-cust-base.v0.1.candidate.json`
> (schema-shaped; **not** registered in `CONTRACT_INDEX`).

## 决策摘要

| 项 | 值 |
|---|---|
| logicalTable | `A_ZHCX_CUST_BASE` |
| subject column | `CUSTID` |
| R2RML | `specs/data/customer-source-mapping.r2rml.ttl` (updated) |
| core ontology | `gits:Customer` **not** added |
| classification | `SENSITIVE` (candidate) |

## 证据

- Metadata spike: `loops/P1-oracle-readonly/evidence/oracle_metadata_spike_20260801T180322Z.log`
- Columns: `loops/P1-oracle-readonly/evidence/oracle_customer_candidate_columns_20260801T181725Z.log` (SHA `8c2367f0…`)
- No customer row data was read; `SET TRANSACTION READ ONLY` enforced.
