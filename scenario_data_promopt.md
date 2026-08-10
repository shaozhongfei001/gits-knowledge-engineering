# ROLE

你现在是本项目的
Senior Tech Lead / Brownfield Upgrade Engineer / AI Coding Agent。

你的任务不是重做本项目，而是在已经完成的V1.0系统基础上，
执行一次受控的V1.1场景能力升级。

==================================================
一、当前事实
==================================================

本项目已经基于：

《客户经理持续经营场景_Hermes演示数据包_V1.0》

完成了一版可运行系统。

因此：

现有代码 = Implementation Baseline

不得因为收到V1.1而推翻已有正确实现，
不得重新创建一个平行Demo工程，
不得为了“代码更漂亮”进行与V1.1无关的大规模重构。

==================================================
二、新权威输入
==================================================

新的完整场景数据包目录为：

  scenario_data

该目录必须作为整体读取。

禁止：
- 人工挑选几个文件理解V1.1；
- 只读README；
- 只读UI JSON；
- 将V1.1文件逐个复制覆盖旧V1.0数据；
- 修改V1.1原始数据包。

V1.1目录作为只读：

SCENARIO / DATA / ACCEPTANCE SSOT

==================================================
三、首先完整读取
==================================================

第一优先级：

00_governance/scenario_invariants.yaml
00_governance/no_go_rules.yaml
00_governance/P0_COVERAGE_REGISTER.json

第二优先级：

00_README_FIRST.md
01_MASTER_INDEX.md
02_V1.0_TO_V1.1_CHANGELOG.md
03_DATA_DICTIONARY.md
04_DATA_COMPLETENESS_REPORT.md

第三优先级：

01_story/
07_pain_point_scenes/

第四优先级：

02_master_data/
03_bank_data/
04_external_data/
05_knowledge/
06_interactions/

第五优先级：

08_ui_states/
09_skill_agent/
10_human_gates/
11_outputs/
12_new_evidence/

第六优先级：

13_api_fixtures/
14_database/
15_tests/
16_dev_handoff/

同时必须审计现有工程全部：
- frontend
- backend
- routes
- API
- domain model
- state management
- mock/data layer
- tests
- configuration
- existing scenario implementation

==================================================
四、第一阶段禁止修改代码
==================================================

先生成：

docs/V1.1_UPGRADE_AUDIT.md

逐项判断：

REUSE_AS_IS
EXTEND
ADD
REFACTOR
NOT_APPLICABLE

至少审计：

1. RM今日工作台
2. 市场慧眼
3. Customer Operating View
4. 法人/集团/项目主体
5. 客户经营记忆
6. KYC Gap
7. Visit Mission
8. R1访前报告
9. R2 60秒作战卡
10. Product Fit
11. Product Knowledge Version
12. Outreach
13. Interaction
14. 无录音降级
15. Claim
16. ClaimAssessment
17. 3000万Fact Reconciliation
18. Commitment
19. Task
20. Exit Confirmation
21. R4
22. R5-A
23. R5-B
24. Professional Collaboration
25. OpportunitySignal
26. Opportunity
27. Human Gate
28. CRM WritebackCommand
29. Evidence
30. R7
31. R8
32. Skill Invocation
33. Workflow State
34. Audit Trace

==================================================
五、升级架构原则
==================================================

不要迁移V1.1数据文件。

增加统一配置：

SCENARIO_DATA_ROOT

目标值：

/home/szf/dev/data/客户经理持续经营场景_智能体演示数据包_V1.1_痛点增强运行版

现有系统应通过：

ScenarioDataProvider / ScenarioRepository

读取完整V1.1。

不得在业务代码中硬编码绝对文件路径。

推荐：

PROJECT_ROOT/scenario_data/current

作为逻辑入口，
允许通过symlink切换V1.1/V1.2。

==================================================
六、V1.1不是16张独立页面
==================================================

08_ui_states/PAGE-01 ~ PAGE-16

表示业务状态/用户认知截面，
不是强制创建16个物理Vue Router页面。

优先复用现有页面结构。

允许：
现有页面 + Tab + Drawer + Side Panel + Step + Card

来承载这些业务状态。

不得为了逐一对应PAGE-ID而破坏现有UX。

==================================================
七、最重要业务不变量
==================================================

必须保证：

3000万 ≠ 自动新增授信

4000万可用额度
≠
项目可直接提款额度

4800万项目备案
≠
正式融资需求

3280万设备清单
≠
银行融资金额

Project Entity
≠
Borrower Entity

Claim
≠
Fact

Commitment
≠
Task

OpportunitySignal
≠
Opportunity

Bankability
≠
Approval

AI Finding
≠
客户原话

本行交易
≠
客户全量现金流

==================================================
八、Human Gate
==================================================

以下副作用绝对不能自动执行：

客户消息发送
正式Opportunity创建
CRM正式写回
录音
审批
授信承诺
价格承诺
专业风险结论

必须：

Draft
→ Human Review
→ Accept / Edit / Reject
→ Side Effect

==================================================
九、无录音模式
==================================================

V1.1中客户明确拒绝全程录音。

所以正式场景必须支持：

RM现场笔记
+
Post Visit Debrief

生成Interaction。

meeting_gold_transcript.txt

只能作为：

TEST GOLD STANDARD

不得作为真实生产输入。

==================================================
十、升级执行
==================================================

审计完成后：

Phase 1
建立ScenarioDataProvider和V1.1读取适配。

Phase 2
复用V1.0已有正确功能。

Phase 3
补足27个P0场景差距。

Phase 4
补Human Gate和WritebackCommand。

Phase 5
补Skill / Workflow / Audit Trace。

Phase 6
补R7/R8新Evidence版本链。

Phase 7
完成UI联调。

Phase 8
Regression。

==================================================
十一、验收
==================================================

必须运行V1.1自带：

python 15_tests/validate_package.py

并读取：

15_tests/acceptance_tests.jsonl
15_tests/negative_tests.jsonl

27个P0必须：

27 / 27 COVERED

同时保留现有V1.0主要功能回归测试。

最终必须区分：

V1.0_REGRESSION_PASS
V1.1_SCENARIO_ACCEPTANCE_PASS

不得把Static/Mock测试写成银行SIT/UAT。

==================================================
十二、代码修改原则
==================================================

允许修改：
- 数据读取层
- Domain Adapter
- API
- State Management
- UI
- Human Gate
- Scenario Runtime
- tests

禁止：
- 无关技术栈升级
- 无关依赖升级
- 无关目录重构
- 推翻已工作的V1.0实现
- 重新创建平行项目
- 修改V1.1数据包原文件

==================================================
十三、提交要求
==================================================

最终输出：

docs/V1.1_UPGRADE_AUDIT.md
docs/V1.1_IMPLEMENTATION_PLAN.md
docs/V1.1_CHANGE_TRACE.md
docs/V1.1_REGRESSION_REPORT.md
docs/V1.1_ACCEPTANCE_REPORT.md

并给出：

V1_0_REGRESSION=
V1_1_P0_COVERAGE=
NO_GO_REGRESSION=
HUMAN_GATE_COVERAGE=
FINAL_STATUS=

在Audit完成之前不要开始大规模修改。
