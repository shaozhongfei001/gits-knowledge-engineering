# ADR-0009｜gits:Customer Spike 权威源选择（A_ZHCX_CUST_BASE）

状态：`ACCEPTED_SPIKE_ONLY`

## 背景

CTR-MAP-001 原将逻辑表 `AUTHORIZED_CUSTOMER_VIEW` 映射到 `gits:Customer`。只读元数据探查（ADR-0007）证明该对象在 EDWCRM 可访问范围内不存在。候选表 `A_ZHCX_CUST_BASE` 含 `CUSTID`（NOT NULL）及客户主属性列。

Owner / data-mapping-owner 于 2026-08-02 确认：以 `A_ZHCX_CUST_BASE` 为 Spike 权威源；**暂不**把 `gits:Customer` 引入核心运行本体（ADR-0001 双核边界）。

## 决策

1. 更新 `specs/data/customer-source-mapping.r2rml.ttl`：`rr:tableName "A_ZHCX_CUST_BASE"`，主语模板列 `{CUSTID}`。
2. `CTR-MAP-001` 兼容性保持 `spike_only`；Catalog/映射输出仍是候选语义，不得自动升级为业务主本体。
3. 发布符合 `CTR-DATA-001` schema 的 **Source Contract 候选实例**（`docs/architecture/candidates/`），未登记为正式权威合同源。
4. 引入 `gits:Customer` 业务类需另开 ADR + 本体变更 + 专用 loop。

## 后果

- Spike 映射可对准真实表/列定位；仍禁止业务行数据读取与写回。
- 核心 OWL/LinkML 不变；`make generate` 对 R2RML（无 generated 制品）无新增产物。

## 验证

- R2RML 源文件反映 `A_ZHCX_CUST_BASE` / `CUSTID`；
- Source Contract 候选 JSON 通过 schema 结构校验；
- `make check` / 后端测试 / 机制 E2E 全绿；独立 QA 复跑 `make verify`。
