# P22 失败记录

本文件记录 P22-llm-wiki-knowledge-map 各 Gate 的失败与根因。规则：失败先记录，再修复；修复后回填根因与修复方式。

## 当前状态

截至 G3（llm_read_map_gate）完成时，无未解决的失败记录。

> 备注：G3 验证期间发现 apps/api 既有 `@SpringBootTest` 上下文加载失败（`knowledge root not a directory: specs/knowledge-architecture`），根因为知识根目录采用相对路径，在模块工作目录下无法解析。该问题为 E1 引入的既有缺陷（基线上 `d158a3f` 复现），已随 G3 一并修复（`KnowledgeArchitectureConfig.resolveKnowledgeRoot` 按 `KnowledgeSnapshotLoaderIT` 一致的 walk-up 方式解析），未作为 Gate 失败留存。
