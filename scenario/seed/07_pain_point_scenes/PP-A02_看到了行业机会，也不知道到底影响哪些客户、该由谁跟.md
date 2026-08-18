# PP-A02｜看到了行业机会，也不知道到底影响哪些客户、该由谁跟

- Scene：`SCENE-P0-02`
- Evidence Level：`E1`
- Event：`EVT-005`
- Page State：`PAGE-02`
- Skill Call：`SCALL-002`
- Human Gate：`HG-A02`
- Acceptance Test：`AT-P0-02`

## 1. 客户经理真实痛点

看到“设备更新”“某产业扩产”这种消息不难，难的是把它落到自己手里具体哪几家企业。

## 2. 在华东精工剧情里怎么发生

系统把设备更新政策与华东精工的行业、项目备案、设备付款变化关联，并找到管户RM张伟。

## 3. 现场数据例子

- 实体ENT-002↔外部项目高置信匹配0.98
- BankCustomerRelationship CUST-002
- RM=P-RM-001

## 4. 对话

**陈晓峰：** 这个政策到底落到哪几户？别给我一份行业摘要。

**系统：** 华东精工智能制造公司与项目备案主体高置信匹配；管户人为张伟。另有3户中相关候选，需要人工核实。

## 5. 系统怎么解决

建立行业事件→产业链角色→企业主体→本行客户→管户关系的映射链。

### 页面状态

`PAGE-02`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-16+SP-02` → `RSK-27,RSK-18,RSK-05`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-A02`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

客户候选池/关注清单

## 7. 不能做什么

不能因同名或集团关系不清自动归属RM

## 8. 验收

机会能追到具体法人主体和RM，低置信关系必须待确认

## 9. 开发落点

- 前端：加载 `PAGE-02` 对应页面状态。
- 后端：按 `SCALL-002` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-02`，并验证Human Gate `HG-A02` 没有被绕过。
