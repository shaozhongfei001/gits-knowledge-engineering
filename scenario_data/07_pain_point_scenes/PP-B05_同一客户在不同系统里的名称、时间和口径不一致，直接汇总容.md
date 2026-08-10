# PP-B05｜同一客户在不同系统里的名称、时间和口径不一致，直接汇总容易错

- Scene：`SCENE-P0-08`
- Evidence Level：`E1`
- Event：`EVT-022`
- Page State：`PAGE-11`
- Skill Call：`SCALL-008`
- Human Gate：`HG-B05`
- Acceptance Test：`AT-P0-08`

## 1. 客户经理真实痛点

一个地方看到4000万可用额度，客户又说要3000万支持，不理清口径就容易把两件事硬拼成一个结论。

## 2. 在华东精工剧情里怎么发生

把4000万可用额度与“3000万支持”并列，生成新增额度/提款/用途调整/项目公司融资四类核实问题。

## 3. 现场数据例子

- FAC-001 available=4000万
- 客户表达=3000万支持
- 最终解释=第一阶段设备款资金安排

## 4. 对话

**系统：** 冲突提示：FAC-001账面可用4000万；客户表达可能涉及3000万支持。

**张伟：** 先不算新增需求，我到现场把额度、提款、用途和项目公司四种情况问清。

## 5. 系统怎么解决

Fact Reconciliation Gate：先做实体、时点、口径和Evidence对账，再生成业务结论。

### 页面状态

`PAGE-11`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-07+SP-13` → `RSK-12,RSK-21`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-B05`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

未确认前仅写Pending/Claim

## 7. 不能做什么

模型不能静默选一个值

## 8. 验收

关键冲突并列展示，并可追到源和时点

## 9. 开发落点

- 前端：加载 `PAGE-11` 对应页面状态。
- 后端：按 `SCALL-008` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-08`，并验证Human Gate `HG-B05` 没有被绕过。
