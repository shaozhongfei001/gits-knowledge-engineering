package com.gien.gits.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gien.gits.ontology.port.ScenarioDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * V1.1场景数据读取器 — 从ScenarioDataProvider读取并解析多格式数据文件
 * <p>
 * 支持格式：JSON, JSONL, CSV, YAML
 * <p>
 * 数据根目录结构（V1.1）：
 * <pre>
 * scenario/seed/
 * ├── 02_master_data/     (customer_master.json, legal_entities.csv, ...)
 * ├── 03_bank_data/       (accounts.csv, credit_facilities.csv, ...)
 * ├── 04_external_data/   (external_events.jsonl, ...)
 * ├── 05_knowledge/       (product_knowledge_cards.yaml, ontology_seed.json, ...)
 * ├── 06_interactions/    (historical_interactions.jsonl, ...)
 * └── ...
 * </pre>
 */
public class V11ScenarioDataReader {

    private static final Logger log = LoggerFactory.getLogger(V11ScenarioDataReader.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private final ScenarioDataProvider provider;

    public V11ScenarioDataReader(ScenarioDataProvider provider) {
        this.provider = provider;
    }

    /**
     * 读取JSON文件，解析为JsonNode
     */
    public Optional<JsonNode> readJson(String relativePath) {
        return provider.readText(relativePath).map(text -> {
            try {
                return JSON_MAPPER.readTree(text);
            } catch (IOException e) {
                log.warn("[V11Reader] Failed to parse JSON: {} — {}", relativePath, e.getMessage());
                return null;
            }
        });
    }

    /**
     * 读取JSON文件，解析为指定类型
     */
    public <T> Optional<T> readJson(String relativePath, Class<T> type) {
        return provider.readText(relativePath).map(text -> {
            try {
                return JSON_MAPPER.readValue(text, type);
            } catch (IOException e) {
                log.warn("[V11Reader] Failed to parse JSON to {}: {} — {}", type.getSimpleName(), relativePath, e.getMessage());
                return null;
            }
        });
    }

    /**
     * 读取JSONL文件，每行解析为JsonNode
     */
    public List<JsonNode> readJsonl(String relativePath) {
        List<String> lines = provider.readLines(relativePath);
        List<JsonNode> results = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            try {
                results.add(JSON_MAPPER.readTree(line));
            } catch (IOException e) {
                log.warn("[V11Reader] Failed to parse JSONL line {} in {}: {}", i + 1, relativePath, e.getMessage());
            }
        }
        return results;
    }

    /**
     * 读取CSV文件，解析为行列表（首行为header）
     */
    public CsvData readCsv(String relativePath) {
        List<String> lines = provider.readLines(relativePath);
        if (lines.isEmpty()) {
            return CsvData.empty();
        }

        String[] headers = parseCsvLine(lines.get(0));
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] values = parseCsvLine(line);
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.length && j < values.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }
            rows.add(row);
        }
        return new CsvData(Arrays.asList(headers), rows);
    }

    /**
     * 读取YAML文件，解析为JsonNode（统一格式处理）
     */
    public Optional<JsonNode> readYaml(String relativePath) {
        return provider.readText(relativePath).map(text -> {
            try {
                return YAML_MAPPER.readTree(text);
            } catch (IOException e) {
                log.warn("[V11Reader] Failed to parse YAML: {} — {}", relativePath, e.getMessage());
                return null;
            }
        });
    }

    /**
     * 读取纯文本文件
     */
    public Optional<String> readText(String relativePath) {
        return provider.readText(relativePath);
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String relativePath) {
        return provider.exists(relativePath);
    }

    /**
     * 列出目录下文件
     */
    public List<String> listFiles(String relativeDir) {
        return provider.listFiles(relativeDir);
    }

    /**
     * 获取底层provider
     */
    public ScenarioDataProvider getProvider() {
        return provider;
    }

    /**
     * 简易CSV行解析（处理引号内的逗号）
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * CSV数据容器
     */
    public static class CsvData {
        private final List<String> headers;
        private final List<Map<String, String>> rows;

        public CsvData(List<String> headers, List<Map<String, String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }

        public static CsvData empty() {
            return new CsvData(Collections.emptyList(), Collections.emptyList());
        }

        public List<String> headers() { return headers; }
        public List<Map<String, String>> rows() { return rows; }
        public int rowCount() { return rows.size(); }
        public boolean isEmpty() { return rows.isEmpty(); }
    }
}
