# Candidate Source Catalog — Oracle EDwCRM (CTR-MAP-001)

> **CANDIDATE PROPOSAL — NOT_A_CONTRACT, CANDIDATE_ONLY, NOT_AUTO_PROMOTED**
>
> This document is a candidate source catalog produced by a read-only metadata
> spike. It is **not** an authoritative Source Contract. It does **not** modify
> `CTR-MAP-001` or any business ontology. It requires data-owner approval and an
> explicit contract change (per `specs/CONTRACT_INDEX.yaml` → `CTR-MAP-001`,
> `spike_only`) to become authoritative. AI-generated facts remain candidate
> Claims/Proposals until the required human-control path completes (see
> `AGENTS.md`).

| Field | Value |
|---|---|
| Contract | `CTR-MAP-001` (`specs/data/customer-source-mapping.r2rml.ttl`, `spike_only`) |
| ADR | [ADR-0007](../ADR-0007-oracle-readonly-enablement.md) — `ACCEPTED_WITH_READ_ONLY_BOUNDARY` |
| Loop | `P1-oracle-readonly` (wave W6) |
| Actor | `impl_oracle` (implementation, READ-ONLY) |
| Source system | Oracle EDwCRM (`oracle-vm:1521/ACRM`, account `edwcrm`) |
| Evidence | `loops/P1-oracle-readonly/evidence/oracle_metadata_spike_20260801T180322Z.log` |
| Evidence SHA-256 | `50f9f9cc5dbeef458f3a67469f3c5fd7e3cc58e763b2e42f7ba57ecba2d99a68` |
| Discovered tables | **212** (from `user_tables`) |
| Discovered columns | **5817** (from `user_tab_columns`) |

## Governance constraints honored

- **READ-ONLY**: every `sqlplus` session began with `SET TRANSACTION READ ONLY`;
  Oracle confirmed with `Transaction set.`. The spike script
  (`scripts/db/oracle_metadata_spike.sh`) is fail-closed: any error (including
  rejection of the read-only transaction) exits non-zero and closes the
  connection.
- **METADATA ONLY**: only data-dictionary views were queried (`user_tables`,
  `user_tab_columns`). No `SELECT` was issued against any business/data table.
  No customer row data was read or stored.
- **Source capture policy** (`ADR-0007` → `HASH_AND_LOCATOR_BY_DEFAULT`): only
  schema locators (schema/table/column names, data types, nullability) are
  recorded below. No row contents.
- **Credential handling**: credentials sourced from out-of-repo
  `GITS_ORACLE_*` env vars; `sqlplus -S /nolog` + `CONNECT` (credential on
  stdin, not argv); password never printed.

## Discovered source schema (candidate catalog)

The following is a **candidate** source catalog for `CTR-MAP-001`. Table and
column names are locators only; semantic assignment to the business ontology
(e.g. `gits:Customer`) is **not** performed here and remains a candidate mapping
decision for the data mapping owner.

### Tables (212 — first 30 shown as a representative sample)

```
ACRM_T_EVENT_DETAIL
A_CUST_ANALYSYS_IFO
A_CUST_ASSACTOR_RELA
A_CUST_SMALL_CAP_LOAN
A_LPERIOD_CONDITION
A_LPERIOD_TMP
A_ZHCX_CUST_BASE
A_ZHCX_CUST_BASE_TMP
A_ZHCX_CUST_EXP
A_ZHCX_CUST_EXP_HIS
A_ZHCX_CUST_EXP_TMP
A_ZHCX_GROUP_BASE
A_ZHCX_GROUP_EXP
A_ZHCX_GROUP_EXP_HIS
C01_CD_ACCT
C01_CRDT_AGT
C01_DEPOSIT_ACCT
CUPD_CCSU
D03_APP_DWCPXY
D03_CUST_CARD_INFO
... (192 more — full list in evidence file)
```

### Columns (5817 total — sample rows shown)

Schema locators: `table_name, column_name, data_type, nullable`.

```
ACRM_T_EVENT_DETAIL   EVENTSEQ     VARCHAR2      Y
ACRM_T_EVENT_DETAIL   JOBID        VARCHAR2      Y
ACRM_T_EVENT_DETAIL   SP_NAME      VARCHAR2      Y
ACRM_T_EVENT_DETAIL   INSERTTIME   TIMESTAMP(6)  Y
A_CUST_ANALYSYS_IFO   CUSTID       VARCHAR2      N
... (full column catalog in evidence file)
```

## Relevance to CTR-MAP-001

`CTR-MAP-001` (`specs/data/customer-source-mapping.r2rml.ttl`) currently names a
single candidate logical table `AUTHORIZED_CUSTOMER_VIEW` with a placeholder
subject template on `CUSTOMER_ID`, explicitly marked `SPIKE_ONLY` pending data
owner approval and a real Source Contract.

This spike confirms the **metadata surface** available for that mapping:

- No view literally named `AUTHORIZED_CUSTOMER_VIEW` was found in
  `user_tables` (the R2RML `tableName` is a candidate target, not a confirmed
  object). A separate `user_views` enumeration (follow-up) would be required to
  confirm whether it exists as a view.
- Several candidate customer-bearing base tables are present as locators, e.g.
  `A_ZHCX_CUST_BASE`, `A_CUST_ANALYSYS_IFO`, `C01_CRDT_AGT`, `D03_CUST_CARD_INFO`.
  Which (if any) is the authoritative source for `gits:Customer` is a **mapping
  decision for the data mapping owner**, not established by this spike.

## What this artifact is NOT

- **NOT a contract change.** `specs/CONTRACT_INDEX.yaml` and
  `specs/data/customer-source-mapping.r2rml.ttl` are untouched.
- **NOT auto-promoted to the business ontology.** No `gits:` class/property
  assignment is made.
- **NOT a customer data dump.** No row data was read; only schema locators.
- **NOT a DBA/SECURITY_OWNER sign-off.** ADR-0007 records those as pending.

## Follow-ups (candidate, not committed)

- Enumerate `user_views` to confirm/deny `AUTHORIZED_CUSTOMER_VIEW` as a view.
- Hash the catalog (table/column names) for tamper-evidence in the evidence
  bundle, if a Source Contract is later proposed.
- Data mapping owner to select the authoritative source table(s) and propose a
  contract change to `CTR-MAP-001` (out of `spike_only`).
