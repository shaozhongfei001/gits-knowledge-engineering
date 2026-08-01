# HZB-KNO-DEV-P0｜工程生成合同

| 项 | 受控值 |
|---|---|
| 工作流 | `HZB-KNO-DEV-P0` |
| 执行角色 | 项目开发经理 / 工程架构师 |
| 生成对象 | `HZB-KNO-DEV-PACKAGE-V0.1` |
| 目标状态 | `DEV_PACKAGE_CANDIDATE` |
| 下一门禁 | Owner Review → Independent QA |
| 冻结 | `NO` |

## 权威顺序

杭州银行已批准需求及Owner决策 → PB-HZB-CORP-V1.0-R2 → HLD受控候选 → HZB-KNO-TECH-V0.2 → SDD评估V0.1 → 旧框架参考资产。

## 本轮允许

- 关闭评估中5项P0 BLOCKER；
- 生成多合同、模块化单体、独立Worker、前端入口和适配器边界；
- 对运行本体核心对象建立最小代码和本地关系库迁移候选；
- 建立可执行的合同、Loop、秘密扫描、只读Guard和Dry-run；
- 形成总体22模块目录与首期纵向切片入口。

## 本轮禁止

- 不宣称22模块和206项功能已经实现；
- 不把模拟或离线自检表述为真实AIOS/CRM/Oracle/IAM/E2E通过；
- 不确定生产数据库，不启用Oracle采集，不接入真实凭据；
- 不把Apache Ossie、图数据库、工作流引擎或多智能体引入关键路径；
- 开发人员不得签署`QA_PASS`、`E2E_PASS`或业务验收。

## 缺失输入规则

真实接口、真实脱敏样本、生产数据库ADR、安全分级、容量目标或授权缺失时，只建立端口、合同和待验证项，禁止用假实现关闭对应验收项。

## 缺陷与状态

缺陷级别使用 `BLOCKER / MAJOR / MINOR / NOTE`。本轮只允许 `DEV_SELF_CHECK_PASS` 或 `BLOCKED`；独立QA后才可进入 `PASS_FOR_OWNER_REVIEW`。
