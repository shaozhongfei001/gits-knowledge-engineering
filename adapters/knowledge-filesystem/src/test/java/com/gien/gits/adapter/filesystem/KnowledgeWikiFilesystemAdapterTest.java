package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 知识地图渲染适配器单元测试（P22 G3）。
 *
 * <p>验证 renderMap / renderKnowledgeItem / renderElement 输出为 LLM 可读 Markdown、
 * 含 KI/KE 导航与权威标注；未知 ID fail-closed 返回空字符串。</p>
 */
class KnowledgeWikiFilesystemAdapterTest {

    private static final String AUTHORITY_TAG = "[AUTHORITATIVE]";

    @Test
    void renderMapContainsMapMetadataAndKiKeNavigation() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        String rendered = adapter.renderMap("KM-CORP-RM-PREVISIT");

        assertNotNull(rendered);
        assertFalse(rendered.isBlank());
        assertTrue(rendered.contains("访前准备任务地图"));
        assertTrue(rendered.contains(AUTHORITY_TAG));
        assertTrue(rendered.contains("PRE_VISIT_PREPARATION"));
        assertTrue(rendered.contains("KI-009"));
        assertTrue(rendered.contains("KE-009-01"));
        assertTrue(rendered.contains("K-Type-F"));
    }

    @Test
    void renderMapWithRootScopeFallsBackToRoot() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        String renderedRoot = adapter.renderMap(null);
        assertFalse(renderedRoot.isBlank());
        assertTrue(renderedRoot.contains("GITS根知识地图"));

        String renderedBlank = adapter.renderMap("   ");
        assertEquals(renderedRoot, renderedBlank);
    }

    @Test
    void renderMapUnknownScopeFallsBackToRoot() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        String rendered = adapter.renderMap("NO-SUCH-MAP");
        assertFalse(rendered.isBlank());
        assertTrue(rendered.contains("GITS根知识地图"));
    }

    @Test
    void renderKnowledgeItemListsElementsOfGivenKi() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        String rendered = adapter.renderKnowledgeItem("KI-009");

        assertFalse(rendered.isBlank());
        assertTrue(rendered.contains("KI-009"));
        assertTrue(rendered.contains("KE-009-01"));
        assertTrue(rendered.contains("KE-009-02"));
        assertTrue(rendered.contains(AUTHORITY_TAG));
    }

    @Test
    void renderKnowledgeItemUnknownKiReturnsEmpty() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        assertEquals("", adapter.renderKnowledgeItem("KI-UNKNOWN"));
        assertEquals("", adapter.renderKnowledgeItem(null));
        assertEquals("", adapter.renderKnowledgeItem(""));
    }

    @Test
    void renderElementRendersSingleElementDetail() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        String rendered = adapter.renderElement("KE-009-01");

        assertFalse(rendered.isBlank());
        assertTrue(rendered.contains("KE-009-01"));
        assertTrue(rendered.contains("客户全称"));
        assertTrue(rendered.contains(AUTHORITY_TAG));
        assertTrue(rendered.contains("企业工商注册"));
    }

    @Test
    void renderElementUnknownIdReturnsEmpty() {
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(fixtureStore());

        assertEquals("", adapter.renderElement("KE-UNKNOWN"));
        assertEquals("", adapter.renderElement(null));
        assertEquals("", adapter.renderElement(""));
    }

    @Test
    void emptyStoreIsFailClosed() {
        KnowledgeWikiFilesystemAdapter adapter =
                new KnowledgeWikiFilesystemAdapter(InMemoryKnowledgeStore.builder().build());

        assertEquals("", adapter.renderMap("X"));
        assertEquals("", adapter.renderMap(null));
        assertEquals("", adapter.renderKnowledgeItem("KI-009"));
        assertEquals("", adapter.renderElement("KE-009-01"));
    }

    // ---- fixtures ----

    private static InMemoryKnowledgeStore fixtureStore() {
        return InMemoryKnowledgeStore.builder()
                .putMap(rootMap())
                .putMap(previsitMap())
                .putElement(element("KE-009-01", "KI-009", "客户全称",
                        "企业工商注册的完整法定名称，须与营业执照完全一致。"))
                .putElement(element("KE-009-02", "KI-009", "统一社会信用代码",
                        "企业唯一身份代码，用于跨系统对齐。"))
                .build();
    }

    private static KnowledgeMap rootMap() {
        return new KnowledgeMap("1.0.0", "KM-GITS-ROOT", "GITS根知识地图", "0.1.0",
                "VALIDATION", "ROOT",
                new KnowledgeMap.Entrypoints(
                        List.of("RELATIONSHIP_MANAGER"),
                        List.of("PRE_VISIT_PREPARATION", "FACT_RECONCILIATION_30M")),
                List.of(new KnowledgeMap.Domain("KD-CORP-RM", "对公客户持续经营",
                        "支持经营触发、访前、互动、访后和持续经营闭环",
                        "specs/knowledge-architecture/maps/corporate-rm/DOMAIN_MAP.md")),
                List.of(), List.of(), List.of(), "RP-CORP-RM-001", "DENY", 1200);
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
}
