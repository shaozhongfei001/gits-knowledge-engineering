# PP-C04｜KYC材料常常列很多“知道什么”，却没有告诉客户经理“还不知道什么、为什么重要”

- Scene：`SCENE-P0-13`
- Evidence Level：`E1`
- Event：`EVT-006`
- Page State：`PAGE-06`
- Skill Call：`SCALL-013`
- Human Gate：`HG-C04`
- Acceptance Test：`AT-P0-13`

## 1. 客户经理真实痛点

最有价值的不是再给我一份公司介绍，而是告诉我哪些关键问题现在还没答案。

## 2. 在华东精工剧情里怎么发生

华东精工KYC Gap列出项目主体、实际投资、付款节奏、现有额度用途、他行方案、供应商接受度。

## 3. 现场数据例子

- UNKNOWN=项目主体/投资构成/他行结构
- CONFLICTING=3000万vs4000万

## 4. 对话

**系统：** 高优先Gap：项目最终主体、投资构成、付款节奏、现有额度用途、他行结构。

**张伟：** 把‘他行具体价格’降级，我主要问他们走集团还是项目公司路径。

## 5. 系统怎么解决

KYC Gap按KNOWN/PARTIAL/STALE/CONFLICTING/UNKNOWN管理，并把Gap转成问题。

### 页面状态

`PAGE-06`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-05` → `RSK-08,RSK-09`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C04`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

问题清单/访后覆盖度

## 7. 不能做什么

不能为了报告完整度补造信息

## 8. 验收

每个高优先Gap至少有一个角色适配的问题

## 9. 开发落点

- 前端：加载 `PAGE-06` 对应页面状态。
- 后端：按 `SCALL-013` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-13`，并验证Human Gate `HG-C04` 没有被绕过。
