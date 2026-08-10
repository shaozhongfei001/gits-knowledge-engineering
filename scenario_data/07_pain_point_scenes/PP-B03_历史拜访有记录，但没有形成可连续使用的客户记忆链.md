# PP-B03｜历史拜访有记录，但没有形成可连续使用的客户记忆链

- Scene：`SCENE-P0-06`
- Evidence Level：`E1`
- Event：`EVT-007`
- Page State：`PAGE-05`
- Skill Call：`SCALL-006`
- Human Gate：`HG-B03`
- Acceptance Test：`AT-P0-06`

## 1. 客户经理真实痛点

几个月前客户说过什么、承诺过什么、我方答应过什么，经常要翻旧纪要；换客户经理更容易断。

## 2. 在华东精工剧情里怎么发生

3月CFO提过扩产并承诺设备清单；7月访前不再重问“有没有扩产”，而是提示清单未闭环。

## 3. 现场数据例子

- INT-20260312
- 客户设备清单承诺due=2026-03-31
- 状态OVERDUE

## 4. 对话

**系统：** 3月12日客户承诺设备清单出来后提供，原到期3月31日，目前未闭环。

**张伟：** 那今天不再问‘有没有扩产’，直接续问设备清单和项目进展。

## 5. 系统怎么解决

Interaction、Commitment、Task、Need、Concern按时间形成Relationship Memory Projection。

### 页面状态

`PAGE-05`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-02+SP-08+SP-10` → `RSK-05,RSK-13,RSK-17`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-B03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

记忆投影/Task/Commitment

## 7. 不能做什么

不能用摘要覆盖原文；过期表达不能当当前事实

## 8. 验收

新RM能恢复未决事项，R8自动继承

## 9. 开发落点

- 前端：加载 `PAGE-05` 对应页面状态。
- 后端：按 `SCALL-006` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-06`，并验证Human Gate `HG-B03` 没有被绕过。
