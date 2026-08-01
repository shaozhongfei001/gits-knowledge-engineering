# P0｜工程开发包生成派工

| 字段 | 值 |
|---|---|
| packet | `HZB-KNO-DEV-P0` |
| status | `READY_FOR_INDEPENDENT_QA` |
| baseline | `specs/BASELINE_INDEX.yaml` |
| contract registry | `specs/CONTRACT_INDEX.yaml` |
| implementation actor | `engineering_generation_agent` |
| QA actor | `PENDING_INDEPENDENT_QA` |

## 客户可感知目标

项目能够在Ubuntu Java 21环境中以一致命令完成环境预检、多合同生成、合同检查、模块化后端构建、前端构建、安全检查和Loop证据检查；失败必须非零退出并留下可复核证据。

## 验收边界

- 本包验收工程机制，不验收真实银行业务功能；
- Oracle、AIOS、CRM、IAM和写回不在本包连接范围；
- 独立QA必须重跑关键命令并抽查红测，开发自检不得代签。
