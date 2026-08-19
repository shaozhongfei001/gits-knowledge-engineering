# P22 LLM-WIKI 知识地图｜Protocol

1. `specs/CONTRACT_INDEX.yaml` 是唯一合同注册入口；合同变更先于生成物和实现，禁止手工编辑 `generated/`。
2. 最高权威《银行知识工程规范打样_fixed.xlsx》：KI/KE/RUL/SK/T/DS 定义以规范为准，不发明结构。
3. 不引入 OpenWiki；增强 Git/文件系统承载权威源 + 控制面（机器读取）至生产可用。
4. 保留"人机共读知识地图/目录/契约"目标；真实平台选型（RAG/GraphDB/MetadataCatalog）留空待 Owner 指定。
5. 控制面读取为内存快照（启动加载），运行时高频读内存；fail-closed（加载失败拒绝启动）。
6. 所有实现 Gate 失败先追加到 `FAILURES.md`，回填根因再修。
7. 开发只可记录 `DEV_SELF_CHECK`；独立 QA 由不同 Actor 执行（P22 已由 independent_qa 记录 QA_PASS）。
8. 未授权前禁止生产 cutover/fusion/写回；禁止将 shadow 证据与正式输出混淆。
