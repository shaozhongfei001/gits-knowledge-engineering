# PP-F03｜访前准备了问题，回来后没人自动检查哪些关键问题其实没问到

- Scene：`SCENE-P0-24`
- Evidence Level：`E1`
- Event：`EVT-030`
- Page State：`PAGE-13`
- Skill Call：`SCALL-024`
- Human Gate：`HG-F03`
- Acceptance Test：`AT-P0-24`

## 1. 客户经理真实痛点

访前准备做得再好，如果现场没问完，回来后不复盘，缺口还是缺口。

## 2. 在华东精工剧情里怎么发生

系统将访前问题与会中Interaction逐项对账，发现“他行方案细节”未回答，访后追问张伟是否获得。

## 3. 现场数据例子

- Question Coverage：他行结构细节=NOT_OBTAINED
- 继续滚入R8

## 4. 对话

**系统：** 访前问题覆盖检查：项目主体=部分回答；付款节奏=已回答；他行结构细节=未获得。

**张伟：** 他行细节保持未获得，下次再问，别自动补。

## 5. 系统怎么解决

Visit Objective/Question Plan Coverage Check + Debrief Follow-up。

### 页面状态

`PAGE-13`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-09` → `RSK-14,RSK-16`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-F03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

KYC Gap状态更新

## 7. 不能做什么

问题没回答不能用模型推断补齐

## 8. 验收

每个P0问题有覆盖状态，缺口继续进入下轮R8

## 9. 开发落点

- 前端：加载 `PAGE-13` 对应页面状态。
- 后端：按 `SCALL-024` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-24`，并验证Human Gate `HG-F03` 没有被绕过。
