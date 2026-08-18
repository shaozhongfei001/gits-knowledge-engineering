# PP-D03｜产品和制度变化靠群通知、培训传递，客户经理容易拿旧规则去谈客户

- Scene：`SCENE-P0-18`
- Evidence Level：`E1`
- Event：`EVT-009`
- Page State：`PAGE-08`
- Skill Call：`SCALL-018`
- Human Gate：`HG-D03`
- Acceptance Test：`AT-P0-18`

## 1. 客户经理真实痛点

规则更新后，真正的问题不是有没有通知，而是一线做方案时能不能自动用到最新版本。

## 2. 在华东精工剧情里怎么发生

查看三年期设备融资候选时，系统展示当前有效版本、材料要求和用途边界，失效版本不参与推荐。

## 3. 现场数据例子

- PROD-FAL V2.1 effective_to=2026-06-30 RETIRED
- V2.2 effective_from=2026-07-01 ACTIVE

## 4. 对话

**系统：** PROD-FAL V2.1已失效，当前有效版本V2.2，自2026-07-01生效。

**赵敏：** 确认按V2.2做准备，旧版材料要求不要再带给客户。

## 5. 系统怎么解决

产品知识版本化、有效期和影响关系治理；Skill只消费当前有效知识。

### 页面状态

`PAGE-08`。页面必须先把问题和数据解释给RM，再提供动作按钮。

### Skill / Policy

`SP-15+PRODUCT_VERSION_POLICY` → `RSK-24`。调用日志见 `09_skill_agent/skill_invocation_trace.jsonl`。

### Human Gate

`HG-D03`。最终决策见 `10_human_gates/human_gate_decisions.jsonl`。

## 6. 需要写回什么

知识资产版本

## 7. 不能做什么

失效规则不能继续作为现行规则被调用

## 8. 验收

每次推荐可追溯到产品/规则版本

## 9. 开发落点

- 前端：加载 `PAGE-08` 对应页面状态。
- 后端：按 `SCALL-018` 的Input/Output Contract准备接口。
- 测试：执行 `AT-P0-18`，并验证Human Gate `HG-D03` 没有被绕过。
