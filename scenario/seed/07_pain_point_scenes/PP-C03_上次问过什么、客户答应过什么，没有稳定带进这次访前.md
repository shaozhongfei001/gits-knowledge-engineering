# PP-C03｜上次问过什么、客户答应过什么，没有稳定带进这次访前

- Scene：`SCENE-P0-12`
- Evidence Level：`E1`
- Event：`EVT-007`
- Page State：`PAGE-05`
- Skill Call：`SCALL-012`
- Human Gate：`HG-C03`
- Acceptance Test：`AT-P0-12`

## 1. 客户经理真实痛点

客户最反感重复问。明明三个月前说过设备清单，忙一阵又重新问“有没有扩产计划”。

## 2. 在华东精工剧情里怎么发生

R1提示3月设备清单承诺尚未提供，问题改成“上次提到的设备清单现在是否已有初稿？”

## 3. 现场数据例子

- 3月设备清单承诺
- 7月访前自动续接
- 不重复问‘是否扩产’

## 4. 对话

**张伟：** 上次设备清单客户已经答应过，今天要接着问，不能装作第一次。

**系统：** 已把3月承诺带入Question Plan，并标记OVERDUE。

## 5. 系统怎么解决

访前Context Assembly强制装配历史Interaction、未解决问题、Commitment和Task。

### 页面状态

`PAGE-05`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-02+SP-08+SP-04` → `RSK-05,RSK-13,RSK-07`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

本次问题计划

## 7. 不能做什么

已确认事实不重复问；过期承诺要标时点

## 8. 验收

R8继承R7未决事项并避免重复问题

## 9. 开发落点

- 前端：加载 `PAGE-05` 对应页面状态。
- 后端：按 `SCALL-012` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-12`，并验证Human Gate `HG-C03` 没有被绕过。
