# 客户经理持续经营业务主链流程说明

> 配套图：`flow.html`（自包含 HTML/SVG）、`flow.png`（预览）
> 主题：客户经理持续经营 — 业务主链闭环
> 业务剧本：华东精工（演示场景，王磊-鑫达贸易 套用），V1.1
> 参考：`docs/dd/engagement-main-chain-flow-design.md`、`docs/dd/必须理解的业务主链_任务骨架.md`

---

## 一、业务闭环核心理念

> **一次客户 Interaction 不能随着 CRM 纪要结束而结束，而必须进入下一轮持续经营记忆。**

整个客户经营链不是"拜访生成报告"的单点场景，而是**从市场信号触发到下一轮持续经营**的完整闭环。每一轮 Interaction 都为下一轮沉淀证据、Claim、Commitment、Task，构成持续进化的客户经营记忆。

**客户**：华东精工装备集团有限公司（CUST-001，战略客户，RM 张伟，合作 8 年）
**触发**：5 条复合信号（存款下降 35% / 设备付款 +32% / 二期项目备案 4800 万 / 设备清单承诺逾期 / 客户意图"三年期结构"）
**时间窗**：2026-07-08 完整拜访日（主线），前后多日承接

---

## 二、流程分阶段概览

| 阶段 | 节点 | 核心活动 | 主要产物（本体对象） | 关键门禁 |
|------|------|----------|--------------------|----------|
| 一 · 经营触发 | ①② | 信号扫描、COV 组装 | OpportunitySignal、Customer Operating View | — |
| 二 · 访前准备 | ③④⑤⑥ | KYC 缺口、Mission、R1/R2、产品适配 | KycGapProfile、PrevisitReport R1/R2、ProductFit | HG-C01、HG-D01、HG-A01 |
| 三 · 会中互动 | ⑦⑧⑨⑩ | Interaction 抽取、事实对账、实时追问、离场 | Candidate Claim、EvidenceBundle、FactReconciliationCase、Commitments | HG-B02、HG-E01 |
| 四 · 离场承诺 | ⑪⑫⑬⑭ | 双方承诺、任务派发、访后分析、报告生成 | Commitment、Task、PostvisitAnalysis、Report(R4/R5-A/R5-B) | HG-C02 |
| 五 · 访后与持续经营 | ⑮⑯⑰ | CRM 写回、专业协同、R7/R8、新证据驱动 | ActionReceipt、NewEvidence、ClaimAssessment 更新 | HG-F01 |

---

## 三、阶段一 · 经营触发（节点 ①—②）

### 3.1 ① 市场慧眼 · 经营触发

**业务动作**：客户经理到岗，系统基于昨夜至凌晨的**外部事件、交易异动、承诺到期**等数据，生成今日经营队列。华东精工因 5 条复合信号进入 Top 1。

**信号来源**（与本体模型映射）：

| 信号 | 本体模型 | 物化表 / 加载 |
|------|----------|--------------|
| 存款下降 35% | `BankRelationshipSnapshot` | H2 `bank_relationship_snapshot`，按 `snapshot_month` 时序对比 |
| 设备供应商付款 +32% | `Transaction` | H2 `transaction`，近 45 天 vs 前 45 天聚合 |
| 二期项目备案 4800 万 | `ExternalEvent` | H2 `external_event`，`source_type=PROJECT_FILING` |
| 设备清单承诺逾期 | `Task` | H2 `task`，`due_date < CURRENT_DATE AND status=PENDING` |
| 客户希望看三年结构 | `Claim (INTENT)` | H2 `claim`，`claim_type=INTENT` |

**产物**：`OpportunitySignal × N` → 排序输出 Top N 客户队列（每条信号绑定客户、来源、强度、时间戳）

**关键约束**（No-Go）：
- **禁止**把项目备案或客户口中的"3000 万支持"直接写成正式融资需求（这是 Claim 而非 Fact）。
- 交易分析仅基于本行可见账户（**本行交易 ≠ 全量现金流**）。

### 3.2 ② 客户洞察 · Customer Operating View（COV）

**业务动作**：RM 打开华东精工的 COV，视图**不按"工商—账户—授信—产品"平铺**，而按业务视角组织：

| COV 区域 | 本体模型 | 加载 |
|----------|----------|------|
| 客户基本信息 | `Customer` | `JdbcCustomerRepository.findById("CUST-001")` |
| 授信与额度 | `CreditFacility` | `JdbcCreditFacilityRepository.findByCustomerId()` |
| 关系快照 | `BankRelationshipSnapshot` | 时序对比 |
| 集团结构 | `LegalEntity + GroupRelationship` | 集团树 |
| 交易流水 | `Transaction` | 滚动窗口聚合 |
| **已确认事实** | `Claim (VERIFIED_FACT)` | 过滤 |
| **待核实信号** | `Claim (CANDIDATE)` | 过滤 |
| 机会信号 | `OpportunitySignal` | 关联 |
| **未闭环承诺** | `Task (PENDING)` | 过滤 |
| KYC 缺口 | `KycGapProfile` | 加载 |

**本体区分**（核心规则，V1.1 强化）：
- `Project ≠ Borrower`（项目主体 ≠ 借款主体）
- `3000 万 ≠ 授信`（客户表达 ≠ 银行确认）
- `AI Finding ≠ 客户原话`（AI 推理 ≠ 客户原话）
- `Claim ≠ Fact`（主张 ≠ 事实，需对账+人工门禁）
- `Signal ≠ Opportunity`（信号 ≠ 商机，需 HG-A03 升级门禁）

---

## 四、阶段二 · 访前准备（节点 ③—⑥）

### 4.1 ③ KYC Gap · 访前准备

**业务动作**：扫描客户 KYC 信息缺口，包括：
- 实际控制人 / 受益人识别信息
- 经营许可 / 行业资质时效
- 实缴资本 / 股权结构变更
- 重大关联关系披露

**产物**：`KycGapProfile`（按对象问题：CFO / 采购 / 财务总经理）状态机：`UNKNOWN → CONFLICTING → PARTIAL → COMPLETE`

**本体对象**：`KycGapProfile`、`VerificationCase`

### 4.2 ④ 访前 Mission · R1 报告

**业务动作**：基于 COV + KYC Gap + 信号，AI 生成结构化访前报告（R1），包含：
- 客户画像（基于 `Customer`/`CreditFacility`）
- 经营洞察（基于 `BankRelationshipSnapshot`/`Transaction`）
- 风险提示（基于 `ExternalEvent`/KYC 冲突）
- 建议话题清单

**产物**：`PrevisitReport R1`，含主目标、副目标、No-Go 规则、证据链接

### 4.3 ⑤ 60 秒作战卡 · R2

**业务动作**：基于 R1 生成 60 秒速读卡：
- 一句话开场（基于客户关系记忆）
- 3 个关键问题（基于 R1 建议话题）
- 风险触发词（基于 R1 风险提示）
- 收尾话术（基于客户偏好）

**产物**：`PrevisitReport R2`

### 4.4 🚧 HG-C01 访前面谈 Mission 确认

**门禁语义**：客户经理必须**显式确认** Mission（R1+R2）才能进入会面阶段。
**决策选项**：ACCEPT（按建议执行）/ EDIT（修改主目标/副目标）/ REJECT（推迟或重新规划）
**记录**：`AuditLog` 写入 `actorId / confirmedAt / mission_id / decision`

### 4.5 ⑥ 产品适配 · 客户触达

**业务动作**：
1. **产品匹配**（`scenario-customer-journey.matchProduct`）：基于 InsightClaim + 产品库，AI 推荐候选产品（如"智造二期固贷 3 年期"），输出 `ProductCandidateClaim`，含 `fitScore` 与 `matchReason`。
2. **Outreach 草稿生成**：基于产品适配与客户历史触达偏好，AI 生成客户触达草稿。

### 4.6 🚧 HG-D01 产品推荐确认

**门禁语义**：AI 推荐产品（`ProductFit`）必须经客户经理**逐条**人工确认才能进入触达。**无门禁不执行副作用**。
**决策选项**：ACCEPT（按推荐）/ EDIT（调整产品/金额/期限）/ REJECT（放弃本产品）
**记录**：产品 ID、版本、决策、操作者、时间戳。

### 4.7 🚧 HG-A01 触达消息确认

**门禁语义**：所有**对外客户消息**（短信、微信、企业邮件、电话话术）必须经人工确认。AI 草稿可编辑；草稿确认后通过 `ActionDispatchPort.send` 发送，**不可逆**前必须**留下 audit log**。
**No-Go**：未授权的群发、未声明的合规话术、未经确认的承诺性措辞。

---

## 五、阶段三 · 会中互动（节点 ⑦—⑩）

### 5.1 ⑦ 会中 Interaction · 事实抽取

**业务动作**：客户经理与客户面对面沟通。会中助手实时提供：
- **追问建议**（基于 R1/R2 风险点 + COV 缺口）
- **Claim 抽取**：从客户原话抽取 `Claim (CANDIDATE)`，类型含 INTENT / NEED / CONCERN / FACT-EXPRESSION
- **Evidence 记录**：按录音授权状态分级
  - 录音授权 GRANTED → 录音转写 → `evidenceType=RECORDING`（可作为 FACT）
  - 录音未授权 + 笔记 → `evidenceType=NOTE`（仅可作为 CLAIM）
  - 录音未授权 + 访后口述 → `evidenceType=DEBRIEF`（仅可作为 SIGNAL）

**产物**：
- `Candidate Claim × N`（客户原话直接抽取，不带 AI 推理）
- `EvidenceBundle`（含 recordingMode / evidenceType / version）

**核心约束**：
- `meeting_gold_transcript` **仅作测试**，不作为生产输入
- `AI Finding ≠ 客户原话` —— 任何 AI 推理必须独立标注 `isAiFinding=true`，与客户原话区分

### 5.2 🚧 ⑧ Fact Reconciliation · 事实对账

**业务动作**：对会中抽取的 Claim 与**权威源**（外部事件、合同、财报）做对账。

**决策（DMN：CTR-RULE-001）**：

| conflictDetected | authoritativeMatch | evidenceComplete | reconciliationStatus |
|---|---|---|---|
| true | — | — | `CONFLICT_REQUIRES_HUMAN_REVIEW` |
| false | true | true | `VERIFIED_FACT` |
| false | — | — | `CANDIDATE_CLAIM` |

**四维校验示例**（华东精工场景）：
- 3000 万（客户口中"支持"）↔ 集团授信 4000 万可用额度（**不一致，本行交易 ≠ 全量现金流**）
- 4000 万（账面可用）↔ 实际可提款（**需澄清提款条件**）
- 4800 万（项目备案总投资）↔ 银行融资需求（**不等于**）
- 3280 万（设备清单）↔ 融资金额（**不等于**）

### 5.3 🚧 HG-B02 事实对账确认

**门禁语义**：所有 `CONFLICT_REQUIRES_HUMAN_REVIEW` 状态的 Claim 必须经客户经理**逐条**确认才能进入离场。
**决策选项**：追问（追加 Evidence）/ 保留待核实 / 人工确认（升级为 `VERIFIED_FACT` 或标注为 `CONFLICTING`）

### 5.4 ⑨ 会中助手 · 实时追问建议

**业务动作**：基于 COV 缺口 + 对账结果，AI 提示"建议追问 XX / 风险触发词 YY / 关联事件 ZZ"。
**重要**：会中助手**只提示**，**不替客户经理提问**——客户经理是唯一对外发声主体。

### 5.5 ⑩ 离场确认

**业务动作**：会面结束前，客户经理与客户做**离场清单确认**：
- 本次会议共识（Claim 升 VERIFIED_FACT）
- 客户承诺（`Commitment`）
- 银行承诺（`Commitment`）
- 下次预约
- 任务派发

### 5.6 🚧 HG-E01 离场确认

**门禁语义**：离场清单是**会面向正式记录的转化点**。无 HG-E01 确认，本场 Interaction 仅作 SIGNAL（不能进入访后分析）。
**副作用触发**：HG-E01 确认后 → 触发 CRM 写回编排（R4 / R5-A / R5-B）。

---

## 六、阶段四 · 离场承诺（节点 ⑪—⑭）

### 6.1 ⑪ Commitment · 双方承诺

**业务动作**：登记本次会面形成的双方承诺。
**关键本体区分**：`Commitment ≠ Task`
- `Commitment` 是**双向承诺**（客户承诺提供 / 银行承诺审批），状态机 `DRAFT → CONFIRMED → IN_PROGRESS → FULFILLED / BROKEN`
- `Task` 是**单向内部任务**（产品专家 / 风险经理 / 合规顾问 派发），独立追踪

**产物**：`Commitment × N`（客户承诺 / 银行承诺分类标签）

### 6.2 ⑫ Task · 任务派发

**业务动作**：基于会面共识和承诺缺口，AI 建议任务派发（产品专家 / 风险经理 / 合规顾问 / 客户经理本人）。
**产物**：`Task × N`，含 owner、dueDate、priority、status

### 6.3 ⑬ 访后分析 · R4 内部正式报告

**业务动作**：基于完整事实链（Evidence + 对账 + 本体区分），生成内部正式报告 R4。
**多出口**：
- R4 内部版（完整事实链、证据引用、对账结果）
- R5-A CRM 短版（脱敏精简）
- R5-B 专业 Fact Pack（原始证据 + 对账 + 本体区分）

### 6.4 ⑭ R5-A CRM 短版 · R5-B Fact Pack

**业务动作**：从 R4 派生两份差异化产出：
- R5-A：适合 CRM 存储的脱敏版（无客户身份证号、卡号等敏感字段）
- R5-B：专业协同用的 Fact Pack（保留完整证据链与本体区分标注）

### 6.5 🚧 HG-C02 报告审批

**门禁语义**：R4 报告正式发布前必须经**报告审批人**确认，确保事实链可追溯、本体区分准确、敏感字段已脱敏。
**记录**：报告 ID、审批人、时间戳、决策、未决项（如有）。

---

## 七、阶段五 · 访后与持续经营（节点 ⑮—⑰）

### 7.1 ⑮ CRM 受控写回

**业务动作**：基于 R5-A 短版，将本次会面纪要通过 `CrmWritebackChannel` 写入 CRM 系统。
**关键约束**：
- **逐条审批**：写回命令的每条 field 单独 Confirm / Edit / Reject
- **无 HG 不执行副作用**：`ControlledActionService.dispatch(CrmWritebackCommand, humanGateRef)`，无 humanGateRef 抛 `ControlledActionRejectedException`
- **幂等性**：写回命令含 `idempotencyKey`，重试不产生重复

### 7.2 🚧 HG-F01 CRM 写回确认

**门禁语义**：CRM 写回是**不可逆外部副作用**。客户经理必须**逐条**审批写回命令，确认无误后系统才通过 `CrmWritebackChannel` 发送。
**审计**：每个 `ActionReceipt` 包含 `actorId / confirmedAt / commandId / channelResponse`，写入 `AuditLogPort`。

### 7.3 ⑯ 专业协同 · 产品/风险/合规

**业务动作**：基于 R5-B Fact Pack 和 Task 派发，专业部门（产品 / 风险 / 合规）协同：
- 产品专家：基于产品适配 + 客户需求，输出**两套融资路径**（如 固贷 + 流贷 / 固贷 + 银承）
- 风险经理：基于 KYC 缺口 + 主体区分，输出**风险评估**
- 合规顾问：基于承诺与触达，输出**合规检查**

**产物**：各专业部门输出物（ProductPath / RiskAssessment / ComplianceCheck）作为 `Task` 完成结果。

### 7.4 ⑰ R7 · R8 · 新证据驱动下一轮

**业务动作**：本轮完成后，进入下一轮持续经营：
- **R7 下次访前报告**：基于 R4/R5 + 新 Evidence 生成，包含遗留问题、建议目标
- **R8 证据版本链**：V1 → V2 → V3，每个版本变化和影响可追溯
- **新 Evidence 驱动**：本轮 ClaimAssessment 更新、Commitment 状态推进、Task 闭环率统计

**关键转变**：
- `ClaimAssessment` 更新：所有 Claim 的状态（VERIFIED_FACT / CONFLICTING / CANDIDATE）随新证据更新
- **持续经营不是新任务**，而是**同一客户旅程的下一相位**
- 状态机：`CustomerJourney` 从 `KYC_COLLECT` 推进到 `PRE_VISIT` / `IN_MEETING` / `POST_VISIT` / `FOLLOW_UP` / `CLOSED`

**闭环回标**：新证据驱动回到 ① 市场慧眼，触发下一轮经营（如：客户已确认三年期方向 → 触发"看产品版本"信号 → 进入下一轮 R7/R8 准备）

---

## 八、人工门禁（Human Gate）总览

按 V1.1 规范，所有受控动作必须经 15 类门禁（**HG-A/B/C/D/E/F** 系列）。本主链涉及：

| 门禁 | 触发环节 | 决策范围 | 责任主体 |
|------|----------|----------|----------|
| HG-A01 | 客户触达消息 | 消息草稿 accept/edit/reject | 客户经理 |
| HG-A03 | 商机升级（Signal→Opportunity） | 信号确认 | 客户经理 |
| HG-B02 | 事实对账 | CONFLICT/VERIFIED/CANDIDATE 逐条 | 客户经理 |
| HG-C01 | 访前 Mission | Mission 定义 accept/edit/reject | 客户经理 |
| HG-C02 | R4 报告审批 | 报告完整性、合规性 | 报告审批人 |
| HG-D01 | 产品推荐 | ProductFit accept/edit/reject | 客户经理 |
| HG-E01 | 离场确认 | 承诺清单、下次预约 | 客户经理 |
| HG-F01 | CRM 写回 | 写回命令逐条 | 客户经理 |

**无 HG 不执行副作用**：`ControlledActionService` 在没有 `humanGateRef` 时抛 `ControlledActionRejectedException`，由全局 `@ExceptionHandler` 统一返回 422。

---

## 九、核心本体对象（Domain Object 总览）

| 对象 | 状态机 | 关键区分 |
|------|--------|----------|
| `OperatingCase` | `OPEN → IN_PROGRESS → WAITING_FOR_HUMAN → IN_PROGRESS → CLOSED` | 案件主实体 |
| `CustomerJourney` | `KYC_COLLECT / PRE_VISIT / IN_MEETING / POST_VISIT / FOLLOW_UP / CLOSED` | 旅程主实体 |
| `Claim` | `CANDIDATE → VERIFIED_FACT / CONFLICTING / REJECTED` | 主张（CANDIDATE 仅是 AI 候选，VERIFIED_FACT 必须有 Evidence+对账+门禁）|
| `Commitment` | `DRAFT → CONFIRMED → IN_PROGRESS → FULFILLED / BROKEN` | 双向承诺（客户承诺 ≠ 银行承诺）|
| `Task` | `CREATED → ASSIGNED → IN_PROGRESS → COMPLETED / CANCELLED` | 内部任务（与 Commitment 独立）|
| `OpportunitySignal` | `DETECTED → QUALIFIED → PROMOTED / REJECTED` | 信号（PROMOTED 才升 Opportunity）|
| `Opportunity` | `DRAFT → CONFIRMED → IN_PROGRESS → WON / LOST` | 商机（由 Signal 升，需 HG-A03）|
| `Evidence` | V1→V2→V3 版本链 | 证据（recordingMode: GRANTED/DECLINED）|
| `FactReconciliationCase` | `OPEN → DMN_DECIDED → HUMAN_CONFIRMED → CLOSED` | 事实对账案件 |
| `HumanGate` | `PENDING → CONFIRMED / REJECTED / EXPIRED` | 人工门禁（与 AuditLog 一一对应）|

---

## 十、关键业务原则（红线）

1. **客户原话优先**：任何 AI 推理（Finding）必须与客户原话（Claim）独立标注，禁止混淆。
2. **本体区分**：`Project ≠ Borrower`、`3000 万 ≠ 授信`、`4000 万 ≠ 提款`、`4800 万 ≠ 融资`、`3280 万 ≠ 融资金额` —— 五区分贯穿全链。
3. **本行可见 ≠ 全量**：交易分析、现金流分析仅基于本行账户，禁止推论客户在他行的情况。
4. **无 HG 不执行**：`ControlledAction` 必须有 `humanGateRef`。
5. **测试脚本与生产隔离**：`meeting_gold_transcript` 等测试脚本不进入生产输入路径。
6. **失败先记录**：任何流程异常先写 `FAILURES.md` 再修复，禁止静默吞异常。
7. **证据不可篡改**：`Evidence` 一旦生成不可修改，只能新增版本（V1→V2→V3），所有版本链可追溯。

---

## 十一、参考

- 详细剧情：`docs/dd/engagement-main-chain-flow-design.md`（华东精工 7 月 8 日完整拜访）
- 任务骨架：`docs/dd/必须理解的业务主链_任务骨架.md`
- V1.1 验收：`docs/V1.1_ACCEPTANCE_REPORT.md`
- 编排器源码：`modules/scenario-customer-journey/src/main/java/com/gien/gits/customerjourney/CustomerJourneyOrchestrator.java`
- 状态机：`modules/scenario-customer-journey/src/main/java/com/gien/gits/customerjourney/OperatingCaseStateMachine.java`
- DMN 决策：`generated/rules/claim-reconciliation.normalized.dmn`（CTR-RULE-001）
- 受控动作：`modules/human-action/src/main/java/com/gien/gits/action/ControlledActionService.java`
