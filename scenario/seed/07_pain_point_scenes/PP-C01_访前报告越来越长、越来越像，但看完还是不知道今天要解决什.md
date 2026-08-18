# PP-C01｜访前报告越来越长、越来越像，但看完还是不知道今天要解决什么

- Scene：`SCENE-P0-10`
- Evidence Level：`E1`
- Event：`EVT-008`
- Page State：`PAGE-07`
- Skill Call：`SCALL-010`
- Human Gate：`HG-C01`
- Acceptance Test：`AT-P0-10`

## 1. 客户经理真实痛点

企业简介、新闻、财务、产品一堆，看完十几页，真正见客户时还是得自己再想一遍。

## 2. 在华东精工剧情里怎么发生

R1只保留本次拜访目标、关键变化、必问问题、产品准备和风险边界。

## 3. 现场数据例子

- R1首页目标1个
- P0问题7个
- No-Go至少4条

## 4. 对话

**张伟：** 别再给我十几页企业介绍，我14:30就去见客户。

**系统：** 首页只保留本次主目标、7个问题、3个风险边界和双方待确认事项；其余放证据附录。

## 5. 系统怎么解决

围绕Visit Objective生成“一页作战摘要+证据附录”。

### 页面状态

`PAGE-07`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-04+SP-10` → `RSK-07,RSK-17`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

固化报告版本

## 7. 不能做什么

不能用长篇资料堆砌代替准备

## 8. 验收

首页1分钟可读完，关键事实可点回证据

## 9. 开发落点

- 前端：加载 `PAGE-07` 对应页面状态。
- 后端：按 `SCALL-010` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-10`，并验证Human Gate `HG-C01` 没有被绕过。
