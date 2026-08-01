# Architecture Decision Record Index

| ADR | 决策 | 状态 | 关键边界 |
|---|---|---|---|
| ADR-0001 | 可编译语义合同＋运行本体控制平面 | Accepted for engineering candidate | 关系库权威状态；RDF/搜索/向量为投影 |
| ADR-0002 | 模块化单体API＋独立Worker | Accepted for engineering candidate | 不提前引入微服务、Kafka、Temporal/Camunda |
| ADR-0003 | 生产关系数据库 | Open | 本地候选不反向决定生产数据库 |
| ADR-0004 | Apache Ossie | Conditional/Experimental | 仅交换Profile，不进核心和写回热路径 |
| ADR-0005 | 展示直接传递 | Accepted with boundary | 仅Experience/BFF可返回厂商展示对象 |
| ADR-0006 | Java 包名统一到 com.gien.gits | Accepted | owner批准；com.gientech.hzb.kno → com.gien.gits（仅Java包） |
| ADR-0007 | Oracle EDwCRM 只读访问启用 | Accepted with read-only boundary | DATA_OWNER已授权；只读强制；专用loop P1-oracle-readonly |
| ADR-0008 | 领域前缀与标识 hzb→gits | Accepted | owner批准；合同/URI/事件/制品名统一 gits；历史证据日志不回写 |
| ADR-0009 | gits:Customer Spike 权威源 A_ZHCX_CUST_BASE | Accepted spike_only | data-mapping-owner确认；不引入核心本体；专用loop P3 |

正式版本、Owner、日期、替代方案、后果和验证证据将在对应ADR评审时补齐；本索引不能替代ADR批准。
