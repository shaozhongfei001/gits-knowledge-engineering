# PP-E03｜客户答应什么、银行答应什么，谈着谈着容易漏，责任和截止时间也不清

- Scene：`SCENE-P0-20`
- Evidence Level：`E1`
- Event：`EVT-028`
- Page State：`PAGE-12`
- Skill Call：`SCALL-020`
- Human Gate：`HG-E03`
- Acceptance Test：`AT-P0-20`

## 1. 客户经理真实痛点

“回头把材料发你”“我回去给你方案”，不当场确认，过几天双方理解可能完全不同。

## 2. 在华东精工剧情里怎么发生

离场前确认：客户7月10日给设备清单；银行7月14日给三年期结构；交易银行补付款工具对照。

## 3. 现场数据例子

- COM-CUST-001 due 7/10
- COM-BANK-001 due 7/14
- COM-BANK-002 due 7/14

## 4. 对话

**张伟：** 离开前我再确认：王总7月10日前给设备清单；我行7月14日前出三年期结构；王彬补付款工具。

**李明：** 准确。方案先做结构，不要写得像已经批了。

## 5. 系统怎么解决

自动提取Customer Commitment与Bank Commitment，离场前生成确认卡。

### 页面状态

`PAGE-12`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-04` → `RSK-10,RSK-07`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-E03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

Commitment + Task候选

## 7. 不能做什么

客户意向不能自动写成承诺；Commitment≠Task

## 8. 验收

每条承诺有Owner、Due、来源；Task与Commitment分开

## 9. 开发落点

- 前端：加载 `PAGE-12` 对应页面状态。
- 后端：按 `SCALL-020` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-20`，并验证Human Gate `HG-E03` 没有被绕过。
