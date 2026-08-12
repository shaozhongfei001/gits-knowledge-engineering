# PP-F06｜一场拜访做完就结束，过几周再见客户又从头翻资料

- Scene：`SCENE-P0-27`
- Evidence Level：`E2`
- Event：`EVT-036`
- Page State：`PAGE-16`
- Skill Call：`SCALL-027`
- Human Gate：`HG-F06`
- Acceptance Test：`AT-P0-27`

## 1. 客户经理真实痛点

持续经营最怕“每次都是第一次见客户”。新材料来了，之前判断应该跟着变。

## 2. 在华东精工剧情里怎么发生

7月10日设备清单到达后，R7修正项目金额/主体/付款节奏判断；下一次R8自动继承，不再问项目是否存在。

## 3. 现场数据例子

- 设备清单3280万
- 备案4800万
- 付款30/40/30
- R7→R8版本继承

## 4. 对话

**系统：** 7月10日新设备清单：3280万，抬头智能制造公司；与4800万备案属于不同口径。

**张伟：** 更新R7，但保留原判断版本；R8带上剩余投资构成和项目主体正式确认两个问题。

## 5. 系统怎么解决

Evidence Ingestion→Claim Assessment→R7更新→Open Question/Task滚入下一R8。

### 页面状态

`PAGE-16`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-07+SP-08+SP-10` → `RSK-12,RSK-13,RSK-17`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F06`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

R7/R8版本、Task状态

## 7. 不能做什么

新证据不能覆盖历史且不留版本

## 8. 验收

R8明确继承R7和新Evidence，历史判断可回放

## 9. 开发落点

- 前端：加载 `PAGE-16` 对应页面状态。
- 后端：按 `SCALL-027` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-27`，并验证Human Gate `HG-F06` 没有被绕过。
