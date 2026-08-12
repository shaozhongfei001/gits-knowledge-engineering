# PP-A03｜机会信号、客户真实需求和正式商机容易被混成一件事

- Scene：`SCENE-P0-03`
- Evidence Level：`E1`
- Event：`EVT-005`
- Page State：`PAGE-02`
- Skill Call：`SCALL-003`
- Human Gate：`HG-A03`
- Acceptance Test：`AT-P0-03`

## 1. 客户经理真实痛点

新闻里说客户扩产，不代表客户一定要融资；客户随口说“看看方案”，也不等于已经形成商机。

## 2. 在华东精工剧情里怎么发生

二期项目备案只作为Signal；客户说“增加3000万支持”也不直接创建正式Opportunity。

## 3. 现场数据例子

- Signal=EXT-001
- Customer confirmation=缺失
- Opportunity status=NOT_CREATED

## 4. 对话

**张伟：** 外部都备案了，要不要直接建个商机？

**系统：** 不建议。当前满足OpportunitySignal，不满足正式Opportunity；仍缺客户确认和资金安排语义。

## 5. 系统怎么解决

建立Signal→机会假设→RM核实→商机候选→正式商机的受控状态机。

### 页面状态

`PAGE-02`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-01+SP-07` → `RSK-03,RSK-12`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-A03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

Signal可写候选区；Opportunity须确认

## 7. 不能做什么

AI不得把新闻或口头表达自动升级为正式商机

## 8. 验收

状态差异、升级条件和证据清楚可见

## 9. 开发落点

- 前端：加载 `PAGE-02` 对应页面状态。
- 后端：按 `SCALL-003` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-03`，并验证Human Gate `HG-A03` 没有被绕过。
