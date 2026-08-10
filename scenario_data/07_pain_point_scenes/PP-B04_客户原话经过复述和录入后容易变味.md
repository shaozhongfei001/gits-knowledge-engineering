# PP-B04｜客户原话经过复述和录入后容易变味

- Scene：`SCENE-P0-07`
- Evidence Level：`E1`
- Event：`EVT-007`
- Page State：`PAGE-05`
- Skill Call：`SCALL-007`
- Human Gate：`HG-B04`
- Acceptance Test：`AT-P0-07`

## 1. 客户经理真实痛点

客户说“先看看三年左右怎么做”，录进系统可能变成“客户有三年期融资需求”，意思已经不一样。

## 2. 在华东精工剧情里怎么发生

同时保留“希望增加3000万左右支持”的原话和RM/AI解释，直到CFO澄清后再更新Intent。

## 3. 现场数据例子

- 客户原话：‘先看看三年左右怎么做’
- 提炼类型=INTENT
- 不写Verified Fact

## 4. 对话

**张伟：** 客户上次说‘先看看三年左右怎么做’，别替他写成‘三年期融资需求已明确’。

**系统：** 已保留客户原话，提炼结果标为INTENT，状态=PENDING/ACTIVE，不覆盖原文。

## 5. 系统怎么解决

原始表达、RM判断、AI推断和已确认事实分层保存。

### 页面状态

`PAGE-05`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-07` → `RSK-10,RSK-11,RSK-12`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-B04`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

确认后的业务对象

## 7. 不能做什么

AI推断不能直接写客户事实

## 8. 验收

任何提炼结果可回到原文；修改保留版本

## 9. 开发落点

- 前端：加载 `PAGE-05` 对应页面状态。
- 后端：按 `SCALL-007` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-07`，并验证Human Gate `HG-B04` 没有被绕过。
