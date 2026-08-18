# 核心数据字典

## 客户与主体
- `Customer`：银行经营关系，不等于法人主体。
- `LegalEntity`：工商法人主体。
- `ProjectEntityRole`：项目中承担实施/借款/用款/还款/担保等角色。
- `BankCustomerRelationship`：银行与客户的关系投影。

## 经营事实
- `Account / DailyBalance / Transaction`：仅表示本行可见账户和交易。
- `Facility / Drawdown`：本行授信与提款。
- `ProductHolding`：客户在本行的产品持有。

## 客户经营知识
- `Interaction`：一次客户沟通事件。
- `Claim`：客户或来源表达的可核实陈述。
- `ClaimAssessment`：对Claim和证据的评估结果。
- `Need / Concern / Preference`：经营需求、顾虑和偏好。
- `Commitment`：客户或银行对外/对内的承诺。
- `Task`：银行内部应执行动作。
- `OpportunitySignal`：机会信号，尚未成为正式商机。
- `Opportunity`：经过人工确认的正式商机。

## Evidence
- `FACT`：可由权威数据或正式材料支持。
- `CLAIM`：客户/人员表达，必须保留speaker和原文。
- `FINDING`：分析结果，必须绑定Evidence和限制。
- `SIGNAL`：值得关注但未证实的机会/风险信号。
- `PENDING_CONFIRMATION`：缺少确认，必须带Open Question。

## Human Gate
任何对客发送、正式商机升级、CRM写回、录音授权和高风险专业结论都必须明确人工决策。

## 特别容易混淆
- Available Credit ≠ New Credit Need
- Project Entity ≠ Borrower Entity
- Claim ≠ Fact
- Commitment ≠ Task
- OpportunitySignal ≠ Opportunity
- Bankability ≠ Approval
