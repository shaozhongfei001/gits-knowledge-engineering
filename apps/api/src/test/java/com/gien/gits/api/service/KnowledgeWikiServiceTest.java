package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.port.KnowledgeWikiPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import com.gien.gits.adapter.filesystem.KnowledgeWikiFilesystemAdapter;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 知识地图读图服务单元测试（P22 G3）。
 *
 * <p>验证：渲染地图注入 systemPrompt、调用 LlmClient、LLM 失败回退到模板。</p>
 */
class KnowledgeWikiServiceTest {

    @Test
    void executeInjectsRenderedMapIntoSystemPrompt() {
        CapturingLlmClient llm = new CapturingLlmClient();
        KnowledgeWikiService service = new KnowledgeWikiService(fixturePort(), llm);

        String result = service.executeWithKnowledgeMap("KM-CORP-RM-PREVISIT", "生成访前客户概览");

        assertEquals("ok", result);
        assertTrue(llm.systemPrompt.contains("受控知识地图"));
        assertTrue(llm.systemPrompt.contains("KE-009-01"));
        assertTrue(llm.systemPrompt.contains("[AUTHORITATIVE]"));
        assertEquals("生成访前客户概览", llm.userPrompt);
    }

    @Test
    void executeWithBlankScopeFallsBackToRootMap() {
        CapturingLlmClient llm = new CapturingLlmClient();
        KnowledgeWikiService service = new KnowledgeWikiService(fixturePort(), llm);

        service.executeWithKnowledgeMap(null, "任务");

        assertTrue(llm.systemPrompt.contains("GITS根知识地图"));
    }

    @Test
    void executeFallsBackToTemplateWhenLlmFails() {
        LlmClient failing = (systemPrompt, userPrompt) -> {
            throw new LlmClientException("llm down");
        };
        KnowledgeWikiService service = new KnowledgeWikiService(fixturePort(), failing);

        String result = service.executeWithKnowledgeMap("KM-CORP-RM-PREVISIT", "生成访前客户概览");

        assertTrue(result.contains("\"fallback\": true"));
        assertTrue(result.contains("knowledgeMapLoaded"));
    }

    @Test
    void buildSystemPromptHandlesEmptyMapText() {
        KnowledgeWikiPort emptyPort = new KnowledgeWikiPort() {
            @Override
            public String renderMap(String scope) {
                return "";
            }

            @Override
            public String renderKnowledgeItem(String kiId) {
                return "";
            }

            @Override
            public String renderElement(String elementId) {
                return "";
            }
        };
        CapturingLlmClient llm = new CapturingLlmClient();
        KnowledgeWikiService service = new KnowledgeWikiService(emptyPort, llm);

        service.executeWithKnowledgeMap("X", "任务");

        assertTrue(llm.systemPrompt.contains("受控知识地图"));
        assertTrue(llm.systemPrompt.contains("不可用"));
    }

    // ---- fixtures ----

    private static KnowledgeWikiPort fixturePort() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putMap(rootMap())
                .putMap(previsitMap())
                .putElement(element("KE-009-01", "KI-009", "客户全称", "企业工商注册的完整法定名称。"))
                .build();
        return new KnowledgeWikiFilesystemAdapter(store);
    }

    private static KnowledgeMap rootMap() {
        return new KnowledgeMap("1.0.0", "KM-GITS-ROOT", "GITS根知识地图", "0.1.0",
                "VALIDATION", "ROOT",
                new KnowledgeMap.Entrypoints(List.of(), List.of("PRE_VISIT_PREPARATION")),
                List.of(), List.of(), List.of(), List.of(), "RP-CORP-RM-001", "DENY", 1200);
    }

    private static KnowledgeMap previsitMap() {
        return new KnowledgeMap("1.0.0", "KM-CORP-RM-PREVISIT", "访前准备任务地图", "0.1.0",
                "VALIDATION", "TASK",
                new KnowledgeMap.Entrypoints(List.of("RELATIONSHIP_MANAGER"),
                        List.of("PRE_VISIT_PREPARATION")),
                List.of(), List.of(), List.of(), List.of(), "RP-CORP-RM-001", "DENY", 2000);
    }

    private static KnowledgeElement element(String id, String ki, String name, String content) {
        return new KnowledgeElement("1.0.0", id, name, "K-Type-F", ki, content,
                new KnowledgeElement.Source("CRM系统", "AUTHORITATIVE"), List.of(), "DRAFT");
    }

    /** 捕获 systemPrompt / userPrompt 的测试桩。 */
    private static final class CapturingLlmClient implements LlmClient {

        private String systemPrompt;
        private String userPrompt;

        @Override
        public String complete(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
            return "ok";
        }
    }
}
