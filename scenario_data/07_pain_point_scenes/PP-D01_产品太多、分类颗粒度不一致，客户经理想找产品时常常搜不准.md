# PP-D01｜产品太多、分类颗粒度不一致，客户经理想找产品时常常搜不准

- Scene：`SCENE-P0-16`
- Evidence Level：`E1`
- Event：`EVT-009`
- Page State：`PAGE-08`
- Skill Call：`SCALL-016`
- Human Gate：`HG-D01`
- Acceptance Test：`AT-P0-16`

## 1. 客户经理真实痛点

客户说的是“设备款怎么安排”“资金别占太多”，系统里却是各种产品名称和多级分类，搜索经常对不上。

## 2. 在华东精工剧情里怎么发生

张伟输入“第一阶段设备款+三年期”，系统匹配设备更新融资、用途边界、供应商付款工具。

## 3. 现场数据例子

- 8类产品知识卡
- 主候选PROD-FAL V2.2
- 流贷=谨慎候选

## 4. 对话

**张伟：** 客户说‘设备款怎么安排’，系统别只搜产品名字。

**系统：** 匹配到设备更新/固定资产融资、流贷用途边界、供应链付款工具三类产品能力。

## 5. 系统怎么解决

建立Product Knowledge Card和客户问题→产品能力的语义映射。

### 页面状态

`PAGE-08`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-15` → `RSK-24,RSK-25`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-D01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

ProductFitAssessment

## 7. 不能做什么

不能只按关键词返回产品目录；不能给正式额度/价格承诺

## 8. 验收

客户自然语言可映射产品能力并解释理由

## 9. 开发落点

- 前端：加载 `PAGE-08` 对应页面状态。
- 后端：按 `SCALL-016` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-16`，并验证Human Gate `HG-D01` 没有被绕过。
