# PP-C05｜不同拜访目标、不同受访人，却经常套同一套KYC问题

- Scene：`SCENE-P0-14`
- Evidence Level：`E1`
- Event：`EVT-006`
- Page State：`PAGE-06`
- Skill Call：`SCALL-014`
- Human Gate：`HG-C05`
- Acceptance Test：`AT-P0-14`

## 1. 客户经理真实痛点

问CFO、采购负责人和技术负责人，问题肯定不能一样；融资推进和关系维护也不是一套问法。

## 2. 在华东精工剧情里怎么发生

对CFO问融资结构和项目主体；对采购负责人问供应商和付款节点；对财务总经理问资金安排。

## 3. 现场数据例子

- CFO问题库20条
- 财务总经理20条
- 采购负责人20条
- 子公司财务20条

## 4. 对话

**张伟：** 李总、王总、陈涛不能问同一套问题。

**系统：** CFO：主体/结构/负债偏好；财务总经理：材料/付款；采购：供应商/交期/付款工具。

## 5. 系统怎么解决

问题计划基于Visit Goal + Stakeholder Role + KYC Gap动态组装。

### 页面状态

`PAGE-06`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-04+SP-05+SP-14` → `RSK-07,RSK-09,RSK-22`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C05`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

QuestionPlan

## 7. 不能做什么

不能向无权回答人员问超职责或敏感事项

## 8. 验收

同一客户拜访不同角色生成不同问题集

## 9. 开发落点

- 前端：加载 `PAGE-06` 对应页面状态。
- 后端：按 `SCALL-014` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-14`，并验证Human Gate `HG-C05` 没有被绕过。
