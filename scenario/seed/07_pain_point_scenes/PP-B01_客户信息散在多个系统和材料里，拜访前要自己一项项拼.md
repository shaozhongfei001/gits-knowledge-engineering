# PP-B01｜客户信息散在多个系统和材料里，拜访前要自己一项项拼

- Scene：`SCENE-P0-04`
- Evidence Level：`E1`
- Event：`EVT-003`
- Page State：`PAGE-03`
- Skill Call：`SCALL-004`
- Human Gate：`HG-B01`
- Acceptance Test：`AT-P0-04`

## 1. 客户经理真实痛点

CRM看客户、授信系统看额度、交易平台看流水、外部工具看工商和项目，历史拜访又在另一处。

## 2. 在华东精工剧情里怎么发生

华东精工客户页一次装配集团关系、授信、交易变化、项目、历史Interaction和产品空白。

## 3. 现场数据例子

- 24户RM客户组合
- 1512条日余额
- 3600条本行可见交易
- 40条历史Interaction

## 4. 对话

**张伟：** 以前准备一次拜访，我得开CRM、授信、交易、外部信息好几个入口。

**系统：** 已在客户经营视图按权限装配主体、授信、交易变化、历史Interaction和外部Signal。

## 5. 系统怎么解决

以权限为前提装配Customer Operating View，而不是再造一套客户主数据。

### 页面状态

`PAGE-03`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-02` → `RSK-05,RSK-18`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-B01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

经营投影/人工补充

## 7. 不能做什么

不能形成绕过原系统权限的新数据通道

## 8. 验收

一处可追溯到源系统和时点，缺数据明确显示Unavailable

## 9. 开发落点

- 前端：加载 `PAGE-03` 对应页面状态。
- 后端：按 `SCALL-004` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-04`，并验证Human Gate `HG-B01` 没有被绕过。
