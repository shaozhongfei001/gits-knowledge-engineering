package com.gien.gits.api.service;

import com.gien.gits.api.adapter.FileSystemScenarioDataProvider;
import com.gien.gits.ontology.port.ScenarioDataProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * V11ScenarioDataReader单元测试 — 验证多格式数据解析
 */
class V11ScenarioDataReaderTest {

    @TempDir
    Path tempDir;
    private V11ScenarioDataReader reader;

    @BeforeEach
    void setUp() throws IOException {
        Path dataRoot = tempDir.resolve("scenario-data");
        Files.createDirectories(dataRoot.resolve("02_master_data"));
        Files.createDirectories(dataRoot.resolve("04_external_data"));
        Files.createDirectories(dataRoot.resolve("05_knowledge"));

        ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot.toString());
        reader = new V11ScenarioDataReader(provider);
    }

    @Test
    void readJson_parsesJsonObject() throws IOException {
        String json = """
            {
              "customer_id": "CUST-001",
              "canonical_name": "华东精工",
              "items": ["a", "b"]
            }
            """;
        Files.writeString(tempDir.resolve("scenario-data/02_master_data/test.json"), json);

        Optional<JsonNode> result = reader.readJson("02_master_data/test.json");
        assertTrue(result.isPresent());
        assertEquals("CUST-001", result.get().path("customer_id").asText());
        assertEquals("华东精工", result.get().path("canonical_name").asText());
        assertEquals(2, result.get().path("items").size());
    }

    @Test
    void readJsonl_parsesEachLine() throws IOException {
        String jsonl = """
            {"id":"1","name":"first"}
            {"id":"2","name":"second"}
            # comment line should be skipped
            {"id":"3","name":"third"}
            """;
        Files.writeString(tempDir.resolve("scenario-data/04_external_data/test.jsonl"), jsonl);

        List<JsonNode> results = reader.readJsonl("04_external_data/test.jsonl");
        assertEquals(3, results.size());
        assertEquals("1", results.get(0).path("id").asText());
        assertEquals("3", results.get(2).path("id").asText());
    }

    @Test
    void readCsv_parsesHeadersAndRows() throws IOException {
        String csv = """
            entity_id,name,role,ownership_pct
            ENT-001,华东精工,母公司,100
            ENT-002,华东智能,子公司,60
            """;
        Files.writeString(tempDir.resolve("scenario-data/02_master_data/test.csv"), csv);

        V11ScenarioDataReader.CsvData data = reader.readCsv("02_master_data/test.csv");
        assertEquals(List.of("entity_id", "name", "role", "ownership_pct"), data.headers());
        assertEquals(2, data.rowCount());
        assertEquals("华东精工", data.rows().get(0).get("name"));
        assertEquals("60", data.rows().get(1).get("ownership_pct"));
    }

    @Test
    void readCsv_handlesQuotedFields() throws IOException {
        String csv = """
            id,description,value
            1,"Contains, comma",100
            2,"Has ""quotes"" inside",200
            """;
        Files.writeString(tempDir.resolve("scenario-data/02_master_data/quoted.csv"), csv);

        V11ScenarioDataReader.CsvData data = reader.readCsv("02_master_data/quoted.csv");
        assertEquals(2, data.rowCount());
        assertEquals("Contains, comma", data.rows().get(0).get("description"));
    }

    @Test
    void readYaml_parsesToNode() throws IOException {
        String yaml = """
            products:
              - product_id: P001
                name: 流动资金贷款
                status: ACTIVE
              - product_id: P002
                name: 项目贷款
                status: INACTIVE
            """;
        Files.writeString(tempDir.resolve("scenario-data/05_knowledge/test.yaml"), yaml);

        Optional<JsonNode> result = reader.readYaml("05_knowledge/test.yaml");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().path("products").size());
        assertEquals("P001", result.get().path("products").path(0).path("product_id").asText());
    }

    @Test
    void readText_returnsRawContent() throws IOException {
        String content = "Hello, World!";
        Files.writeString(tempDir.resolve("scenario-data/02_master_data/test.txt"), content);

        Optional<String> result = reader.readText("02_master_data/test.txt");
        assertTrue(result.isPresent());
        assertEquals("Hello, World!", result.get());
    }

    @Test
    void nonexistentFile_returnsEmpty() {
        assertFalse(reader.exists("nonexistent/file.json"));
        assertEquals(Optional.empty(), reader.readJson("nonexistent/file.json"));
        assertTrue(reader.readJsonl("nonexistent/file.jsonl").isEmpty());
        assertTrue(reader.readCsv("nonexistent/file.csv").isEmpty());
        assertEquals(Optional.empty(), reader.readYaml("nonexistent/file.yaml"));
    }
}
