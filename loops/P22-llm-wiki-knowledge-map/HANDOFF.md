# P22 HANDOFF（G3 → 下一批）

| 字段 | 值 |
|---|---|
| **Loop** | P22-llm-wiki-knowledge-map |
| **Wave** | W1 |
| **From** | feature-pilot-g3（feature_pilot 角色） |
| **Gate 完成** | llm_read_map_gate（G3：大模型优先读图） |
| **状态** | DEV_SELF_CHECK（非 QA_PASS） |
| **证据** | EV-P22-G3-001（EVIDENCE.md）+ EVIDENCE.json |

## 已完成（本包）

1. `modules/knowledge-architecture/.../port/KnowledgeWikiPort.java` — 渲染 Port（renderMap/renderKnowledgeItem/renderElement），fail-closed 空文本。
2. `adapters/knowledge-filesystem/.../KnowledgeWikiFilesystemAdapter.java` — 从 InMemoryKnowledgeStore 渲染 LLM 可读受控地图（含 [AUTHORITATIVE]），KI→KE 分层导航。
3. `apps/api/.../config/KnowledgeArchitectureConfig.java` — 装配 `KnowledgeWikiPort` bean + 知识根路径 CWD 无关解析（修复既有 apps/api 上下文测试失败）。
4. `apps/api/.../service/KnowledgeWikiService.java` — 读图→systemPrompt→LlmClient.complete，LLM 失败 fallback 模板。
5. 测试：KnowledgeWikiFilesystemAdapterTest(7) + IT(2) + KnowledgeWikiServiceTest(4)；`make check` 全绿；`-pl modules/knowledge-architecture,adapters/knowledge-filesystem,apps/api -am test` 全通过（apps/api 321）。

## 未完成（转下一 holder）

- **G4 P22 shadow E2E**：`scripts/run_p22_shadow_e2e.py --mode shadow`，产 replayable shadow evidence。本轮未做。
- **G5 独立 QA**：`make verify` + `make backend-test`（含 JaCoCo 覆盖率 ≥ 0.80）。

## 建议 next holder

tech_lead 或 e2e_owner 接棒 G4（shadow E2E），完成后再交独立 QA（G5）。
