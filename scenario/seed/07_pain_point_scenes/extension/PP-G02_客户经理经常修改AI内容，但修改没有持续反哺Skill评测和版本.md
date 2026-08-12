# PP-G02｜客户经理经常修改AI内容，但修改没有持续反哺Skill评测和版本改进

- Priority：P1
- Evidence：E2
- 推荐剧情时间：7月20日
- 推荐页面：PAGE-18

## 真实业务表达
如果每次都是客户经理手工改完就算了，同类错误下次还会再来。

## 剧情
张伟把正式Opportunity建议EDIT为Signal，这个反馈进入Eval Case并绑定Skill版本。

## 系统解决方向
Accept/Edit/Reject→Run Metadata→Eval Case→Regression→Release/Limit/Rollback。

## 需要的数据
用户编辑、Skill版本、模型/Prompt版本、Outcome

## 知识 / 对象 / Skill
- 知识：评测集、错误分类、发布标准
- 对象：EvaluationCase, RunMetadata, Version, HumanFeedback
- Skill：平台治理能力
- Runtime：Skill/Agent发布与回滚Workflow

## Human Gate / 边界
- Human：Skill Owner决定发布/回滚
- No-Go：用户反馈不能直接自动改生产规则

## 验收
关键Skill每次发布有回归证据和Owner签署

> 说明：本项为P1，其中E3内容不得在未完成用户访谈/制度确认前写成银行已确认现状。
