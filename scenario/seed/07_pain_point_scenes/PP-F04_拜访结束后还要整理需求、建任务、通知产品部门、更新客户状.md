# PP-F04｜拜访结束后还要整理需求、建任务、通知产品部门、更新客户状态，重复录入很重

- Scene：`SCENE-P0-25`
- Evidence Level：`E1`
- Event：`EVT-032`
- Page State：`PAGE-14`
- Skill Call：`SCALL-025`
- Human Gate：`HG-F04`
- Acceptance Test：`AT-P0-25`

## 1. 客户经理真实痛点

一场会回来，要写纪要、填CRM、给产品经理发消息、建待办、提醒下周跟，最耗的是重复整理。

## 2. 在华东精工剧情里怎么发生

会后一次生成R4、R5-A、R5-B、客户承诺、银行Task、交易银行协同Task和风险会商建议。

## 3. 现场数据例子

- R4/R5-A/R5-B+3个专业Fact Pack
- 一次Interaction多出口

## 4. 对话

**张伟：** 一场会别让我再写四遍。

**系统：** 已分别生成R4个人复盘、R5-A内部报告、R5-B CRM短版，以及产品/风险/交易银行Fact Pack。

## 5. 系统怎么解决

一次Interaction形成多出口，但每个输出有不同权限和字段边界。

### 页面状态

`PAGE-14`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-10+SP-15+SP-17` → `RSK-17,RSK-25,RSK-28`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F04`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

通过WritebackCommand

## 7. 不能做什么

内部报告不能全文写CRM；AI不能直接分派高风险任务

## 8. 验收

同一Interaction不重复录入，各输出边界可配置

## 9. 开发落点

- 前端：加载 `PAGE-14` 对应页面状态。
- 后端：按 `SCALL-025` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-25`，并验证Human Gate `HG-F04` 没有被绕过。
