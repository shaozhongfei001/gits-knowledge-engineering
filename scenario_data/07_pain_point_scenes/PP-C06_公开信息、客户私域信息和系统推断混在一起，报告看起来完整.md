# PP-C06｜公开信息、客户私域信息和系统推断混在一起，报告看起来完整但不够可信

- Scene：`SCENE-P0-15`
- Evidence Level：`E1`
- Event：`EVT-008`
- Page State：`PAGE-07`
- Skill Call：`SCALL-015`
- Human Gate：`HG-C06`
- Acceptance Test：`AT-P0-15`

## 1. 客户经理真实痛点

外部备案、客户口头说法、银行系统事实如果不分开，很容易误以为都是已确认事实。

## 2. 在华东精工剧情里怎么发生

4800万备案标为SIGNAL；4000万可用额标为FACT；3000万口头表达标为INTENT/待核实。

## 3. 现场数据例子

- FACT=4000万可用额
- SIGNAL=4800万备案
- INTENT=3000万口头表达

## 4. 对话

**系统：** 4800万=外部SIGNAL；4000万可用额度=银行FACT；3000万=客户INTENT待核实。

**张伟：** 这样我现场不会把三种口径当成同一类事实。

## 5. 系统怎么解决

报告强制区分FACT/CLAIM/FINDING/SIGNAL/PENDING，并显示来源、时点和限制。

### 页面状态

`PAGE-07`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-07+SP-10` → `RSK-12,RSK-17`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C06`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

确认后的报告版本

## 7. 不能做什么

无引用重大事实不能进入正式输出

## 8. 验收

关键结论可追源，缺证信息明确标识

## 9. 开发落点

- 前端：加载 `PAGE-07` 对应页面状态。
- 后端：按 `SCALL-015` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-15`，并验证Human Gate `HG-C06` 没有被绕过。
