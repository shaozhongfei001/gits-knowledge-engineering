# P22 ｜ Next Session Baton

| 字段 | 值 |
|---|---|
| **Updated** | `2026-08-19T20:35:00Z` |
| **holder** | `tech_lead` |
| **packet** | `P22-llm-wiki-knowledge-map` |
| **wave** | `W1` |
| **do_not_start** | 禁止改动 generated/；禁止修改 P20 已 qa_pass 合同；禁止生产切换；禁止自签 QA_PASS |

短提示词：你是 `tech_lead`。P22 **G3 已完成**（feature-pilot-g3，DEV_SELF_CHECK，commit 见 EVIDENCE EV-P22-G3-001）。已完成：
- KnowledgeWikiPort（renderMap/renderKnowledgeItem/renderElement，fail-closed）
- KnowledgeWikiFilesystemAdapter（从 InMemoryKnowledgeStore 渲染 LLM 可读受控地图）
- KnowledgeWikiService（读图→systemPrompt→LlmClient.complete，失败 fallback 模板）
- 修复 apps/api 上下文测试：知识根路径 CWD 无关解析
- `make check` 全绿；`-pl modules/knowledge-architecture,adapters/knowledge-filesystem,apps/api -am test` 全通过

下一步（W1 待办）：**G4 P22 shadow E2E**（scripts/run_p22_shadow_e2e.py，本轮 G3 未做）。建议由 e2e_owner 或 feature_pilot 接棒，产出 replayable shadow evidence 后进入 G5 独立 QA。
