# PP-G03｜数据缺失、接口慢或外部源不可用时，系统不能靠“硬生成”装成完整

- Priority：P1
- Evidence：E3
- 推荐剧情时间：异常演示
- 推荐页面：PAGE-07

## 真实业务表达
真实银行系统一定会有数据没到、权限不够、接口超时，系统要知道什么时候该降级，而不是编一个答案。

## 剧情
外部行业源不可用时，访前报告明确降级：行内数据可用、行业竞争部分不可用，不得猜测补齐。

## 系统解决方向
Data Sufficiency + Degraded Mode + Manual Fallback。

## 需要的数据
数据可用性、freshness、coverage、接口状态

## 知识 / 对象 / Skill
- 知识：降级规则、No-Go Output
- 对象：DataAvailability, Coverage, DegradedState
- Skill：各Skill降级合同
- Runtime：Runtime Skill显式错误合同

## Human Gate / 边界
- Human：RM决定是否继续使用受限结果
- No-Go：数据缺失时模型不能猜测补全

## 验收
关键数据源失效时不产生伪完整结论，场景可受限继续

> 说明：本项为P1，其中E3内容不得在未完成用户访谈/制度确认前写成银行已确认现状。
