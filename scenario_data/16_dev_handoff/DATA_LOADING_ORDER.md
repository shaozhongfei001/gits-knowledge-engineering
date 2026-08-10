# 数据加载顺序

1. `00_governance/`：No-Go、证据策略、P0覆盖合同；
2. `02_master_data/`：客户、法人、关系、RM客户池；
3. `03_bank_data/`：账户、余额、交易、授信、产品持有、财务数据；
4. `04_external_data/`：外部事件和实体解析；
5. `05_knowledge/`：产品规则、KYC问题、Ontology、Skill Registry；
6. `06_interactions/`：历史Interaction、现场笔记、访后口述、业务对象；
7. `08_ui_states/`：页面状态；
8. `09_skill_agent/`：Skill/Agent/Workflow回放；
9. `10_human_gates/`：人工决策；
10. `11_outputs/`：R0-R8及CRM写回建议；
11. `12_new_evidence/`：7/10新材料；
12. `15_tests/`：验收和负向测试。

开发时不要以`99_legacy_v1.0_reference/`为当前SSOT，它只是V1.0审计参考。
