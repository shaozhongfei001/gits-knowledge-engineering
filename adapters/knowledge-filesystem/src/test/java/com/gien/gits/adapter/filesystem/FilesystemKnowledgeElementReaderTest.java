package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.KnowledgeElement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemKnowledgeElementReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void listByKnowledgeItemLoadsElementsForKi() throws IOException {
        Path elementsDir = Files.createDirectory(tempDir.resolve("elements"));
        Path kiDir = Files.createDirectory(elementsDir.resolve("KI-009"));
        Files.writeString(kiDir.resolve("KE-009-01.md"), KE_NAME, StandardCharsets.UTF_8);
        Files.writeString(kiDir.resolve("KE-009-02.md"), KE_NUMBER, StandardCharsets.UTF_8);

        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(elementsDir);
        List<KnowledgeElement> all = reader.listByKnowledgeItem("KI-009");

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(e -> e.elementId().equals("KE-009-01")));
        assertTrue(all.stream().anyMatch(e -> e.elementId().equals("KE-009-02")));
    }

    @Test
    void listByKnowledgeItemOnlyReturnsThatKi() throws IOException {
        Path elementsDir = Files.createDirectory(tempDir.resolve("elements2"));
        Path kiA = Files.createDirectory(elementsDir.resolve("KI-009"));
        Path kiB = Files.createDirectory(elementsDir.resolve("KI-FRONT-001"));
        Files.writeString(kiA.resolve("KE-009-01.md"), KE_NAME, StandardCharsets.UTF_8);
        Files.writeString(kiB.resolve("KE-FRONT-001-01.md"), KE_SUPPLIER, StandardCharsets.UTF_8);

        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(elementsDir);
        List<KnowledgeElement> kiAOnly = reader.listByKnowledgeItem("KI-009");

        assertEquals(1, kiAOnly.size());
        assertEquals("KE-009-01", kiAOnly.get(0).elementId());
    }

    @Test
    void findReturnsMatchingElement() throws IOException {
        Path elementsDir = Files.createDirectory(tempDir.resolve("elements3"));
        Path kiDir = Files.createDirectory(elementsDir.resolve("KI-009"));
        Files.writeString(kiDir.resolve("KE-009-01.md"), KE_NAME, StandardCharsets.UTF_8);

        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(elementsDir);
        assertTrue(reader.find("KE-009-01").isPresent());
        assertTrue(reader.find("KE-UNKNOWN").isEmpty());
        assertTrue(reader.find(null).isEmpty());
        assertTrue(reader.find("").isEmpty());
    }

    @Test
    void skipsMalformedElementFailClosed() throws IOException {
        Path elementsDir = Files.createDirectory(tempDir.resolve("elements4"));
        Path kiDir = Files.createDirectory(elementsDir.resolve("KI-009"));
        Files.writeString(kiDir.resolve("KE-009-01.md"), KE_NAME, StandardCharsets.UTF_8);
        Files.writeString(kiDir.resolve("bad.md"), "---\n{not-json}\n---", StandardCharsets.UTF_8);
        Files.writeString(kiDir.resolve("missing.md"), "---\n{\"elementId\":\"KE-X\"}\n---", StandardCharsets.UTF_8);
        Files.writeString(kiDir.resolve("non-ke.md"), KE_NAME, StandardCharsets.UTF_8); // 文件名不以 KE- 开头，应忽略

        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(elementsDir);
        List<KnowledgeElement> all = reader.listByKnowledgeItem("KI-009");

        // 只加载 KE- 前缀且合法的文件
        assertEquals(1, all.size());
        assertEquals("KE-009-01", all.get(0).elementId());
    }

    @Test
    void rejectsPathTraversalInKnowledgeItemId() throws IOException {
        Path elementsDir = Files.createDirectory(tempDir.resolve("elements5"));
        Files.createDirectory(elementsDir.resolve("KI-009"));

        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(elementsDir);
        assertTrue(reader.listByKnowledgeItem("../secret").isEmpty());
        assertTrue(reader.listByKnowledgeItem("").isEmpty());
        assertTrue(reader.listByKnowledgeItem(null).isEmpty());
    }

    @Test
    void returnsEmptyWhenDirMissing() {
        FilesystemKnowledgeElementReader reader = new FilesystemKnowledgeElementReader(tempDir.resolve("nope"));
        assertTrue(reader.listByKnowledgeItem("KI-009").isEmpty());
        assertTrue(reader.find("KE-009-01").isEmpty());
    }

    private static final String KE_NAME = """
            ---
            {"schemaVersion":"1.0.0","elementId":"KE-009-01","name":"客户全称","kind":"K-Type-F","knowledgeItemId":"KI-009","content":"企业工商注册的完整法定名称","source":{"sourceRef":"CRM系统","authority":"AUTHORITATIVE"},"status":"DRAFT"}
            ---
            """;

    private static final String KE_NUMBER = """
            ---
            {"schemaVersion":"1.0.0","elementId":"KE-009-02","name":"客户编号","kind":"K-Type-F","knowledgeItemId":"KI-009","content":"行内唯一内部标识编码","source":{"sourceRef":"CRM系统","authority":"AUTHORITATIVE"},"status":"DRAFT"}
            ---
            """;

    private static final String KE_SUPPLIER = """
            ---
            {"schemaVersion":"1.0.0","elementId":"KE-FRONT-001-01","name":"上游供应商列表","kind":"K-Type-F","knowledgeItemId":"KI-FRONT-001","content":"客户直接原材料供应商清单","source":{"sourceRef":"行内交易流水+外部工商/产业数据","authority":"AUTHORITATIVE"},"status":"DRAFT"}
            ---
            """;
}
