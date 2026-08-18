# 客户经理持续经营场景_智能体演示数据包_V1.1
## 痛点增强运行版

这是V1.0的正式增量升级，不是重新想象一套场景。

## 这版解决什么问题

V1.0主要证明“华东精工持续经营机制可以跑通”。V1.1进一步要求：

> **每个P0痛点都必须在张伟的真实工作过程中发生，并且有剧情、有数据、有对话、有页面状态、有Skill调用、有Human Gate、有验收。**

当前结果：
- 全量痛点：39项；
- P0：27项；
- P0 FULLY_BOUND：27/27；
- P1/P2也已提供剧情扩展文件，但不冒充P0完成度。

## 最先看什么

### 产品/业务人员
1. `01_story/02_ZHANGWEI_FULL_DAY_20260708.md`
2. `07_pain_point_scenes/00_P0_SCENE_INDEX.md`
3. `V1.1_开发索引与P0覆盖矩阵.xlsx`

### Cursor / Codex / CodeBuddy
1. `00_governance/scenario_invariants.yaml`
2. `00_governance/no_go_rules.yaml`
3. `00_governance/P0_COVERAGE_REGISTER.json`
4. `16_dev_handoff/CODING_AGENT_MASTER_PROMPT.md`
5. `16_dev_handoff/SCENE_SCREEN_SKILL_API_TRACE.md`
6. `15_tests/validate_package.py`

### 前端
从 `08_ui_states/PAGE-01.json` 到 `PAGE-16.json` 实现主流程。

### 后端
先使用 `13_api_fixtures/openapi_stub.yaml` + `mock_api_server.py`。

## 数据规模

- 本行可见交易：3600条；
- 日余额：1512条；
- 外部事件：160条；
- 历史Interaction：40条；
- Gold Standard会中Utterance：107条；
- UI页面状态：18个；
- Skill调用：39次；
- Human Gate：27个；
- Runtime Event Stream：102条；
- 验收测试：37条；
- 负向测试：12条。

## 重要边界

全部数据为合成演示数据。

客户在主剧情中拒绝全程录音，所以生产模拟输入是：
`现场笔记 + 访后口述`

`meeting_gold_transcript.txt`仅是测试Gold Standard，用来验证抽取是否正确，不代表银行生产系统取得了客户录音。
