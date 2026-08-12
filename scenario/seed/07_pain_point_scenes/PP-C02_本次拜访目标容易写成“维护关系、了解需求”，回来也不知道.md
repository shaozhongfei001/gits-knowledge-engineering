# PP-C02｜本次拜访目标容易写成“维护关系、了解需求”，回来也不知道算不算完成

- Scene：`SCENE-P0-11`
- Evidence Level：`E1`
- Event：`EVT-008`
- Page State：`PAGE-07`
- Skill Call：`SCALL-011`
- Human Gate：`HG-C02`
- Acceptance Test：`AT-P0-11`

## 1. 客户经理真实痛点

“去维护一下关系”“顺便聊聊融资”这种目标太虚，现场很容易聊散。

## 2. 在华东精工剧情里怎么发生

华东精工主目标明确为核实二期项目主体与3000万资金安排语义；现金管理只是副目标。

## 3. 现场数据例子

- 主目标=项目主体+3000万语义
- 副目标=供应链/现金管理

## 4. 对话

**系统：** 建议主目标：核实二期项目主体与第一阶段设备款资金安排。

**张伟：** 确认。现金管理和供应链只作为副目标，不要把话题带散。

## 5. 系统怎么解决

Visit Mission Planner把经营触发转成可验证的主目标、副目标和离场条件。

### 页面状态

`PAGE-07`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-04` → `RSK-07`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-C02`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

VisitPlan

## 7. 不能做什么

不能把产品销售目标自动当客户真实需求

## 8. 验收

访后可逐项目标判断完成/未完成/待跟进

## 9. 开发落点

- 前端：加载 `PAGE-07` 对应页面状态。
- 后端：按 `SCALL-011` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-11`，并验证Human Gate `HG-C02` 没有被绕过。
