# PP-F05｜如果AI直接写CRM，模型错误、推断或敏感内部判断可能变成正式记录

- Scene：`SCENE-P0-26`
- Evidence Level：`E1`
- Event：`EVT-033`
- Page State：`PAGE-15`
- Skill Call：`SCALL-026`
- Human Gate：`HG-F05`
- Acceptance Test：`AT-P0-26`

## 1. 客户经理真实痛点

CRM是正式记录，不能让模型觉得“差不多”就写进去。

## 2. 在华东精工剧情里怎么发生

“设备更新融资线索”只作为OpportunitySignal；RM对“创建正式商机”可EDIT/REJECT。

## 3. 现场数据例子

- 写回命令逐条before/proposed/risk
- 正式Opportunity建议=EDIT成Signal

## 4. 对话

**系统：** 建议：创建正式设备更新融资Opportunity。

**张伟：** 不接受。改成OpportunitySignal，等项目主体和资金路径确认后再升级。

## 5. 系统怎么解决

受控写回：Draft→字段映射→风险分级→Accept/Edit/Reject→WritebackCommand→审计。

### 页面状态

`PAGE-15`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`WRITEBACK_POLICY` → `PS-03`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F05`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

确认后才写CRM

## 7. 不能做什么

Agent不得直接写CRM源表

## 8. 验收

所有写回可审计、有幂等键、有人工决策

## 9. 开发落点

- 前端：加载 `PAGE-15` 对应页面状态。
- 后端：按 `SCALL-026` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-26`，并验证Human Gate `HG-F05` 没有被绕过。
