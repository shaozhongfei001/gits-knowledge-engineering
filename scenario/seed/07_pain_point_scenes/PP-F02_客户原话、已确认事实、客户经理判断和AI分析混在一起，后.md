# PP-F02｜客户原话、已确认事实、客户经理判断和AI分析混在一起，后续的人不知道该信哪层

- Scene：`SCENE-P0-23`
- Evidence Level：`E1`
- Event：`EVT-031`
- Page State：`PAGE-13`
- Skill Call：`SCALL-023`
- Human Gate：`HG-F02`
- Acceptance Test：`AT-P0-23`

## 1. 客户经理真实痛点

“客户可能愿意做供应链”到底是客户明确说的，还是客户经理自己的判断，必须看得出来。

## 2. 在华东精工剧情里怎么发生

“如果能减少短期资金占用，可以看看”保存为客户表达；“供应链机会增强”保存为Finding/Signal。

## 3. 现场数据例子

- Original Claim vs AI Finding分离
- crm_fact_allowed字段

## 4. 对话

**张伟：** 客户说‘可以看看供应链’，这只是表达，不是已经有供应链融资需求。

**系统：** 已标记Original Claim；‘供应链机会增强’单独作为AI Finding/Signal。

## 5. 系统怎么解决

访后语义分层：Original Claim / Verified Fact / RM Judgment / AI Finding / Pending。

### 页面状态

`PAGE-13`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-07` → `RSK-11,RSK-12`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F02`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

仅确认字段进入CRM事实区

## 7. 不能做什么

推断不能写入事实字段；原始表达不能删除

## 8. 验收

关键项都能回答谁说的、什么时候、依据是什么

## 9. 开发落点

- 前端：加载 `PAGE-13` 对应页面状态。
- 后端：按 `SCALL-023` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-23`，并验证Human Gate `HG-F02` 没有被绕过。
