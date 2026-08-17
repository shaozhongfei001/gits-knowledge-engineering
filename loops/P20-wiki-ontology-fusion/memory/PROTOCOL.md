# P20 Wiki-first＋Ontology融合｜Protocol

1. `specs/CONTRACT_INDEX.yaml`是唯一合同注册入口；
2. 合同变更先于生成物和实现，禁止手工编辑`generated/`；
3. Owner授权前只允许维护合同候选、ADR候选和审查证据；
4. 所有实现Gate失败先追加到`FAILURES.md`；
5. 开发只可记录`DEV_SELF_CHECK_PASS`，独立QA由不同Actor执行；
6. 当前P19与P20证据、Baton和状态严格隔离。
