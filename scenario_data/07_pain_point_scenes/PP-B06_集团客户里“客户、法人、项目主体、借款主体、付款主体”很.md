# PP-B06｜集团客户里“客户、法人、项目主体、借款主体、付款主体”很容易被当成一个对象

- Scene：`SCENE-P0-09`
- Evidence Level：`E2`
- Event：`EVT-020`
- Page State：`PAGE-04`
- Skill Call：`SCALL-009`
- Human Gate：`HG-B06`
- Acceptance Test：`AT-P0-09`

## 1. 客户经理真实痛点

嘴里说“华东精工”，可能指集团，也可能指项目公司；但授信、付款、项目备案对应主体可能完全不同。

## 2. 在华东精工剧情里怎么发生

外部备案可能指向智能制造公司，授信主体是集团本部；访前必须核实谁借、谁用、谁还、谁担保。

## 3. 现场数据例子

- Borrower=ENT-001
- ProjectEntity candidate=ENT-002
- Entity role mismatch=OPEN

## 4. 对话

**张伟：** 项目备案是智能制造公司，但授信在集团本部。

**系统：** 已拆分ProjectEntity=ENT-002、BorrowerEntity=ENT-001；借/用/还/担保关系仍待确认。

## 5. 系统怎么解决

以Ontology和实体解析明确Party/LegalEntity/ProjectEntity/BankCustomerRelationship等不同对象。

### 页面状态

`PAGE-04`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-02+SP-13` → `RSK-18,RSK-21`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-B06`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

主体关系投影

## 7. 不能做什么

企业简称不能自动等同唯一法人主体

## 8. 验收

正确区分集团本部与项目公司

## 9. 开发落点

- 前端：加载 `PAGE-04` 对应页面状态。
- 后端：按 `SCALL-009` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-09`，并验证Human Gate `HG-B06` 没有被绕过。
