package com.gien.gits.api.adapter;

import com.gien.gits.ontology.port.ScenarioDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 文件系统场景数据提供者 — 从外部目录（SCENARIO_DATA_ROOT）读取V1.1场景数据
 * <p>
 * 配置项：scenario.data-root 指向场景数据根目录的绝对路径
 * <p>
 * 版本切换机制：通过修改data-root路径或symlink实现
 * <pre>
 *   scenario.data-root=/opt/gits/scenario-data  → 指向当前版本symlink
 *   /opt/gits/scenario-data → /opt/gits/scenario-data-v1.1
 * </pre>
 */
public class FileSystemScenarioDataProvider implements ScenarioDataProvider {

    private static final Logger log = LoggerFactory.getLogger(FileSystemScenarioDataProvider.class);

    private final Path rootPath;

    public FileSystemScenarioDataProvider(String dataRoot) {
        Path resolved = Paths.get(dataRoot);
        if (!Files.isDirectory(resolved)) {
            throw new IllegalArgumentException(
                "SCENARIO_DATA_ROOT does not exist or is not a directory: " + dataRoot);
        }
        this.rootPath = resolved.toAbsolutePath().normalize();
        log.info("[ScenarioData] FileSystem provider initialized: root={}", this.rootPath);
    }

    @Override
    public String getProviderType() {
        return "filesystem";
    }

    @Override
    public boolean exists(String relativePath) {
        return Files.isRegularFile(resolve(relativePath));
    }

    @Override
    public Optional<InputStream> openStream(String relativePath) {
        Path target = resolve(relativePath);
        if (!Files.isRegularFile(target)) {
            log.debug("[ScenarioData] File not found: {}", target);
            return Optional.empty();
        }
        try {
            return Optional.of(Files.newInputStream(target));
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to open stream: {} — {}", target, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> readText(String relativePath) {
        Path target = resolve(relativePath);
        if (!Files.isRegularFile(target)) {
            log.debug("[ScenarioData] File not found: {}", target);
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(target));
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to read text: {} — {}", target, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<String> readLines(String relativePath) {
        Path target = resolve(relativePath);
        if (!Files.isRegularFile(target)) {
            log.debug("[ScenarioData] File not found: {}", target);
            return Collections.emptyList();
        }
        try {
            return Files.readAllLines(target);
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to read lines: {} — {}", target, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listFiles(String relativeDir) {
        Path dir = resolve(relativeDir);
        if (!Files.isDirectory(dir)) {
            log.debug("[ScenarioData] Directory not found: {}", dir);
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .sorted()
                .toList();
        } catch (IOException e) {
            log.warn("[ScenarioData] Failed to list directory: {} — {}", dir, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getRootDescription() {
        return rootPath.toString();
    }

    private Path resolve(String relativePath) {
        return rootPath.resolve(relativePath).normalize();
    }
}
