# AI工程协作宪法

## 权威顺序

1. 已批准的杭州银行需求、Owner决策与变更记录；
2. `specs/BASELINE_INDEX.yaml`列出的受控输入；
3. 已批准ADR与 `specs/CONTRACT_INDEX.yaml`；
4. 当前Loop与dispatch；
5. 实现代码与聊天记录。

低权威来源不得反向覆盖高权威来源。旧项目业务结论只能作为参考或假设，不能静默进入本项目。

## 开工红线

1. 先确认Baton；不是holder且不在`parallel_allowed`时停止。
2. 合同变化先改合同源，再运行 `make generate && make check`，然后才写实现。
3. 生成物只读，禁止手工编辑 `generated/`。
4. 不发明字段、状态、权限、接口或业务责任。
5. AI输出只能是候选Claim/Proposal；不得绕过人工确认直接形成正式事实或写回。
6. 禁止 `git add .`；提交必须按显式路径。
7. 失败先留证据再修，开发不得自签QA/E2E/业务验收。

## 合同类型

OpenAPI不是唯一合同。事件、语义、Shape、OWL、规则、数据映射、Skill、Action、Evidence、Run Manifest和评测均属于受控合同；唯一入口是 `specs/CONTRACT_INDEX.yaml`。

## 直接传递边界

领域API返回稳定的业务语义和展示中立数据。ECharts option等厂商展示对象只能存在于Experience/BFF或前端适配层，不得渗入核心领域合同。

## 收工协议

更新HANDOFF、SHARED_MEMORY、ROLE_BOARD、NEXT_SESSION、STATE、证据与dispatch状态；运行 `make memory-check LOOP=<id>` 和 `make evidence-check LOOP=<id>`。未落盘的结论视为不存在。
