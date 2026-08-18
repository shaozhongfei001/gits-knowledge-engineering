# PP-A01｜外部行业、政策和企业事件太分散，客户经理很难长期盯住

- Scene：`SCENE-P0-01`
- Evidence Level：`E1`
- Event：`EVT-005`
- Page State：`PAGE-02`
- Skill Call：`SCALL-001`
- Human Gate：`HG-A01`
- Acceptance Test：`AT-P0-01`

## 1. 客户经理真实痛点

平时要靠网站、行业资讯、群消息和别人转发去拼信息，等自己看到时，机会往往已经不新鲜了。

## 2. 在华东精工剧情里怎么发生

设备更新政策出现后，系统主动提示其对高端装备制造客户的可能影响，但明确这只是行业机会线索。

## 3. 现场数据例子

- EXT-004 设备更新政策
- EXT-001 二期项目备案
- FIND-EQP-001 设备付款+32%

## 4. 对话

**张伟：** 最近这类政策和产业消息太多，我不可能每天全盯。

**系统：** 这条设备更新政策与华东精工二期项目、近期设备付款变化同时出现，建议列为高相关Signal；尚未形成客户正式需求。

## 5. 系统怎么解决

持续采集政策、产业、招投标、项目和企业事件，先形成可解释的机会假设，再映射到行内客户。

### 页面状态

`PAGE-02`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-16` → `RSK-27,RSK-04`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-A01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

只写OpportunitySignal/候选客户

## 7. 不能做什么

不能以外部事件直接认定客户真实融资需求

## 8. 验收

机会有来源、时点、影响逻辑；未核实前必须是Signal/Candidate

## 9. 开发落点

- 前端：加载 `PAGE-02` 对应页面状态。
- 后端：按 `SCALL-001` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-01`，并验证Human Gate `HG-A01` 没有被绕过。
