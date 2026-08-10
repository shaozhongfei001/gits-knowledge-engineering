# Cursor / Codex / CodeBuddy 开发交接

## 1. 先不要让Coding Agent“自己理解银行业务”
开发Agent必须先读取：
1. `00_governance/scenario_invariants.yaml`
2. `00_governance/no_go_rules.yaml`
3. `00_governance/P0_COVERAGE_REGISTER.json`
4. `01_story/02_ZHANGWEI_FULL_DAY_20260708.md`
5. `16_dev_handoff/SCENE_SCREEN_SKILL_API_TRACE.md`
6. `15_tests/acceptance_tests.jsonl`

## 2. 前后端都以场景对象为合同
前端不要硬编码剧情文案；从API fixture或后端对象读取。
后端不要把“3000万”写死为融资需求；必须从ClaimAssessment状态返回。

## 3. 开发时必须保留的对象
- LegalEntity / ProjectEntityRole
- Interaction / Claim / ClaimAssessment
- Evidence / AnalyticalFinding
- Commitment / Task
- OpportunitySignal / Opportunity
- ProductKnowledgeVersion
- WritebackCommand / HumanGateDecision
- ReportVersion

## 4. 演示优先
第一阶段不需要连接真实银行系统。先使用：
- `13_api_fixtures/mock_api_server.py`
- `13_api_fixtures/requests/`
- `13_api_fixtures/responses/`
- `14_database/`（可选）

## 5. 每次开发完成必须跑
```bash
python 15_tests/validate_package.py
python -m unittest 15_tests/test_scenario_data.py
```

并针对页面实现补充前端E2E，不得把本包STATIC_REPLAY测试写成真实银行SIT/UAT通过。
