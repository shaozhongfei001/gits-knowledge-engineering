# PP-D02｜客户讲的是业务难题，银行讲的是产品术语，两边语言对不上

- Scene：`SCENE-P0-17`
- Evidence Level：`E1`
- Event：`EVT-009`
- Page State：`PAGE-08`
- Skill Call：`SCALL-017`
- Human Gate：`HG-D02`
- Acceptance Test：`AT-P0-17`

## 1. 客户经理真实痛点

客户说“付款压力大、期限不合适、成本高”，客户经理得自己翻译成可能的银行产品。

## 2. 在华东精工剧情里怎么发生

“先把设备款安排起来、别搞太复杂”被映射为付款节奏、融资期限、供应商接受度等产品能力。

## 3. 现场数据例子

- 客户语言‘设备款安排’→Need/Concern→ProductCapability
- 不直接等于产品名

## 4. 对话

**王强：** 我们就是想先把第一阶段设备款安排起来，别搞太复杂。

**张伟：** 我先把这个理解成资金安排问题，不直接对应某一个产品，后面按主体、用途和期限拆。

## 5. 系统怎么解决

建立客户语言→Need/Concern→Product Capability→Product Candidate语义桥。

### 页面状态

`PAGE-08`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-15` → `RSK-10,RSK-24`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-D02`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

Need/Concern/ProductFit候选

## 7. 不能做什么

客户抱怨一句不能自动推荐某产品

## 8. 验收

输出解释为什么推荐并保留客户原话

## 9. 开发落点

- 前端：加载 `PAGE-08` 对应页面状态。
- 后端：按 `SCALL-017` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-17`，并验证Human Gate `HG-D02` 没有被绕过。
