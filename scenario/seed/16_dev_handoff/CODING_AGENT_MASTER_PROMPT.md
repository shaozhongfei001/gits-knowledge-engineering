# Coding Agent Master Prompt — V1.1

你正在实现“华东精工客户经理持续经营闭环”演示系统。

数据根目录：
`<SET_PACKAGE_ROOT>/客户经理持续经营场景_智能体演示数据包_V1.1_痛点增强运行版`

强制先读：
- 00_governance/scenario_invariants.yaml
- 00_governance/no_go_rules.yaml
- 00_governance/P0_COVERAGE_REGISTER.json
- 01_story/02_ZHANGWEI_FULL_DAY_20260708.md
- 16_dev_handoff/SCENE_SCREEN_SKILL_API_TRACE.md
- 15_tests/acceptance_tests.jsonl

目标：
1. 以PAGE-01~PAGE-16为前端主流程；
2. 使用13_api_fixtures作为初始后端合同；
3. P0的27个场景全部可回放；
4. 每个关键结论显示Evidence/类型/时点；
5. 所有Human Gate可见、可操作、可审计；
6. CRM只模拟WritebackCommand，不直接写任何真实CRM；
7. 客户拒绝录音时必须切换“笔记+访后口述”；
8. 新Evidence进入后R7/R8有版本差异。

严禁：
- 3000万自动变新增授信；
- 4000万自动变项目可用额度；
- 外部事件直接变客户事实；
- Signal直接升级Opportunity；
- Bankability写成Approval；
- 自动外发客户消息；
- 自动写CRM；
- 使用失效产品版本；
- 缺数据时伪造完整结论。

开发结果至少包含：
- 页面主流程；
- API mock；
- 状态管理；
- Human Gate交互；
- P0场景自动回放；
- 测试；
- README。
