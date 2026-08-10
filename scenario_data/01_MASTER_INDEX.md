# V1.1 MASTER INDEX

## 00_governance
控制包。包含证据来源、No-Go、场景不变量、P0覆盖登记和数据血缘。

## 01_story
完整剧情。既有“张伟的一天”，也有2026-03-12到2026-07-20的多日经营弧线。

## 02_master_data
客户、法人、集团关系、关键人、RM客户池、行业分类和客户行业映射。

## 03_bank_data
账户、日余额、3600条交易、月度关系快照、授信、提款、担保、产品持有、财务报表、应收应付、盈利和分析Finding。

## 04_external_data
160条合成外部事件、实体解析候选和来源许可。

## 05_knowledge
产品知识版本、KYC问题库、拜访SOP、Evidence Policy、Ontology Seed、Skill Registry、知识资产和隐性经验候选。

## 06_interactions
40条历史Interaction、触达记录、107条Gold Standard Utterance、现场笔记、访后口述、Claim Assessment、Commitment、Task和Open Question。

## 07_pain_point_scenes
27个P0详细场景合同 + 12个P1/P2扩展剧情。这里是业务需求与系统实现之间最直接的桥。

## 08_ui_states
18个页面状态。其中PAGE-01~16是核心客户经理闭环；17/18用于主管辅导和AI运营扩展。

## 09_skill_agent
Skill调用、Workflow状态迁移、Agent Trace、Runtime Event Stream、Skill I/O和Eval反馈。

## 10_human_gates
27个P0 Human Gate，以及录音Consent、产品经理/风险经理复核、CRM写回审计。

## 11_outputs
R0-R8、产品适配、专业Fact Pack、CRM写回命令和报告版本链。

## 12_new_evidence
2026-07-10设备清单、付款计划、客户消息、供应商付款接受度和Evidence Metadata。

## 13_api_fixtures
OpenAPI Stub、13组Request/Response、Postman Collection、FastAPI Mock Server。

## 14_database
MySQL 8参考Schema、核心Seed、CSV Load示例和数据模型开发说明。

## 15_tests
37条Acceptance Test、12条Negative Test、数据质量规则、可直接运行Validator和Unittest。

## 16_dev_handoff
给Cursor/Codex/CodeBuddy的开发交接、Master Prompt、构建顺序、场景-页面-Skill-API映射、Demo Runbook。

## 99_legacy_v1.0_reference
V1.0只读参考副本。不得作为V1.1当前SSOT。
