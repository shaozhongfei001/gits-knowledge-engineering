# PP-E01｜客户现场表达常常很含糊，一顺着听就容易把“想法”记成“正式需求”

- Scene：`SCENE-P0-19`
- Evidence Level：`E2`
- Event：`EVT-022`
- Page State：`PAGE-11`
- Skill Call：`SCALL-019`
- Human Gate：`HG-E01`
- Acceptance Test：`AT-P0-19`

## 1. 客户经理真实痛点

客户说“希望你们增加3000万支持”，听上去很明确，其实新增额度还是设备款安排完全是两回事。

## 2. 在华东精工剧情里怎么发生

张伟不接受“3000万=新增授信”，现场追问新增额度、现有额度提款、用途调整还是项目公司融资。

## 3. 现场数据例子

- 14:47客户‘增加3000万’
- 14:48追问四种语义
- 14:49客户澄清

## 4. 对话

**王强：** 我们希望银行这边能增加3000万左右支持。

**张伟：** 我确认一下：新增额度、现有额度提款、用途调整，还是项目公司单独融资？

## 5. 系统怎么解决

Interaction抽取Claim，并触发语义歧义和Fact Reconciliation。

### 页面状态

`PAGE-11`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-07+SP-13` → `RSK-10,RSK-11,RSK-12,RSK-21`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-E01`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

Claim/Pending，不自动Opportunity

## 7. 不能做什么

客户一句话不能自动形成正式需求或商机

## 8. 验收

3000万必须输出待核实而非正式新增授信

## 9. 开发落点

- 前端：加载 `PAGE-11` 对应页面状态。
- 后端：按 `SCALL-019` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-19`，并验证Human Gate `HG-E01` 没有被绕过。
