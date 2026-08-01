# Architecture Decision Record Index

| ADR | 决策 | 状态 | 关键边界 |
|---|---|---|---|
| ADR-0001 | 可编译语义合同＋运行本体控制平面 | Accepted for engineering candidate | 关系库权威状态；RDF/搜索/向量为投影 |
| ADR-0002 | 模块化单体API＋独立Worker | Accepted for engineering candidate | 不提前引入微服务、Kafka、Temporal/Camunda |
| ADR-0003 | 生产关系数据库 | Open | 本地候选不反向决定生产数据库 |
| ADR-0004 | Apache Ossie | Conditional/Experimental | 仅交换Profile，不进核心和写回热路径 |
| ADR-0005 | 展示直接传递 | Accepted with boundary | 仅Experience/BFF可返回厂商展示对象 |
| ADR-0006 | Java 包名统一到 com.gien.gits | Proposed | 候选Proposal，待owner批准并单开loop；不阻断当前构建 |
| ADR-0007 | Oracle EDwCRM 只读访问启用 | Accepted with read-only boundary | DATA_OWNER已授权；只读强制；DBA/SECURITY_OWNER书面确认待补；专用loop P1-oracle-readonly |

正式版本、Owner、日期、替代方案、后果和验证证据将在对应ADR评审时补齐；本索引不能替代ADR批准。
