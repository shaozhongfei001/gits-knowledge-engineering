# Scene → Screen → Skill → API 映射

| Scene/Event | Screen | Skill/Policy | API | Human Gate |
|---|---|---|---|---|
| EVT-002 | PAGE-01 | SP-01 | GET /api/v1/rm/day | RM可调整队列 |
| EVT-003 | PAGE-03 | SP-02 | GET /api/v1/customers/{id}/operating-view | 权限上下文 |
| EVT-006 | PAGE-06 | SP-05 | GET /api/v1/customers/{id}/kyc-gap | RM选择问题 |
| EVT-008 | PAGE-07 | SP-04+SP-10 | POST /api/v1/visits/prep | HG-C01/HG-C02 |
| EVT-009 | PAGE-08 | SP-15 | 产品知识查询/内部接口 | HG-D01/HG-D03 |
| EVT-013 | PAGE-09 | SP-03 | POST /api/v1/outreach/draft | RM确认发送 |
| EVT-018 | PAGE-10 | Consent Policy | Interaction采集入口 | HG-E04 |
| EVT-022 | PAGE-11 | SP-07+SP-13 | POST /api/v1/reconciliation | HG-E01 |
| EVT-028 | PAGE-12 | SP-06 | POST /api/v1/commitments/exit-confirm | HG-E03 |
| EVT-031 | PAGE-13 | SP-06+SP-09 | POST /api/v1/interactions/extract | HG-F01/F02/F03 |
| EVT-032 | PAGE-14 | SP-10+SP-15+SP-17 | POST /api/v1/reports/post-visit | HG-F04 |
| EVT-033 | PAGE-15 | Writeback Policy | POST /api/v1/writeback/commands | HG-F05 |
| EVT-036 | PAGE-16 | SP-07+SP-08+SP-10 | POST /api/v1/evidence/ingest | HG-F06 |
