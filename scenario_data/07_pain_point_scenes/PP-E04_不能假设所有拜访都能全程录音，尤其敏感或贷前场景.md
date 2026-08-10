# PP-E04｜不能假设所有拜访都能全程录音，尤其敏感或贷前场景

- Scene：`SCENE-P0-21`
- Evidence Level：`E1`
- Event：`EVT-018`
- Page State：`PAGE-10`
- Skill Call：`SCALL-021`
- Human Gate：`HG-E04`
- Acceptance Test：`AT-P0-21`

## 1. 客户经理真实痛点

有的客户不接受录音，有的场景不适合开着录音设备，系统不能把全程录音当唯一入口。

## 2. 在华东精工剧情里怎么发生

演示可用合成Transcript，但产品同时支持现场笔记、访后口述、图片和经授权材料。

## 3. 现场数据例子

- recording_consent=DECLINED
- 生产输入=8条现场笔记+3分20秒访后口述
- Gold transcript=仅测试

## 4. 对话

**张伟：** 李总，如果您方便，我们可以开录音用于会后整理，您不方便也完全没问题。

**李明：** 项目还没完全定，我不希望全程录音。

**张伟：** 明白，我只做简要笔记，回去后自己口述复盘。

## 5. 系统怎么解决

多模态Interaction采集：授权录音/现场笔记/访后口述/附件并行，并记录来源和授权状态。

### 页面状态

`PAGE-10`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-06+SP-09+CONSENT_POLICY` → `RSK-10,RSK-14`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-E04`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

Interaction对象及来源

## 7. 不能做什么

未经授权不得录音；无录音不能假装有完整原话

## 8. 验收

无录音时场景仍可运行并明确证据等级

## 9. 开发落点

- 前端：加载 `PAGE-10` 对应页面状态。
- 后端：按 `SCALL-021` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-21`，并验证Human Gate `HG-E04` 没有被绕过。
