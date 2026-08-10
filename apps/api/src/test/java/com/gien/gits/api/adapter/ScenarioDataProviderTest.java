package com.gien.gits.api.adapter;

import com.gien.gits.ontology.port.ScenarioDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScenarioDataProvider适配器单元测试
 */
class ScenarioDataProviderTest {

    // ========== FileSystemScenarioDataProvider ==========

    @Test
    void filesystemProvider_readsJsonFile(@TempDir Path tempDir) throws IOException {
        // 准备测试数据
        Path dataRoot = tempDir.resolve("scenario-data");
        Files.createDirectories(dataRoot.resolve("02_master_data"));
        String jsonContent = """
            {
              "customer_id": "CUST-001",
              "canonical_name": "测试客户",
              "industry": "制造业"
            }
            """;
        Files.writeString(dataRoot.resolve("02_master_data/customer_master.json"), jsonContent);

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());

        assertEquals("filesystem", provider.getProviderType());
        assertTrue(provider.exists("02_master_data/customer_master.json"));
        assertFalse(provider.exists("02_master_data/nonexistent.json"));

        Optional<String> text = provider.readText("02_master_data/customer_master.json");
        assertTrue(text.isPresent());
        assertTrue(text.get().contains("测试客户"));
    }

    @Test
    void filesystemProvider_readsCsvFile(@TempDir Path tempDir) throws IOException {
        Path dataRoot = tempDir.resolve("scenario-data");
        Files.createDirectories(dataRoot.resolve("02_master_data"));
        String csvContent = """
            entity_id,name,role,ownership_pct
            ENT-001,华东精工,母公司,100
            ENT-002,华东智能,子公司,60
            """;
        Files.writeString(dataRoot.resolve("02_master_data/legal_entities.csv"), csvContent);

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());

        List<String> lines = provider.readLines("02_master_data/legal_entities.csv");
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains("entity_id"));
        assertTrue(lines.get(1).contains("华东精工"));
    }

    @Test
    void filesystemProvider_readsJsonlFile(@TempDir Path tempDir) throws IOException {
        Path dataRoot = tempDir.resolve("scenario-data");
        Files.createDirectories(dataRoot.resolve("04_external_data"));
        String jsonlContent = """
            {"event_id":"EVT-001","title":"项目备案"}
            {"event_id":"EVT-002","title":"招标公告"}
            """;
        Files.writeString(dataRoot.resolve("04_external_data/external_events.jsonl"), jsonlContent);

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());

        List<String> lines = provider.readLines("04_external_data/external_events.jsonl");
        assertEquals(2, lines.size());
    }

    @Test
    void filesystemProvider_listsFiles(@TempDir Path tempDir) throws IOException {
        Path dataRoot = tempDir.resolve("scenario-data");
        Path masterDir = dataRoot.resolve("02_master_data");
        Files.createDirectories(masterDir);
        Files.writeString(masterDir.resolve("customer_master.json"), "{}");
        Files.writeString(masterDir.resolve("legal_entities.csv"), "id,name");

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());

        List<String> files = provider.listFiles("02_master_data");
        assertEquals(2, files.size());
        assertTrue(files.contains("customer_master.json"));
        assertTrue(files.contains("legal_entities.csv"));
    }

    @Test
    void filesystemProvider_nonexistentPath_returnsEmpty(@TempDir Path tempDir) throws IOException {
        Path dataRoot = tempDir.resolve("scenario-data");
        Files.createDirectories(dataRoot);

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());

        assertFalse(provider.exists("nonexistent/file.json"));
        assertEquals(Optional.empty(), provider.readText("nonexistent/file.json"));
        assertTrue(provider.readLines("nonexistent/file.json").isEmpty());
        assertTrue(provider.listFiles("nonexistent/dir").isEmpty());
    }

    @Test
    void filesystemProvider_invalidRoot_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new FileSystemScenarioDataProvider("/nonexistent/path/that/does/not/exist"));
    }

    // ========== ClasspathScenarioDataProvider ==========

    @Test
    void classpathProvider_returnsType() {
        ScenarioDataProvider provider = new ClasspathScenarioDataProvider();
        assertEquals("classpath", provider.getProviderType());
        assertTrue(provider.getRootDescription().startsWith("classpath:"));
    }

    @Test
    void classpathProvider_nonexistentResource_returnsEmpty() {
        ScenarioDataProvider provider = new ClasspathScenarioDataProvider();
        assertFalse(provider.exists("nonexistent/file.json"));
        assertEquals(Optional.empty(), provider.readText("nonexistent/file.json"));
        assertTrue(provider.readLines("nonexistent/file.json").isEmpty());
    }

    @Test
    void classpathProvider_listFiles_returnsEmpty() {
        ScenarioDataProvider provider = new ClasspathScenarioDataProvider();
        // Classpath directory listing is not supported
        assertTrue(provider.listFiles("any/directory").isEmpty());
    }
}
