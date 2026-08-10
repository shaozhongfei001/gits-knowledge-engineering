package com.gien.gits.api.config;

import com.gien.gits.api.adapter.ClasspathScenarioDataProvider;
import com.gien.gits.api.adapter.FileSystemScenarioDataProvider;
import com.gien.gits.ontology.port.ScenarioDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 场景数据配置 — 根据SCENARIO_DATA_ROOT选择数据提供者
 * <p>
 * 优先级：
 * 1. scenario.data-root 配置了有效路径 → FileSystemScenarioDataProvider (V1.1+)
 * 2. 未配置或路径无效 → ClasspathScenarioDataProvider (V1.0 fallback)
 * <p>
 * 版本切换方式：
 * - 修改 application.yaml 中 scenario.data-root
 * - 设置环境变量 SCENARIO_DATA_ROOT
 * - 使用 symlink: scenario-data → scenario-data-v1.1
 */
@Configuration
public class ScenarioDataConfig {

    private static final Logger log = LoggerFactory.getLogger(ScenarioDataConfig.class);

    @Bean
    public ScenarioDataProvider scenarioDataProvider(
            @Value("${scenario.data-root:}") String dataRoot) {

        if (dataRoot != null && !dataRoot.isBlank()) {
            try {
                ScenarioDataProvider provider = new FileSystemScenarioDataProvider(dataRoot);
                log.info("[ScenarioData] Using FileSystem provider: root={}", dataRoot);
                return provider;
            } catch (IllegalArgumentException e) {
                log.warn("[ScenarioData] SCENARIO_DATA_ROOT invalid ({}), falling back to classpath. Error: {}",
                    dataRoot, e.getMessage());
            }
        }

        // 也检查环境变量
        String envRoot = System.getenv("SCENARIO_DATA_ROOT");
        if (envRoot != null && !envRoot.isBlank()) {
            try {
                ScenarioDataProvider provider = new FileSystemScenarioDataProvider(envRoot);
                log.info("[ScenarioData] Using FileSystem provider (from env): root={}", envRoot);
                return provider;
            } catch (IllegalArgumentException e) {
                log.warn("[ScenarioData] SCENARIO_DATA_ROOT env invalid ({}), falling back to classpath. Error: {}",
                    envRoot, e.getMessage());
            }
        }

        ScenarioDataProvider provider = new ClasspathScenarioDataProvider();
        log.info("[ScenarioData] Using Classpath provider (fallback)");
        return provider;
    }
}
