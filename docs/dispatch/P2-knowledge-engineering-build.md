# P2｜知识工程合同忠实构建派工

| 字段 | 值 |
|---|---|
| packet | `P2-KNOWLEDGE-ENGINEERING-BUILD` |
| status | `IN_PROGRESS` |
| baseline | `specs/BASELINE_INDEX.yaml` |
| contract registry | `specs/CONTRACT_INDEX.yaml` |
| implementation actor | `tech_lead`（经共享记忆派工多智能体） |
| QA actor | `independent_qa`（独立 actor，复跑门禁） |

## 客户可感知目标

工程从「机制 dry-run 候选」推进到「合同忠实实现 + 机制级真实 Agent E2E + 独立 QA 通过」的可交付候选包；多智能体经共享记忆派工与验收，逐波留证、fail-closed。

## 验收边界

- 实现必须忠实于已注册合同（`specs/CONTRACT_INDEX.yaml`），不发明字段/状态/权限/接口；
- 生成物只读；合同变更先改源再 `make generate && make check`；
- AI 输出仅候选 Claim/Proposal；开发不得自签 QA/E2E/业务验收；
- Oracle 仅只读元数据探查（ADR-0007）；AIOS/CRM/IAM/写回不在范围；
- 禁止 `git add .`；失败先留证再修。

## 波次

- W0 包名统一（done, `5d2b5bb`）
- W1 operational-ontology 核对
- W2 叶模块实现（semantic-runtime / context-evidence / human-action / evaluation，并行）
- W3 适配器（semantic-jena / persistence-relational，并行）
- W4 apps 合同接线（api/OpenAPI + worker/AsyncAPI）
- W5 语义/规则运行时门禁（SHACL/DMN/LinkML）
- W6 Oracle 只读映射 Spike（CTR-MAP-001）
- W7 机制级 Agent E2E（mock 外部）+ run-manifest
- W8 独立 QA + 发关门
