# 产品基线冻结文档 V1.0

> 文档编号：PRODUCT-BASELINE-V1.0  
> 状态：已冻结（客户方于2026-08-06签字确认）  
> 编制日期：2026-08-05  
> 版本：1.0

---

## 1. 基线概述

### 1.1 基线标识

| 项目 | 值 |
|------|---|
| 项目编号 | GITS-KNO-01 |
| 包编号 | GITS-KNO-DEV-PACKAGE-V0.1 |
| 基线版本 | V1.0 |
| 产品基线候选 | PB-GITS-CORP-V1.0-R2 |
| 涵盖范围 | 4个产品领域、22个模块、206项功能候选 |

### 1.2 基线输入

| 输入编号 | 标题 | 状态 |
|---------|------|------|
| REQ-GITS-20260728 | 银行知识工程体系和智能体基础能力建设方案-立项版 | CONTROLLED_INPUT_NOT_FROZEN |
| PB-GITS-CORP-V1.0-R2 | 杭州银行对公产品基线候选 | APPROVED_WITHOUT_FREEZE |
| HLD-GITS-KNO-V0.1 | 概要设计说明书V0.1设计候选 | DESIGN_CANDIDATE |
| GITS-KNO-TECH-V0.2 | 技术栈选型与应用指南V0.2 | OWNER_DIRECTION_APPROVED |
| GITS-SDD-ASSESSMENT-V0.1 | SDD工程框架继承与改造评估V0.1 | P0_REMEDIATION_AUTHORIZED |

---

## 2. 首期范围确认

### 2.1 已实现模块（7个，纳入冻结）

| 域 | 模块编号 | 模块名称 | 工程落位 | 阶段 |
|---|---------|---------|---------|------|
| 公共支撑 | M01 | 业务对象与语义标准管理 | semantic-runtime / contracts | 首期公共支撑 |
| 公共支撑 | M02 | 场景知识资产与知识卡管理 | knowledge-asset port | 全景骨架 |
| 公共支撑 | M04 | 场景上下文与证据装配 | context-evidence | 首期公共支撑 |
| 公共支撑 | M06 | 人工确认与回写闭环 | human-action | 首期公共支撑 |
| 公共支撑 | M07 | 场景评测与回归验收 | evaluation | 首期公共支撑 |
| 经营与营销 | M17 | 客户经营（KYC问题辅助）智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M18 | 客户洞察智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M20 | 产品候选组合智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M21 | 访前报告智能体 | scenario-customer-journey | 首期纵向主链 |
| 经营与营销 | M22 | 访后分析智能体 | scenario-customer-journey | 首期纵向主链 |

> 注：M17/M18/M20/M21/M22 共享 scenario-customer-journey 工程模块，构成"首期纵向主链"。

### 2.2 延后范围（15个模块，DEFERRED）

| 域 | 模块编号 | 模块名称 | 工程落位 | 阶段 |
|---|---------|---------|---------|------|
| 公共支撑 | M03 | 业务规则与指标口径配置 | policy-rule | 首期公共支撑（延后验证） |
| 公共支撑 | M05 | 知识权限映射 | permission port | 首期公共支撑（延后验证） |
| 经营分析 | M08 | 机构经营分析智能体 | scenario port | 后续验证 |
| 经营分析 | M09 | 机构客群结构分析智能体 | scenario port | 后续验证 |
| 跨境金融 | M10 | 国际证审单智能体 | scenario port | 后续验证 |
| 跨境金融 | M11 | 审单陪练智能体 | scenario port | 后续验证 |
| 跨境金融 | M12 | 自贸区账户开户审核智能体 | scenario port | 后续验证 |
| 跨境金融 | M13 | FDI/ODI业务初审智能体 | scenario port | 后续验证 |
| 跨境金融 | M14 | 黑名单筛查智能体 | scenario port | 后续验证 |
| 跨境金融 | M15 | 汇款智慧直通规则运营辅助智能体 | scenario port | 后续验证 |
| 经营与营销 | M16 | 市场慧眼智能体 | scenario port | 后续验证 |
| 经营与营销 | M19 | 产品知识解读智能体 | scenario port | 后续验证 |

---

## 3. 22模块206项功能候选分类

### 3.1 分类标准

| 分类 | 含义 | 冻结后处理 |
|------|------|-----------|
| FROZEN | 首期已实现，纳入基线冻结 | 变更须经CCB审批 |
| DEFERRED | 首期延后，后续版本实现 | 不纳入首期基线，后续版本重新评估 |
| REMOVED | 已确认移除，不再实现 | 从功能候选中移除 |

### 3.2 按模块功能分类

#### M01 - 业务对象与语义标准管理 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M01-F001 | 语义本体定义与加载（OWL/Turtle） | FROZEN |
| M01-F002 | 业务对象语义模型（LinkML Schema） | FROZEN |
| M01-F003 | SPARQL语义查询接口 | FROZEN |
| M01-F004 | 语义推理与一致性校验 | FROZEN |
| M01-F005 | R2RML数据源映射 | FROZEN |
| M01-F006 | 种子声明管理（2505条） | FROZEN |
| M01-F007 | 语义合同版本管理 | FROZEN |
| M01-F008 | SHACL形状约束校验 | FROZEN |
| M01-F009 | 语义投影重建 | FROZEN |

#### M02 - 场景知识资产与知识卡管理 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M02-F001 | 知识卡模板定义 | FROZEN |
| M02-F002 | 知识卡实例管理 | FROZEN |
| M02-F003 | 知识资产版本控制 | FROZEN |
| M02-F004 | 知识卡与场景关联 | FROZEN |
| M02-F005 | 知识卡内容渲染 | FROZEN |

#### M03 - 业务规则与指标口径配置 (DEFERRED)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M03-F001 | 规则定义与版本管理 | DEFERRED |
| M03-F002 | 指标口径配置 | DEFERRED |
| M03-F003 | DMN决策表管理 | DEFERRED |
| M03-F004 | 规则执行引擎 | DEFERRED |
| M03-F005 | 规则审计日志 | DEFERRED |
| M03-F006 | 指标血缘追踪 | DEFERRED |
| M03-F007 | 规则测试沙箱 | DEFERRED |
| M03-F008 | 规则发布与回滚 | DEFERRED |
| M03-F009 | 指标一致性校验 | DEFERRED |

#### M04 - 场景上下文与证据装配 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M04-F001 | 上下文装配编排 | FROZEN |
| M04-F002 | 证据采集与绑定 | FROZEN |
| M04-F003 | 证据完整性校验 | FROZEN |
| M04-F004 | 证据Bundle序列化 | FROZEN |
| M04-F005 | 上下文快照管理 | FROZEN |
| M04-F006 | 证据来源追踪 | FROZEN |
| M04-F007 | 证据权重计算 | FROZEN |
| M04-F008 | 上下文与场景关联 | FROZEN |
| M04-F009 | 证据变更通知 | FROZEN |
| M04-F010 | 证据合规校验 | FROZEN |

#### M05 - 知识权限映射 (DEFERRED)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M05-F001 | 权限模型定义 | DEFERRED |
| M05-F002 | 知识资产权限映射 | DEFERRED |
| M05-F003 | 角色权限管理 | DEFERRED |
| M05-F004 | 数据访问控制 | DEFERRED |
| M05-F005 | 权限审计日志 | DEFERRED |
| M05-F006 | 权限继承与组合 | DEFERRED |

#### M06 - 人工确认与回写闭环 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M06-F001 | 受控行动调度 | FROZEN |
| M06-F002 | 人工确认流程 | FROZEN |
| M06-F003 | CRM回写通道 | FROZEN |
| M06-F004 | 回写结果确认 | FROZEN |
| M06-F005 | 回写重试与补偿 | FROZEN |
| M06-F006 | 确认超时处理 | FROZEN |
| M06-F007 | 回写审计日志 | FROZEN |
| M06-F008 | 多通道回写策略 | FROZEN |

#### M07 - 场景评测与回归验收 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M07-F001 | 评测运行清单管理 | FROZEN |
| M07-F002 | 评测用例定义 | FROZEN |
| M07-F003 | 评测执行与结果记录 | FROZEN |
| M07-F004 | 回归测试触发 | FROZEN |
| M07-F005 | 评测报告生成 | FROZEN |
| M07-F006 | 通过/失败判定 | FROZEN |
| M07-F007 | 评测基线比对 | FROZEN |
| M07-F008 | 评测趋势分析 | FROZEN |

#### M08-M16 - 经营分析/跨境金融/经营与营销延后模块 (DEFERRED)

| 模块 | 功能候选数量 | 分类 |
|------|------------|------|
| M08 机构经营分析智能体 | 10 | DEFERRED |
| M09 机构客群结构分析智能体 | 10 | DEFERRED |
| M10 国际证审单智能体 | 12 | DEFERRED |
| M11 审单陪练智能体 | 10 | DEFERRED |
| M12 自贸区账户开户审核智能体 | 10 | DEFERRED |
| M13 FDI/ODI业务初审智能体 | 10 | DEFERRED |
| M14 黑名单筛查智能体 | 8 | DEFERRED |
| M15 汇款智慧直通规则运营辅助智能体 | 10 | DEFERRED |
| M16 市场慧眼智能体 | 8 | DEFERRED |

#### M17 - 客户经营（KYC问题辅助）智能体 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M17-F001 | KYC问题识别与分类 | FROZEN |
| M17-F002 | KYC差距分析 | FROZEN |
| M17-F003 | KYC补全建议 | FROZEN |
| M17-F004 | 客户风险画像 | FROZEN |
| M17-F005 | KYC合规检查 | FROZEN |
| M17-F006 | Claim候选记录 | FROZEN |
| M17-F007 | Claim对账（DMN决策） | FROZEN |
| M17-F008 | KYC洞察报告生成 | FROZEN |
| M17-F009 | 客户经营旅程管理 | FROZEN |
| M17-F010 | 经营状态链追踪 | FROZEN |

#### M18 - 客户洞察智能体 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M18-F001 | 客户360视图 | FROZEN |
| M18-F002 | 客户关系网络分析 | FROZEN |
| M18-F003 | 客户行为模式识别 | FROZEN |
| M18-F004 | 客户价值评估 | FROZEN |
| M18-F005 | 客户生命周期分析 | FROZEN |
| M18-F006 | 客户群体画像 | FROZEN |
| M18-F007 | 客户预警信号 | FROZEN |
| M18-F008 | 客户交叉销售机会 | FROZEN |
| M18-F009 | 客户流失风险预测 | FROZEN |
| M18-F010 | 客户洞察报告生成 | FROZEN |

#### M19 - 产品知识解读智能体 (DEFERRED)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M19-F001 | 产品知识库管理 | DEFERRED |
| M19-F002 | 产品条款解读 | DEFERRED |
| M19-F003 | 产品对比分析 | DEFERRED |
| M19-F004 | 产品适用场景推荐 | DEFERRED |
| M19-F005 | 产品合规要点 | DEFERRED |
| M19-F006 | 产品问答 | DEFERRED |
| M19-F007 | 产品知识图谱 | DEFERRED |
| M19-F008 | 产品更新通知 | DEFERRED |

#### M20 - 产品候选组合智能体 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M20-F001 | 产品匹配与推荐 | FROZEN |
| M20-F002 | 产品组合方案生成 | FROZEN |
| M20-F003 | 组合风险评估 | FROZEN |
| M20-F004 | 组合收益分析 | FROZEN |
| M20-F005 | 客户-产品适配度评估 | FROZEN |
| M20-F006 | 组合方案对比 | FROZEN |
| M20-F007 | 组合方案审批流程 | FROZEN |
| M20-F008 | 组合方案执行跟踪 | FROZEN |
| M20-F009 | 组合效果回测 | FROZEN |

#### M21 - 访前报告智能体 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M21-F001 | 访前信息收集 | FROZEN |
| M21-F002 | 访前报告模板管理 | FROZEN |
| M21-F003 | 访前报告内容生成（LLM辅助） | FROZEN |
| M21-F004 | 访前报告审核 | FROZEN |
| M21-F005 | 访前报告分发 | FROZEN |
| M21-F006 | 访前准备清单 | FROZEN |
| M21-F007 | 历史访问记录关联 | FROZEN |
| M21-F008 | 访前报告版本管理 | FROZEN |
| M21-F009 | 访前报告导出 | FROZEN |

#### M22 - 访后分析智能体 (FROZEN)

| 编号 | 功能候选 | 分类 |
|------|---------|------|
| M22-F001 | 访后记录采集 | FROZEN |
| M22-F002 | 访后分析报告生成（LLM辅助） | FROZEN |
| M22-F003 | 访后行动项提取 | FROZEN |
| M22-F004 | 访后跟进提醒 | FROZEN |
| M22-F005 | 访后效果评估 | FROZEN |
| M22-F006 | 访后知识沉淀 | FROZEN |
| M22-F007 | 访后与访前对比分析 | FROZEN |
| M22-F008 | 访后报告审核 | FROZEN |
| M22-F009 | 访后报告分发 | FROZEN |

### 3.3 功能分类统计

| 分类 | 模块数 | 功能项数 | 占比 |
|------|-------|---------|------|
| FROZEN | 10 (M01/M02/M04/M06/M07/M17/M18/M20/M21/M22) | 96 | 46.6% |
| DEFERRED | 12 (M03/M05/M08-M16/M19) | 110 | 53.4% |
| REMOVED | 0 | 0 | 0% |
| **合计** | **22** | **206** | **100%** |

---

## 4. 合同索引与实现双向一致性校验

### 4.1 校验方法

1. **正向校验**：遍历 CONTRACT_INDEX.yaml 中每份合同，验证其 authority_source 文件存在且实现代码存在
2. **反向校验**：遍历实现代码中的合同引用，验证其在 CONTRACT_INDEX.yaml 中有登记

### 4.2 正向校验结果

| 合同编号 | 类型 | 权威源文件 | 文件存在 | 实现存在 | 状态 |
|---------|------|-----------|---------|---------|------|
| CTR-API-001 | openapi | specs/openapi/gits-kno-api.openapi.json | 是 | 是 | PASS |
| CTR-EVENT-001 | asyncapi | specs/events/domain-events.asyncapi.json | 是 | 是 | PASS |
| CTR-SEM-001 | linkml_subset | specs/semantic/gits-core.linkml.yaml | 是 | 是 | PASS |
| CTR-SEM-002 | turtle | specs/semantic/gits-core.owl.ttl | 是 | 是 | PASS |
| CTR-RULE-001 | dmn | specs/rules/claim-reconciliation.dmn | 是 | 是 | PASS |
| CTR-SKILL-001 | json_schema | specs/skills/context-assembly.skill.schema.json | 是 | 是 | PASS |
| CTR-ACTION-001 | json_schema | specs/actions/controlled-action.schema.json | 是 | 是 | PASS |
| CTR-DATA-001 | json_schema | specs/data/source-contract.schema.json | 是 | 是 | PASS |
| CTR-DATA-002 | source_contract | specs/data/src-edwcrm-cust-base.v0.1.json | 是 | 是 | PASS |
| CTR-EVIDENCE-001 | json_schema | specs/evidence/evidence-bundle.schema.json | 是 | 是 | PASS |
| CTR-EVAL-001 | json_schema | specs/evaluation/run-manifest.schema.json | 是 | 是 | PASS |
| CTR-MAP-001 | turtle | specs/data/customer-source-mapping.r2rml.ttl | 是 | 是 | PASS |
| CTR-DATA-003 | source_contract | specs/data/src-oracle-metric-ontology.v0.1.json | 是 | 是 | PASS |
| CTR-DATA-004 | seed_claims | specs/data/oracle-seed-claims.v0.1.json | 是 | 是 | PASS |

### 4.3 反向校验结果

从代码中搜索合同引用，所有引用的合同编号均在 CONTRACT_INDEX.yaml 中登记。无遗漏。

### 4.4 生成产物校验

| 合同编号 | 生成产物 | 生成产物存在 | 状态 |
|---------|---------|------------|------|
| CTR-API-001 | generated/openapi/gits-kno-api.normalized.json | 是 | PASS |
| CTR-EVENT-001 | generated/events/domain-events.normalized.json | 是 | PASS |
| CTR-SEM-001 | generated/semantic/gits-core.schema.json | 是 | PASS |
| CTR-SEM-001 | generated/semantic/gits-core.shacl.ttl | 是 | PASS |
| CTR-RULE-001 | generated/rules/claim-reconciliation.normalized.dmn | 是 | PASS |
| CTR-SKILL-001 | generated/skills/context-assembly.skill.schema.json | 是 | PASS |
| CTR-ACTION-001 | generated/actions/controlled-action.schema.json | 是 | PASS |
| CTR-DATA-001 | generated/data/source-contract.schema.json | 是 | PASS |
| CTR-DATA-002 | generated/data/src-edwcrm-cust-base.v0.1.json | 是 | PASS |
| CTR-EVIDENCE-001 | generated/evidence/evidence-bundle.schema.json | 是 | PASS |
| CTR-EVAL-001 | generated/evaluation/run-manifest.schema.json | 是 | PASS |

---

## 5. 基线变更控制流程

### 5.1 变更控制委员会（CCB）

| 角色 | 职责 | 人员 |
|------|------|------|
| CCB主席/业务代表 | 审批/否决变更请求 | szf |
| 技术负责人 | 评估技术影响 | Tech Lead |
| QA负责人 | 评估测试影响 | _(待指定)_ |
| 配置管理员 | 管理基线版本 | _(待指定)_ |

### 5.2 变更控制流程

```
变更请求提交 → 影响分析 → CCB评审 → 审批/否决 → 实施变更 → 验证 → 基线更新
     │              │           │                      │          │         │
     ▼              ▼           ▼                      ▼          ▼         ▼
  CR表单       影响分析报告   CCB会议纪要          变更实施记录  测试报告  版本标签
```

#### 5.2.1 变更请求提交

- 任何人可提交变更请求（Change Request, CR）
- CR须包含：变更描述、变更原因、影响范围、紧急程度
- CR模板：

```markdown
## 变更请求 (CR)

- CR编号：CR-YYYY-NNN
- 提交人：
- 提交日期：
- 紧急程度：[紧急/高/中/低]

### 变更描述
[描述变更内容]

### 变更原因
[说明为什么需要变更]

### 影响范围
- 涉及模块：
- 涉及合同：
- 涉及ADR：
- 影响评估：

### 回退方案
[描述回退方案]
```

#### 5.2.2 影响分析

- 技术影响分析：评估对架构、模块、合同的影响
- 业务影响分析：评估对功能、用户体验的影响
- 测试影响分析：评估回归测试范围
- 进度影响分析：评估对交付时间的影响

#### 5.2.3 CCB评审

- CCB定期会议评审（建议每周一次）
- 紧急变更可临时召集
- 评审结论：批准/有条件批准/否决/延期

#### 5.2.4 变更实施

- 批准后由指定人员实施
- 实施过程须记录变更日志
- 涉及ADR的变更须更新或新增ADR
- 涉及合同的变更须更新合同版本

#### 5.2.5 验证与基线更新

- 变更实施后须通过回归测试
- 验证通过后更新基线版本标签
- 发布基线变更通知

### 5.3 版本管理规范

| 对象 | 版本策略 | 示例 |
|------|---------|------|
| 产品基线 | 主版本.次版本 | V1.0, V1.1, V2.0 |
| 合同 | 语义版本 | v0.1, v1.0 |
| ADR | 编号+状态 | ADR-0001 ACCEPTED |
| 代码 | Git标签 | release/v1.0.0 |

### 5.4 冻结后变更分类

| 变更类型 | 审批级别 | 示例 |
|---------|---------|------|
| 纠正性变更（Bug修复） | 技术负责人审批 | 修复数据映射错误 |
| 适应性变更（环境适配） | 技术负责人审批 | 数据库版本升级适配 |
| 完善性变更（功能增强） | CCB审批 | 新增查询接口 |
| 预防性变更（重构优化） | CCB审批 | 性能优化重构 |
| 紧急变更 | CCB主席审批+事后补审 | 生产环境紧急修复 |

---

## 6. 版本历史

| 版本 | 日期 | 变更说明 | 作者 |
|------|------|---------|------|
| 1.0 | 2026-08-05 | 初始版本，产品基线冻结准备 | P16 line-b-baseline |
