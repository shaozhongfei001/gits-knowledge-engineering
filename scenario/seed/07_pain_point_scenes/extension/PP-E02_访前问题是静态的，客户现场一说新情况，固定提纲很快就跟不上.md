# PP-E02｜访前问题是静态的，客户现场一说新情况，固定提纲很快就跟不上

- Priority：P1
- Evidence：E1
- 推荐剧情时间：14:35
- 推荐页面：PAGE-10

## 真实业务表达
客户突然说“项目可能放在子公司”，下一步该追问借款主体、担保还是账户安排，不能还照着原问题表念。

## 剧情
客户说项目更倾向子公司后，系统建议追问借/用/还/担保主体；只显示1-2个高价值问题，不干扰会谈。

## 系统解决方向
基于新Claim和Open Question动态生成补充问题，并支持关闭/稍后问。

## 需要的数据
当前Interaction、访前QuestionPlan、Ontology关系

## 知识 / 对象 / Skill
- 知识：追问规则、会中提示边界
- 对象：DynamicQuestion, OpenQuestion, Claim
- Skill：SP-05；SP-06
- Runtime：RSK-09, RSK-10

## Human Gate / 边界
- Human：RM决定问/跳过
- No-Go：不能实时弹大量问题干扰客户沟通

## 验收
仅在新信息改变关键判断时提示，RM可关闭

> 说明：本项为P1，其中E3内容不得在未完成用户访谈/制度确认前写成银行已确认现状。
