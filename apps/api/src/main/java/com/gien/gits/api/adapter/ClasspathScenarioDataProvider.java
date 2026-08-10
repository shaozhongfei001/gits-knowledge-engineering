package com.gien.gits.api.adapter;

import com.gien.gits.ontology.port.ScenarioDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Classpath场景数据提供者 — 从classpath:scenario-data/读取V1.0内嵌数据
 * <p>
 * 作为V1.0兼容方案，当SCENARIO_DATA_ROOT未配置时自动fallback使用。
 * 数据来自 src/main/resources/scenario-data/ 目录。
 */
public class ClasspathScenarioDataProvider implements ScenarioDataProvider {

    private static final Logger log = LoggerFactory.getLogger(ClasspathScenarioDataProvider.class);
    private static final String CLASSPATH_PREFIX = "scenario-data/";

    public ClasspathScenarioDataProvider() {
        log.info("[ScenarioData] Classpath provider initialized: root=classpath:{}", CLASSPATH_PREFIX);
    }

    @Override
    public String getProviderType() {
        return "classpath";
    }

    @Override
    public boolean exists(String relativePath) {
        return getResourceAsStream(relativePath) != null;
    }

    @Override
    public Optional<InputStream> openStream(String relativePath) {
        InputStream is = getResourceAsStream(relativePath);
        if (is == null) {
            log.debug("[ScenarioData] Classpath resource not found: {}", CLASSPATH_PREFIX + relativePath);
            return Optional.empty();
        }
        return Optional.of(is);
    }

    @Override
    public Optional<String> readText(String relativePath) {
        try (InputStream is = getResourceAsStream(relativePath)) {
            if (is == null) {
                log.debug("[ScenarioData] Classpath resource not found: {}", CLASSPATH_PREFIX + relativePath);
                return Optional.empty();
            }
            return Optional.of(new String(is.readAllBytes()));
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to read classpath resource: {} — {}",
                CLASSPATH_PREFIX + relativePath, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<String> readLines(String relativePath) {
        try (InputStream is = getResourceAsStream(relativePath)) {
            if (is == null) {
                log.debug("[ScenarioData] Classpath resource not found: {}", CLASSPATH_PREFIX + relativePath);
                return Collections.emptyList();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to read classpath lines: {} — {}",
                CLASSPATH_PREFIX + relativePath, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listFiles(String relativeDir) {
        // Classpath目录列表在运行时受限，返回空列表
        // V1.0数据文件路径已知，由ScenarioSeedDataService硬编码处理
        log.debug("[ScenarioData] Classpath directory listing not supported for: {}", relativeDir);
        return Collections.emptyList();
    }

    @Override
    public String getRootDescription() {
        return "classpath:" + CLASSPATH_PREFIX;
    }

    private InputStream getResourceAsStream(String relativePath) {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(CLASSPATH_PREFIX + relativePath);
    }
}
