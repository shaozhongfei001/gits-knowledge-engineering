# P22 失败记录

本文件记录 P22-llm-wiki-knowledge-map 各 Gate 的失败与根因。规则：失败先记录，再修复；修复后回填根因与修复方式。

## 当前状态

全部历史失败均已解决并回填根因（见下方各记录）。G0-G3 + E1 已完成并通过受控证据记录（contract_registration/generate/check/knowledge_architecture_check/element_read_gate/llm_read_map_gate 均 pass）。

> 备注 1：G3 验证期间发现 apps/api 既有 `@SpringBootTest` 上下文加载失败（`knowledge root not a directory`），根因为知识根目录用相对路径在模块工作目录不可达。已修复（`KnowledgeArchitectureConfig.resolveKnowledgeRoot` walk-up 解析）。
> 备注 2：受控证据 `record_gate.py` 要求 gate command 可执行。修正 `element_read_gate` 为 `./mvnw`（项目 Maven ≥3.9，系统 mvn 3.6.3 被 enforcer 拒绝）、`llm_read_map_gate` 为 KnowledgeWiki 相关测试命令。

## 20260819T125043Z｜element_read_gate（已解决）

- Command: `mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test`
- Exit: `1`
- Root Cause: 系统 Maven 3.6.3 不满足 enforcer 要求（≥3.9）；应使用项目 wrapper `./mvnw`。
- Fix: LOOP.yaml 该 gate command 改为 `./mvnw -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test`。
- Resolution: 重跑通过（EXIT=0）。

## 20260819T125200Z｜llm_read_map_gate（已解决）

- Command: `./mvnw -pl apps/api -am test -Dtest='KnowledgeWiki*' -Dsurefire.failIfNoSpecifiedTests=false`
- Exit: `1`
- Root Cause: `KnowledgeWikiFilesystemAdapterIT` 用相对路径 `specs/knowledge-architecture`，在 `apps/api -am` 从子模块目录运行时解析失败。
- Fix: IT 改为 walk-up 解析知识根目录（与 KnowledgeSnapshotLoaderIT 一致）。
- Resolution: 重跑 IT 通过。

## 20260819T125300Z｜llm_read_map_gate（已解决）

- Command: `./mvnw -pl apps/api -am test -Dtest='KnowledgeWiki*' -Dsurefire.failIfNoSpecifiedTests=false`
- Exit: `1`
- Root Cause: E1 `KnowledgeSnapshotLoader` 漏加载子目录 TASK 地图（`maps/corporate-rm/previsit-preparation.md`），且 `InMemoryKnowledgeStore.rootMap()` 硬编码 `"ROOT"` 键而实际根地图 mapId=`KM-GITS-ROOT`。
- Fix: loader 改为 `Files.walk(maps)` 直接解析每个地图文件（不依赖 mapId→路径约定）；`rootMap()` 按 `mapType=ROOT` 匹配。
- Resolution: 重跑 IT 全部通过（KnowledgeSnapshotLoaderIT 3/3 + KnowledgeWikiFilesystemAdapterIT 2/2），llm_read_map_gate EXIT=0。

## 20260819T125822Z｜backend_test

- Command: `make backend-test`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/backend_test-20260819T125822Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.

## 20260819T130658Z｜independent_qa

- Command: `make verify`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/independent_qa-20260819T130658Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.

## 20260819T130713Z｜independent_qa

- Command: `PYTHON=/home/szf/.workbuddy/binaries/python/versions/3.14.3/bin/python3 make verify`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/independent_qa-20260819T130713Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.

## 20260819T130744Z｜independent_qa

- Command: `PATH=/home/szf/.workbuddy/binaries/python/versions/3.14.3/bin:$PATH make verify`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/independent_qa-20260819T130744Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.

## 20260819T131109Z｜independent_qa

- Command: `PATH=/home/szf/.workbuddy/binaries/python/versions/3.14.3/bin:$PATH MAVEN_OPTS="-Ddependency.check.auto.update=false" make verify`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/independent_qa-20260819T131109Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.

## 20260819T131337Z｜independent_qa

- Command: `PATH=/home/szf/.workbuddy/binaries/python/versions/3.14.3/bin:$PATH MAVEN_OPTS="-Ddependency.check.auto.update=false" NPM_CONFIG_LEGACY_PEER_DEPS=true make verify`
- Exit: `2`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/independent_qa-20260819T131337Z.log`
- Classification: `PENDING_ROOT_CAUSE`
- Next: diagnose, record root cause, fix, rerun the original gate.
