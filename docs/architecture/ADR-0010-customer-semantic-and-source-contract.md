# ADR-0010｜引入 gits:Customer 语义类并正式登记客户 Source Contract

状态：`ACCEPTED`

## 背景

ADR-0009 已确认 `A_ZHCX_CUST_BASE` / `CUSTID` 为客户映射 Spike 源定位，但明确：`gits:Customer` 不在核心语义合同中，`CTR-MAP-001` 保持 `spike_only`，Source Contract 仅为候选。Owner 要求另开 ADR 与专用 loop，完成正式化。

## 决策

1. **语义合同（非运行控制面）**：在 `CTR-SEM-001`（LinkML）与 `CTR-SEM-002`（OWL）中新增领域参考类 `gits:Customer` 及与 Source Contract 对齐的最小属性集。按 ADR-0001，Customer **不属于**关系型运营对象控制面（不新增 `operating_case` 类运营表/权威状态机）。
2. **Source Contract 正式化**：将候选实例提升为权威合同源 `specs/data/src-edwcrm-cust-base.v0.1.json`，登记为 `CTR-DATA-002`（`kind=source_contract_instance`，校验 CTR-DATA-001 schema 后生成只读制品）。
3. **映射升级**：`CTR-MAP-001` 兼容性由 `spike_only` 调整为 `versioned_mapping`；R2RML 继续指向 `A_ZHCX_CUST_BASE` / `{CUSTID}`，并声明依赖 ADR-0010 / CTR-DATA-002。
4. **边界不变**：不授权客户行数据读取、不授权写回、不自称真实接口 E2E / 生产就绪。

## 后果

- `make generate` 将产出含 Customer 的 schema/SHACL，以及正式 Source Contract 生成物；
- 映射与语义类可对齐；运营库仍不承载 Customer 权威事务状态；
- 后续若要把 Customer 写入运营控制面，须另开 ADR（表结构/双时间/权限）。

## 验证

- `make generate && make check` 通过；
- 机制 E2E 断言 `customerClassInCoreOntology=true` 且映射定位为 `A_ZHCX_CUST_BASE`；
- 独立 QA 复跑 `make verify`；memory/evidence-check 通过。
