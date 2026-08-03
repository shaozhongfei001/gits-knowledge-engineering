# GITS-KNO Spec Index

> 权威 spec 索引。以 `specs/CONTRACT_INDEX.yaml` 和各 loop `STATE.json` 为唯一事实来源。
> 未经 active dispatch 授权的 M01-M22 不得实现业务行为。

## 当前 Active Dispatch/Loop

| Dispatch | Packet | Loop | Status | Implementation Actor | 合同关联 |
|---|---|---|---|---|---|
| P5 | P5-CUSTOMER-JOURNEY-SLICE | P5-customer-journey-slice | CLOSED (QA pass) | CTR-API-001, CTR-SEM-001, CTR-SEM-002, CTR-DATA-001, CTR-DATA-002 |

## 已关闭 Dispatch/Loop

| Dispatch | Packet | Loop | Status | QA Actor | 关闭依据 |
|---|---|---|---|---|---|
| P0 | GITS-KNO-DEV-P0 | P0-framework-dryrun | ready_for_independent_qa | pending | 5/5 implementation gates pass；STATE 已同步 |
| P1 | P1-ORACLE-READONLY | P1-oracle-readonly | qa_passed → CLOSED | independent_qa_subagent | EVIDENCE.json 3/3 gates + independent_qa pass |
| P2 | P2-KNOWLEDGE-ENGINEERING-BUILD | P2-knowledge-engineering-build | qa_pass → CLOSED | independent_qa_subagent | EVIDENCE.json independent_qa pass + git commit 4432973 |
| P3 | P3-MAPPING-AND-GAPS | P3-mapping-and-gaps | qa_pass → CLOSED | independent_qa_subagent | EVIDENCE.json independent_qa pass + git commit 0313da2 |
| P4 | P4-CUSTOMER-SEMANTIC-FORMALIZATION | P4-customer-semantic-formalization | qa_pass → CLOSED | independent_qa_agent | EVIDENCE.json independent_qa pass + Baton transferred 2026-08-02 |

## 规划中 Dispatch/Loop

| Dispatch | Loop | Status | 前置条件 |
|---|---|---|---|
| P1 | P1-oracle-readonly | closed (QA pass) | Oracle VM 可达；ADR-0007 已批准 |

## 模块-阶段映射

首期公共支撑: M01, M02, M03, M04, M05, M06, M07
首期纵向主链: M17, M18, M20, M21, M22
后续验证: M08-M16, M19

> 详见 `docs/governance/MODULE_CATALOG.md`

## 合同注册表摘要

共 12 份合同（ENGINEERING_CANDIDATE）：

| ID | Kind | Authority Source |
|---|---|---|
| CTR-API-001 | openapi | specs/openapi/gits-kno-api.openapi.json |
| CTR-EVENT-001 | asyncapi | specs/events/domain-events.asyncapi.json |
| CTR-SEM-001 | linkml_subset | specs/semantic/gits-core.linkml.yaml |
| CTR-SEM-002 | turtle | specs/semantic/gits-core.owl.ttl |
| CTR-RULE-001 | dmn | specs/rules/claim-reconciliation.dmn |
| CTR-SKILL-001 | json_schema | specs/skills/context-assembly.skill.schema.json |
| CTR-ACTION-001 | json_schema | specs/actions/controlled-action.schema.json |
| CTR-DATA-001 | json_schema | specs/data/source-contract.schema.json |
| CTR-DATA-002 | source_contract_instance | specs/data/src-edwcrm-cust-base.v0.1.json |
| CTR-EVIDENCE-001 | json_schema | specs/evidence/evidence-bundle.schema.json |
| CTR-EVAL-001 | json_schema | specs/evaluation/run-manifest.schema.json |
| CTR-MAP-001 | turtle | specs/data/customer-source-mapping.r2rml.ttl |

> 详见 `specs/CONTRACT_INDEX.yaml`

## 显式非声明

CUSTOMER_CONFIRMED=NO | PRODUCTION_READY=NO | REAL_INTERFACE_CONNECTED=NO | REAL_E2E_PASS=NO | QA_PASS=LOOP_SCOPED_ONLY | FROZEN=NO
