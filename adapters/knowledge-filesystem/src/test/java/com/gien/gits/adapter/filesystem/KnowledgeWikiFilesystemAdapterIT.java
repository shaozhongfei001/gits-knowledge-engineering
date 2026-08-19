package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * 知识地图渲染适配器集成测试（P22 G3）。
 *
 * <p>从真实权威源（specs/knowledge-architecture）加载内存快照，验证渲染输出包含
 * 真实 KI/KE 数据与权威标注；未知 ID fail-closed 返回空字符串。</p>
 */
class KnowledgeWikiFilesystemAdapterIT {

    private static final Path KNOWLEDGE_ROOT =
            Path.of("specs/knowledge-architecture").toAbsolutePath();

    @Test
    void renderMapContainsRealKnowledgeItemsAndElements() {
        InMemoryKnowledgeStore store = new KnowledgeSnapshotLoader(KNOWLEDGE_ROOT).load();
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(store);

        // 根地图渲染：应包含 ROOT 地图元数据 + 全量 KI→KE 导航（含真实 KE）。
        String root = adapter.renderMap(null);
        assertFalse(root.isBlank());
        assertTrue(root.contains("GITS企业知识工程根知识地图"));
        assertTrue(root.contains("[AUTHORITATIVE]"));
        assertTrue(root.contains("KI-009"));
        assertTrue(root.contains("KE-009-01"));
        assertTrue(root.contains("KI-FRONT-001"));

        // 访前任务地图渲染。
        String previsit = adapter.renderMap("KM-CORP-RM-PREVISIT");
        assertFalse(previsit.isBlank());
        assertTrue(previsit.contains("访前准备任务地图"));
        assertTrue(previsit.contains("PRE_VISIT_PREPARATION"));
    }

    @Test
    void renderKnowledgeItemAndElementReturnRealData() {
        InMemoryKnowledgeStore store = new KnowledgeSnapshotLoader(KNOWLEDGE_ROOT).load();
        KnowledgeWikiFilesystemAdapter adapter = new KnowledgeWikiFilesystemAdapter(store);

        String ki = adapter.renderKnowledgeItem("KI-009");
        assertFalse(ki.isBlank());
        assertTrue(ki.contains("KE-009-01"));
        assertTrue(ki.contains("客户全称"));

        String element = adapter.renderElement("KE-009-01");
        assertFalse(element.isBlank());
        assertTrue(element.contains("企业工商注册"));
        assertTrue(element.contains("AUTHORITATIVE"));

        // fail-closed
        assertTrue(adapter.renderKnowledgeItem("KI-UNKNOWN").isEmpty());
        assertTrue(adapter.renderElement("KE-UNKNOWN").isEmpty());
    }
}
