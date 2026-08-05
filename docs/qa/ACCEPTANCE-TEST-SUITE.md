# E2E验收测试用例集

> 版本: V1.0 | 日期: 2026-08-06 | 项目: GITS知识工程

## 概述

本文档定义GITS知识工程系统E2E验收测试用例集，覆盖5大业务链路共42项测试。
自动化脚本: `scripts/e2e-acceptance.sh`

---

## 链路1: 客户上下文管理 (8项)

| ID | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|----|----------|----------|----------|----------|
| E2E-CC-01 | 创建客户上下文 | 无 | POST /api/v1/customer-contexts with valid body | 201, 返回含id的JSON |
| E2E-CC-02 | 查询单个客户上下文 | E2E-CC-01通过 | GET /api/v1/customer-contexts/{id} | 200, 返回完整客户信息 |
| E2E-CC-03 | 查询客户上下文列表 | 至少1条数据 | GET /api/v1/customer-contexts | 200, 返回分页列表 |
| E2E-CC-04 | 分页查询 | 至少3条数据 | GET /api/v1/customer-contexts?size=2&page=0 | 200, 返回2条数据 |
| E2E-CC-05 | 更新客户上下文 | E2E-CC-01通过 | PUT /api/v1/customer-contexts/{id} | 200, 返回更新后数据 |
| E2E-CC-06 | 删除客户上下文 | E2E-CC-01通过 | DELETE /api/v1/customer-contexts/{id} | 204 |
| E2E-CC-07 | 创建重复客户ID | 无 | POST 相同customerId | 409 Conflict |
| E2E-CC-08 | 创建空body | 无 | POST /api/v1/customer-contexts with {} | 400 Bad Request |

## 链路2: 客户旅程管理 (8项)

| ID | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|----|----------|----------|----------|----------|
| E2E-JR-01 | 启动KYC审查旅程 | 存在客户上下文 | POST /api/v1/engagement-journeys | 201, status=STARTED |
| E2E-JR-02 | 查询旅程详情 | E2E-JR-01通过 | GET /api/v1/engagement-journeys/{id} | 200, 含完整旅程信息 |
| E2E-JR-03 | 查询旅程列表 | 至少1条旅程 | GET /api/v1/engagement-journeys | 200, 返回分页列表 |
| E2E-JR-04 | 按客户过滤旅程 | 存在多条旅程 | GET /api/v1/engagement-journeys?customerContextId=xxx | 200, 仅返回该客户旅程 |
| E2E-JR-05 | 完成旅程 | 旅程状态为STARTED | POST /api/v1/engagement-journeys/{id}/complete | 200, status=COMPLETED |
| E2E-JR-06 | 启动旅程-无效客户ID | 无 | POST with 不存在的customerContextId | 404 Not Found |
| E2E-JR-07 | 重复完成旅程 | 旅程已COMPLETED | POST /api/v1/engagement-journeys/{id}/complete | 409 Conflict |
| E2E-JR-08 | 查询旅程事件 | 旅程有事件 | GET /api/v1/engagement-journeys/{id}/events | 200, 返回事件列表 |

## 链路3: 知识规则管理 (8项)

| ID | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|----|----------|----------|----------|----------|
| E2E-KR-01 | 创建阈值规则 | 无 | POST /api/v1/knowledge-rules with THRESHOLD type | 201 |
| E2E-KR-02 | 创建复合规则 | 无 | POST /api/v1/knowledge-rules with COMPOSITE type | 201 |
| E2E-KR-03 | 查询规则列表 | 至少1条规则 | GET /api/v1/knowledge-rules | 200 |
| E2E-KR-04 | 更新规则优先级 | E2E-KR-01通过 | PUT /api/v1/knowledge-rules/{id} | 200 |
| E2E-KR-05 | 停用规则 | E2E-KR-01通过 | PUT /api/v1/knowledge-rules/{id} status=INACTIVE | 200 |
| E2E-KR-06 | 删除规则 | E2E-KR-01通过 | DELETE /api/v1/knowledge-rules/{id} | 204 |
| E2E-KR-07 | 创建重复规则编码 | 无 | POST 相同ruleCode | 409 Conflict |
| E2E-KR-08 | 规则触发验证 | 规则已激活 | 匹配条件的数据触发规则 | 动作执行成功 |

## 链路4: KYC洞察与声明 (9项)

| ID | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|----|----------|----------|----------|----------|
| E2E-KY-01 | 记录风险声明 | 存在客户上下文 | POST /api/v1/kyc-insights with claimType=RISK_ASSESSMENT | 201 |
| E2E-KY-02 | 记录合规声明 | 存在客户上下文 | POST /api/v1/kyc-insights with claimType=COMPLIANCE_CHECK | 201 |
| E2E-KY-03 | 查询洞察列表 | 至少1条洞察 | GET /api/v1/kyc-insights | 200 |
| E2E-KY-04 | 按客户过滤洞察 | 存在多条洞察 | GET /api/v1/kyc-insights?customerContextId=xxx | 200 |
| E2E-KY-05 | 查询声明详情 | E2E-KY-01通过 | GET /api/v1/kyc-insights/{id} | 200 |
| E2E-KY-06 | 声明调解-自动通过 | 冲突检测=false, 权威匹配=true | 调解结果=AUTO_APPROVED | |
| E2E-KY-07 | 声明调解-需人工审核 | 冲突检测=true | 调解结果=MANUAL_REVIEW | |
| E2E-KY-08 | 声明调解-降级处理 | 证据不完整 | 调解结果=DEGRADED | |
| E2E-KY-09 | 置信度评分验证 | 记录声明 | confidenceScore在[0,1]范围 | |

## 链路5: CRM回写与LLM降级 (9项)

| ID | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|----|----------|----------|----------|----------|
| E2E-CR-01 | CRM回写-HTTP模式 | CRM服务可用 | 触发CRM回写 | WritebackResult.success=true |
| E2E-CR-02 | CRM回写-日志降级 | CRM服务不可用 | 触发CRM回写 | 自动降级到LoggingChannel |
| E2E-CR-03 | CRM回写-重试 | CRM临时不可用 | 触发CRM回写 | 重试后成功或降级 |
| E2E-CR-04 | LLM调用-Real模式 | LLM服务可用 | engagement.llm.mode=real | 返回LLM生成内容 |
| E2E-CR-05 | LLM降级-Mock模式 | LLM服务不可用 | 自动降级 | 返回模板/正则内容 |
| E2E-CR-06 | LLM熔断器 | 连续5次失败 | 熔断器打开 | 后续请求直接降级 |
| E2E-CR-07 | LLM熔断恢复 | 熔断器半开 | 等待half-open-delay后 | 允许探测请求 |
| E2E-CR-08 | 领域事件发布 | 关键业务操作 | 执行操作后 | Spring Event已发布 |
| E2E-CR-09 | DMN引擎决策 | 声明调解请求 | 调用reconcile | 返回ReconciliationResult |

---

## 自动化执行

```bash
# 基础E2E验收
./scripts/e2e-acceptance.sh http://localhost:8080 YOUR_API_KEY

# 生产环境接口验证
./scripts/prod-verify.sh http://localhost:8080
```

## 通过标准

- 42项测试全部通过或N/A
- 0项FAIL
- 性能基线满足P95阈值
