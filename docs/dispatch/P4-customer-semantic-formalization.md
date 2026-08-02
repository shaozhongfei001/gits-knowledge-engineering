# P4｜gits:Customer 语义正式化与 Source Contract 登记派工

| 字段 | 值 |
|---|---|
| packet | `P4-CUSTOMER-SEMANTIC-FORMALIZATION` |
| status | `IN_PROGRESS` |
| baseline | `specs/BASELINE_INDEX.yaml` |
| contract registry | `specs/CONTRACT_INDEX.yaml` |
| implementation actor | `tech_lead` |
| QA actor | `independent_qa` |
| ADR | `ADR-0010` |

## Owner 决策

引入 `gits:Customer` 到语义核心合同（LinkML/OWL），正式登记 EDWCRM 客户 Source Contract，并将 `CTR-MAP-001` 从 `spike_only` 升级为 `versioned_mapping`。Customer **不**进入运营控制面关系库。

## 验收边界

- 合同源先改，再 `make generate && make check`；禁止手改 `generated/`；
- 不读客户行数据；不写回；不自签 QA；
- 不宣称生产就绪或真实接口 E2E。
