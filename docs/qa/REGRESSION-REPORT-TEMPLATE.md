# 回归测试报告模板

> 项目: GITS知识工程 | 版本: V1.0

## 1. 基本信息

| 字段 | 值 |
|------|-----|
| 报告编号 | REG-YYYY-MMDD-NN |
| 测试日期 | |
| 测试环境 | □ Local □ Staging □ Production |
| 构建版本 | git commit: |
| 测试人员 | |
| 审核人员 | |

## 2. 测试范围

### 2.1 单元测试

| 模块 | 测试数 | 通过 | 失败 | 跳过 | 覆盖率 |
|------|--------|------|------|------|--------|
| operational-ontology | | | | | |
| semantic-runtime | | | | | |
| context-evidence | | | | | |
| human-action | | | | | |
| evaluation | | | | | |
| scenario-customer-journey | | | | | |
| scenario-hermes | | | | | |
| api | | | | | |
| worker | | | | | |
| **合计** | | | | | |

### 2.2 集成测试

| 测试场景 | 结果 | 备注 |
|----------|------|------|
| MySQL数据源连接 | □ PASS □ FAIL | |
| Flyway迁移 | □ PASS □ FAIL | |
| Oracle数据源连接(可选) | □ PASS □ FAIL □ N/A | |
| LLM接口调用 | □ PASS □ FAIL | |
| CRM接口调用 | □ PASS □ FAIL | |

### 2.3 E2E测试

| 链路 | 测试数 | 通过 | 失败 |
|------|--------|------|------|
| 客户上下文 | 8 | | |
| 客户旅程 | 8 | | |
| 知识规则 | 8 | | |
| KYC洞察 | 9 | | |
| CRM/LLM降级 | 9 | | |
| **合计** | 42 | | |

## 3. 性能测试

| 接口 | P50 | P95 | P99 | 目标P95 | 结果 |
|------|-----|-----|-----|---------|------|
| GET /api/v1/customer-contexts | | | | <500ms | □ PASS □ FAIL |
| POST /api/v1/engagement-journeys | | | | <2s | □ PASS □ FAIL |
| GET /api/v1/knowledge-rules | | | | <500ms | □ PASS □ FAIL |
| GET /api/v1/kyc-insights | | | | <500ms | □ PASS □ FAIL |

## 4. 安全测试

| 检查项 | 结果 | 备注 |
|--------|------|------|
| API Key认证 | □ PASS □ FAIL | |
| Swagger禁用 | □ PASS □ FAIL | |
| H2控制台禁用 | □ PASS □ FAIL | |
| CORS配置 | □ PASS □ FAIL | |
| 敏感信息不暴露 | □ PASS □ FAIL | |

## 5. 缺陷清单

| ID | 严重程度 | 描述 | 状态 |
|----|----------|------|------|
| | □P0 □P1 □P2 □P3 | | □ Open □ Fixed □ Deferred |

## 6. 结论

□ **PASS** — 全部测试通过，建议发布
□ **CONDITIONAL PASS** — 存在P2/P3缺陷，不影响核心功能，建议附条件发布
□ **FAIL** — 存在P0/P1缺陷，不建议发布

### 签署

| 角色 | 姓名 | 日期 |
|------|------|------|
| 测试人员 | | |
| 审核人员 | | |
| 项目经理 | | |
