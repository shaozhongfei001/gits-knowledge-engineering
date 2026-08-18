# 数据模型开发说明

1. **Canonical data is file-first**：V1.1包中的JSON/JSONL/CSV是场景SSOT；MySQL脚本只作为开发适配。
2. Customer与LegalEntity分开；集团简称不能直接做法人主键。
3. `OpportunitySignal`和`Opportunity`必须分表或至少分状态并有人工升级门禁。
4. `Commitment`与`Task`分开：前者表示双方承诺，后者表示银行内部行动。
5. `Claim`、`ClaimAssessment`、`Evidence`需要保留版本和原文引用。
6. `crm_writeback_command`不得与CRM正式业务表合并；它是受控副作用队列。
7. 大流水数据建议按日期/账户索引；演示环境可直接CSV加载。
