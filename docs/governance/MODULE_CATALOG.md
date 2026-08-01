# 22模块工程目录与阶段映射

模块编号沿用当前产品/HLD候选作为追踪定位符。未获得Owner冻结前，不将其解释为最终产品基线。

| 域 | ID | 模块 | 工程落位 | 当前阶段 |
|---|---|---|---|---|
| 公共支撑 | M01 | 业务对象与语义标准管理 | semantic-runtime / contracts | 首期公共支撑 |
| 公共支撑 | M02 | 场景知识资产与知识卡管理 | knowledge-asset port | 全景骨架 |
| 公共支撑 | M03 | 业务规则与指标口径配置 | policy-rule | 首期公共支撑 |
| 公共支撑 | M04 | 场景上下文与证据装配 | context-evidence | 首期公共支撑 |
| 公共支撑 | M05 | 知识权限映射 | permission port | 首期公共支撑 |
| 公共支撑 | M06 | 人工确认与回写闭环 | human-action | 首期公共支撑 |
| 公共支撑 | M07 | 场景评测与回归验收 | evaluation | 首期公共支撑 |
| 经营分析 | M08 | 机构经营分析智能体 | scenario port | 后续验证 |
| 经营分析 | M09 | 机构客群结构分析智能体 | scenario port | 后续验证 |
| 跨境金融 | M10 | 国际证审单智能体 | scenario port | 后续验证 |
| 跨境金融 | M11 | 审单陪练智能体 | scenario port | 后续验证 |
| 跨境金融 | M12 | 自贸区账户开户审核智能体 | scenario port | 后续验证 |
| 跨境金融 | M13 | FDI/ODI业务初审智能体 | scenario port | 后续验证 |
| 跨境金融 | M14 | 黑名单筛查智能体 | scenario port | 后续验证 |
| 跨境金融 | M15 | 汇款智慧直通规则运营辅助智能体 | scenario port | 后续验证 |
| 经营与营销 | M16 | 市场慧眼智能体 | scenario port | 后续验证 |
| 经营与营销 | M17 | 客户经营（KYC问题辅助）智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M18 | 客户洞察智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M19 | 产品知识解读智能体 | scenario port | 后续验证 |
| 经营与营销 | M20 | 产品候选组合智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M21 | 访前报告智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M22 | 访后分析智能体 | scenario-customer-journey | 首期纵向主链 |

首期不是五个孤立智能体，而是同一个 `OperatingCase` 下的六阶段持续经营状态链。`Interaction / Claim / Evidence / HumanConfirmation / Action / Receipt / Evaluation`由公共内核承担，场景模块不能各自复制一套状态与责任链。
