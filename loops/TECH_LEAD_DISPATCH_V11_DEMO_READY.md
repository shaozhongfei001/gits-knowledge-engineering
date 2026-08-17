# Tech Lead 派工单：V1.1 演示就绪修复

> 优先级：P0 | 目标：100% 剧情环节可演示 | 截止：2026-08-12

## 背景

V1.1 螺旋迭代旅程（Previsit → Postvisit → Product Recommend）核心流程约 70% 可演示，存在 3 类阻塞问题需协作修复。

---

## 阻塞问题清单

### 🔴 B1: 人工门禁全线 400（GateType 枚举不匹配）

**现象**：`GET /api/v1/human-gates` 返回 400
```
No enum constant com.gien.gits.ontology.GateType.D01_EVIDENCE_BUNDLE
```

**根因**：种子数据 `V016__v11_seed_data.sql` 中使用了已废弃的 GateType 枚举值。SQL 已在本地修复（`B01_KYC_INSIGHT→B01_CONTEXT_ENRICH`, `C01_ENGAGEMENT_PLAN→C01_PREVISIT_APPROVE`, `D01_EVIDENCE_BUNDLE→D01_PRODUCT_RECOMMEND`），但当前运行实例未重启。

**影响范围**：
- `GET /api/v1/human-gates` — 400
- `GET /api/v1/human-gates/{gateId}` — 400
- `POST /api/v1/human-gates/{gateId}/decide` — 400
- 审计追踪中引用了旧枚举值的 JSON 快照

**修复方案**：
1. 确认 `V016__v11_seed_data.sql` 中 3 处 GateType 替换 + 2 处 audit_trace JSON 修复已提交
2. 重启后端服务（H2 内存库会重新执行 Flyway）
3. 验证 `GET /api/v1/human-gates` 返回 200

**指派**：Feature Pilot（验证 SQL 修复 + 重启确认）

---

### 🔴 B2: 信号确认/驳回 500（confirmSignal / dismissSignal 内部错误）

**现象**：
- `POST /api/v1/engagement/signal/{signalId}/confirm` → 500 `INTERNAL_ERROR`
- `POST /api/v1/engagement/signal/{signalId}/dismiss` → 500 `INTERNAL_ERROR`

**根因分析**（需深入排查）：
- Controller 层代码正确（`KycInsightController.confirmSignal` → `kycInsightService.confirmSignal(UUID.fromString(signalId))`）
- Service 层代码简单（`signalRepo.updateStatus(signalId, SignalStatus.CONFIRMED)`）
- `JdbcOpportunitySignalRepository.updateStatus` SQL：`UPDATE opportunity_signal SET status = ?, confirmed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE signal_id = ?`
- **KycInsightService 通过 `@Bean` 方式创建（`new KycInsightService(...)`），`@Transactional` 注解不生效**，但 JdbcTemplate.update 不需要事务上下文
- GlobalExceptionHandler 吞了异常堆栈（`server.error.include-stacktrace` 未配置），无法看到具体错误

**排查步骤**：
1. 临时在 `application.yaml` 添加 `server.error.include-stacktrace: always` 和 `server.error.include-message: always`
2. 重启服务，复现信号确认 500，查看完整堆栈
3. 根据堆栈定位根因（可能是 SQL 执行错误、类型转换错误、或 Bean 注入问题）

**指派**：Feature Pilot（排查 + 修复）

---

### 🔴 B3: 前端开发服务器未启动

**现象**：`localhost:5173` 无服务

**影响**：无法演示前端界面

**修复方案**：
1. `cd frontend && npm install && npm run dev`
2. 确认 Vite 代理配置正确（`/api` → `localhost:8080`）
3. 如后端在 8888 端口，需修改 `frontend/vite.config.ts` 的 proxy target

**指派**：Feature Pilot（前端启动 + 代理配置）

---

### 🟡 M1: 前后端 API 路径漂移（6 处不一致）

**现象**：前端 `engagement.ts` 中部分 API 路径与后端 Controller 不匹配

| 前端路径 | 后端实际路径 | 状态 |
|---------|------------|------|
| `/customer` | `/api/v1/engagement/customer` | ⚠️ 前端 baseURL 已包含 `/api/v1/engagement`，实际匹配 |
| `/api/journey/${journeyId}` | `/api/journey/{journeyId}` | ⚠️ 前端硬编码了 `/api` 前缀 |
| `/api/journey/case/${customerId}` | `/api/journey` (POST /open) | ❌ 路径+方法不匹配 |
| `/api/case` | `/api/case` | ✅ 匹配 |
| `/api/claim/case/${caseId}` | `/api/claim/case/{caseId}` | ✅ 匹配 |
| `/api/evaluation/${caseId}` | `/api/evaluation/{caseId}` | ✅ 匹配 |
| `/api/interaction` | `/api/interaction` | ⚠️ 后端只有 `GET /{interactionId}`，无列表端点 |

**关键问题**：
1. **前端 `engagement.ts` 混用了两种路径前缀**：部分用 baseURL 相对路径（`/customer`），部分硬编码绝对路径（`/api/journey/...`）
2. **InteractionController 缺少列表查询端点**：前端调用 `GET /api/interaction` 但后端只有 `GET /api/interaction/{interactionId}`
3. **CustomerJourneyController 路径不匹配**：前端调用 `GET /api/journey/case/${customerId}` 但后端是 `POST /api/journey/open`

**修复方案**：
1. 统一前端 API 路径风格：全部使用 baseURL 相对路径或全部使用绝对路径
2. 后端 `InteractionController` 添加 `GET /api/interaction?caseId=` 列表端点
3. 前端 `fetchJourneyByCustomer` 改为调用正确的端点

**指派**：集成工程师（前后端契约对齐）

---

### 🟡 M2: KYC 缺口画像无种子数据

**现象**：`GET /api/v1/engagement/kyc/CUST-TEST-001/gap-profile` 返回 404

**根因**：V016 种子数据中未包含 KYC 缺口画像记录

**修复方案**：在 `V016__v11_seed_data.sql` 中添加 KYC 缺口画像种子数据

**指派**：Feature Pilot

---

### 🟡 M3: 访后复盘 NPE（rawTranscript 为空时）

**现象**：当 `rawTranscript` 为空字符串时，`SemanticPatternExtractionStrategy.extract()` 中 regex 匹配 null 的 `text` 字段导致 NPE

**根因**：`SemanticPatternExtractionStrategy` 的 `extract()` 方法未对 `transcript.text()` 做 null 检查

**修复方案**：在 `extract()` 方法开头添加 null-guard：
```java
if (transcript == null || transcript.text() == null || transcript.text().isBlank()) {
    return ExtractionResult.empty();
}
```

**指派**：Feature Pilot

---

### 🟢 L1: GlobalExceptionHandler 吞异常堆栈

**现象**：所有 500 错误只返回 `{"errorCode":"INTERNAL_ERROR","message":"An unexpected error occurred"}`，无法排查

**修复方案**：
1. `application.yaml` 添加：
```yaml
server:
  error:
    include-stacktrace: always
    include-message: always
```
2. 开发环境启用，生产环境关闭

**指派**：Feature Pilot

---

## 工作包分配建议

### 工作包 1: 后端阻塞修复（Feature Pilot）
- B1: 确认 GateType SQL 修复 + 重启验证
- B2: 排查信号确认 500 + 修复
- M2: 添加 KYC 种子数据
- M3: 修复 SemanticPatternExtractionStrategy null-guard
- L1: 启用错误堆栈输出

### 工作包 2: 前端启动 + 契约对齐（集成工程师）
- B3: 启动前端开发服务器
- M1: 修复前后端 API 路径漂移（6 处）

### 工作包 3: E2E 验证（E2E Owner）
- 工作包 1+2 完成后，执行全量演示验证
- 验证所有 29 个剧情环节均可演示

---

## 验证标准

所有以下端点必须返回 2xx：

| # | 剧情环节 | 端点 |
|---|---------|------|
| 1 | 客户列表 | `GET /api/v1/engagement/customer?rmId=ALL` |
| 2 | 经营视图 | `GET /api/v1/engagement/customer/{id}/operating-view` |
| 3 | KYC缺口画像 | `GET /api/v1/engagement/kyc/{id}/gap-profile` |
| 4 | 旅程启动 | `POST /api/v1/engagement/journey/start` |
| 5 | 访前准备 | `POST /api/v1/engagement/journey/{id}/previsit` |
| 6 | 外联脚本 | `POST /api/v1/engagement/journey/outreach-script` |
| 7 | 会面脚本 | `POST /api/v1/engagement/journey/meeting-script` |
| 8 | 访后复盘 | `POST /api/v1/engagement/journey/{id}/postvisit` |
| 9 | 证据迭代 | `POST /api/v1/engagement/journey/{id}/new-evidence` |
| 10 | 机会信号 | `GET /api/v1/engagement/signal/{caseId}` |
| 11 | 信号确认 | `POST /api/v1/engagement/signal/{id}/confirm` |
| 12 | 信号驳回 | `POST /api/v1/engagement/signal/{id}/dismiss` |
| 13 | 产品匹配 | `POST /api/v1/engagement/customer/{id}/product-matching` |
| 14 | 旅程完成 | `POST /api/v1/engagement/journey/{id}/complete` |
| 15 | 人工门禁列表 | `GET /api/v1/human-gates` |
| 16 | 人工门禁详情 | `GET /api/v1/human-gates/{id}` |
| 17 | 人工门禁决策 | `POST /api/v1/human-gates/{id}/decide` |
| 18 | 承诺列表 | `GET /api/v1/commitments` |
| 19 | 承诺完成 | `PUT /api/v1/commitments/{id}/status` |
| 20 | CRM回写列表 | `GET /api/v1/crm/writeback-commands` |
| 21 | CRM回写决策 | `POST /api/v1/crm/writeback-commands/{id}/decide` |
| 22 | 审计追踪 | `GET /api/v1/audit-trace` |
| 23 | 证据版本 | `GET /api/v1/evidences/{id}/versions` |
| 24 | 主张查询 | `GET /api/claim/case/{caseId}` |
| 25 | 交易流水 | `GET /api/v1/engagement/customer/{id}/transactions` |
| 26 | 外联脚本列表 | `GET /api/v1/engagement/journey/outreach-scripts` |
| 27 | 会面脚本列表 | `GET /api/v1/engagement/journey/meeting-scripts` |
| 28 | 交互记录 | `GET /api/interaction?caseId=` |
| 29 | 架构状态 | `GET /api/v1/architecture/status` |

前端：`localhost:5173` 可访问，Dashboard + EngagementWorkspace 页面正常渲染。
