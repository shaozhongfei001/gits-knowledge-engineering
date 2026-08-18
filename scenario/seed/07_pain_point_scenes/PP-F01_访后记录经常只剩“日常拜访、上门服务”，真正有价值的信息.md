# PP-F01｜访后记录经常只剩“日常拜访、上门服务”，真正有价值的信息没带回来

- Scene：`SCENE-P0-22`
- Evidence Level：`E1`
- Event：`EVT-031`
- Page State：`PAGE-13`
- Skill Call：`SCALL-022`
- Human Gate：`HG-F01`
- Acceptance Test：`AT-P0-22`

## 1. 客户经理真实痛点

CRM里有一条拜访记录，不代表银行真正知道客户发生了什么。

## 2. 在华东精工剧情里怎么发生

华东精工访后不写一句“拜访CFO”，而是提取项目主体、3000万澄清、客户顾虑、双方承诺和机会信号。

## 3. 现场数据例子

- 10个Interaction业务对象
- 5个Task/OpenQuestion持续维护

## 4. 对话

**系统：** 从现场笔记和访后口述提取：项目主体、3000万澄清、客户顾虑、承诺、Task、3个OpportunitySignal。

**张伟：** 先给我看对象，不要只生成一段拜访总结。

## 5. 系统怎么解决

访后将记录转为Interaction业务对象，而不是只生成文字摘要。

### 页面状态

`PAGE-13`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-10` → `RSK-10,RSK-11,RSK-17`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

R5-B/客户记忆受控写回

## 7. 不能做什么

不能只用一段LLM摘要替代结构化对象

## 8. 验收

R5-B包含关键经营对象且可追原始记录

## 9. 开发落点

- 前端：加载 `PAGE-13` 对应页面状态。
- 后端：按 `SCALL-022` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-22`，并验证Human Gate `HG-F01` 没有被绕过。
