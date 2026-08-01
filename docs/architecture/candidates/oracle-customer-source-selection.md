# Candidate Authoritative Source Selection — gits:Customer (CTR-MAP-001)

> **CANDIDATE PROPOSAL — NOT_A_CONTRACT, CANDIDATE_ONLY, NOT_AUTO_PROMOTED**
>
> Produced by a read-only Oracle metadata spike under ADR-0007. Requires **data-mapping-owner** approval and an explicit contract change to become authoritative. No customer row data was read; read-only transaction enforced.

## 背景

CTR-MAP-001 (`specs/data/customer-source-mapping.r2rml.ttl`, `spike_only`) 把逻辑表 `AUTHORIZED_CUSTOMER_VIEW` 映射到 `gits:Customer`，主语模板 `customer/{CUSTOMER_ID}`。只读元数据探查发现两个合同级缺口：

1. **源对象不存在**：`AUTHORIZED_CUSTOMER_VIEW` 在 EDWCRM 可访问范围内无表、无视图、无同义词（`user_views`/`all_views`/`all_tables`/`all_synonyms` 均无）。含 `CUSTOMER` 的对象也无。R2RML 的源是占位假设。
2. **语义目标不存在**：核心运行本体（CTR-SEM-002, `gits-core.owl.ttl`）只有操作类 `OperatingCase/Interaction/Claim/Evidence/HumanConfirmation/Action/Receipt/Evaluation`，**没有 `gits:Customer`**。按 ADR-0001，运行本体是控制面，业务域实体（Customer）不在核心内。

## 只读发现（证据）

- 全库 212 表 / 5817 列（见 `loops/P1-oracle-readonly/evidence/oracle_metadata_spike_20260801T180322Z.log`，SHA `50f9f9cc…`）。
- 客户域候选表 70+ 张（`A_CUST_*`、`A_ZHCX_CUST_*`、`DM_CSP_*CUST*`、`F_CUST_*` 等）。
- **最强候选 `A_ZHCX_CUST_BASE`**（EDWCRM schema）：`CUSTID VARCHAR2 NOT NULL`（主键候选，契合 `CUSTOMER_ID`），`CUSTNAME`、`CUSTCODE`、`CERTNO`（证件号）、`CUSTTYPE`、`CUSTSTATUS`、`SEX`、`BIRTHDAY`、`AGE`、`ADDRESS`、`EDUCATION`、`ANNUALINCOME`、`REGION`、`BRID`（网点）等客户主属性。
  - 列证据：`loops/P1-oracle-readonly/evidence/oracle_customer_candidate_columns_20260801T181725Z.log`，SHA `8c2367f0…`。

## 候选权威源选择（待 data-mapping-owner 确认）

| 项 | 候选值 |
|---|---|
| sourceContractId | `SRC-EDWCRM-CUST-BASE-v0.1`（候选） |
| system | `EDWCRM` |
| owner | `data_mapping_owner` |
| classification | `SENSITIVE`（客户主数据，待 owner 终定） |
| object.schema | `EDWCRM` |
| object.name | `A_ZHCX_CUST_BASE` |
| object.authority | `AUTHORITATIVE`（候选） |
| 主键/标识 | `CUSTID` → `customer/{CUSTID}` |
| 候选字段映射 | CUSTID→customer id；CUSTNAME→name；CERTNO→cert number；CUSTTYPE→type；CUSTSTATUS→status；REGION/BRID→branch |

## 决策点（需 data-mapping-owner 拍板）

1. **源选择**：是否确认 `A_ZHCX_CUST_BASE`（EDWCRM）为 `gits:Customer` 的候选权威源，替代不存在的 `AUTHORIZED_CUSTOMER_VIEW`？
2. **语义目标**：`gits:Customer` 不在核心运行本体。两条路：
   - (A) 引入 `gits:Customer` 为业务域类（扩展本体，需新 ADR + 本体变更 + `make generate && make check`），随后把 CTR-MAP-001 从 `spike_only` 升为正式映射；
   - (B) 维持 CTR-MAP-001 `spike_only`，本次只记录候选源选择，不引入业务类、不改本体。
3. **分类**：客户主数据分类（建议 `SENSITIVE`，待 owner 终定）。

## 不越界声明

- 本提案是候选 Proposal；在 owner 确认 + 合同变更前，不写回 `specs/`、不改 `generated/`、不自动升级为业务主本体。
- 仅只读元数据；未读客户行数据；`SET TRANSACTION READ ONLY` 强制。
