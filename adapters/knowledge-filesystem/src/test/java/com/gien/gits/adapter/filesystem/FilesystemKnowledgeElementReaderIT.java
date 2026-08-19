package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.KnowledgeElement;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 集成测试：用真实 filesystem reader 加载 P22 权威规范录入的知识要素数据
 * {@code specs/knowledge-architecture/elements/}，验证与《银行知识工程规范打样_fixed.xlsx》对齐。
 */
class FilesystemKnowledgeElementReaderIT {

    private static Path ELEMENTS;
    private static FilesystemKnowledgeElementReader reader;

    @BeforeAll
    static void resolveElementsDir() {
        Path root = Path.of(".").toAbsolutePath().normalize();
        while (root != null && !root.resolve("specs/knowledge-architecture").toFile().isDirectory()) {
            root = root.getParent();
        }
        assertTrue(root != null && root.resolve("specs/knowledge-architecture").toFile().isDirectory(),
                "specs/knowledge-architecture not found");
        ELEMENTS = root.resolve("specs/knowledge-architecture/elements");
        reader = new FilesystemKnowledgeElementReader(ELEMENTS);
    }

    @Test
    void loadsAllAuthoritativeElementsAcrossKis() {
        int total = 0;
        for (String ki : new String[]{"KI-009", "KI-FRONT-001", "KI-FRONT-002", "KI-FRONT-003",
                "KI-FRONT-004", "KI-FRONT-005", "KI-FRONT-006"}) {
            List<KnowledgeElement> elements = reader.listByKnowledgeItem(ki);
            total += elements.size();
        }
        // 规范 4.4 共 39 个 KE：8+3+9+6+4+4+5=39
        assertEquals(39, total, "should load all 39 authoritative knowledge elements");
    }

    @Test
    void ki009HasEightCoreFields() {
        List<KnowledgeElement> elements = reader.listByKnowledgeItem("KI-009");
        assertEquals(8, elements.size());
        // 验证 K-Type-F 事实要素的 source.authority 为 AUTHORITATIVE
        assertTrue(elements.stream().allMatch(e -> e.kind().equals("K-Type-F")));
        assertTrue(elements.stream().allMatch(e -> e.source().authority().equals("AUTHORITATIVE")));
    }

    @Test
    void findLoadsSpecificElement() {
        assertTrue(reader.find("KE-009-01").isPresent());
        assertEquals("客户全称", reader.find("KE-009-01").get().name());
        assertFalse(reader.find("KE-NOT-EXIST").isPresent());
    }

    @Test
    void kiFront002HasNineDimensions() {
        List<KnowledgeElement> elements = reader.listByKnowledgeItem("KI-FRONT-002");
        assertEquals(9, elements.size());
        // 八维 + 综合结论，含 K-Type-M 量化评分
        assertTrue(elements.stream().anyMatch(e -> e.kind().equals("K-Type-M")));
        assertTrue(elements.stream().anyMatch(e -> e.kind().equals("K-Type-E")));
    }
}
