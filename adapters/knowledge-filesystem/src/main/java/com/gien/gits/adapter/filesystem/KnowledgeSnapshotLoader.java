package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 知识快照装载器（P22 E1）：启动时从 Git/文件系统权威源一次性加载到
 * {@link InMemoryKnowledgeStore}。复用现有 {@code Filesystem*Reader} 的 fail-closed 解析
 * 与字段校验，避免重复解析逻辑。加载完成后运行时不再触盘（GAP-2 消除）。
 *
 * <p>加载失败（目录缺失/权限异常）抛 {@link IllegalStateException} 以 fail-closed 拒绝启动，
 * 不产生半成品快照。</p>
 */
public final class KnowledgeSnapshotLoader {

    private final Path knowledgeRoot;

    public KnowledgeSnapshotLoader(Path knowledgeRoot) {
        this.knowledgeRoot = Objects.requireNonNull(knowledgeRoot, "knowledgeRoot");
    }

    /**
     * 构建并校验不可变知识快照。
     *
     * @return 已填充且非空的 {@link InMemoryKnowledgeStore}
     * @throws IllegalStateException 若知识根目录不可读或加载结果为空
     */
    public InMemoryKnowledgeStore load() {
        if (!Files.isDirectory(knowledgeRoot)) {
            throw new IllegalStateException("knowledge root not a directory: " + knowledgeRoot);
        }

        InMemoryKnowledgeStore.Builder builder = InMemoryKnowledgeStore.builder();

        loadMaps(builder);
        loadAssets(builder);
        loadContracts(builder);
        loadPolicies(builder);
        loadElements(builder);

        if (!builder.isNonEmpty()) {
            throw new IllegalStateException("knowledge snapshot is empty at: " + knowledgeRoot);
        }
        return builder.build();
    }

    private void loadMaps(InMemoryKnowledgeStore.Builder builder) {
        Path mapsDir = knowledgeRoot.resolve("maps");
        if (!Files.isDirectory(mapsDir)) {
            return;
        }
        // 遍历 <maps>/**/*.md：既含 <domain>/DOMAIN_MAP.md，也含子目录中的 TASK 地图
        //（如 <domain>/previsit-preparation.md）。每个文件直接解析为 KnowledgeMap（不依赖
        // mapId→路径命名约定，因文件命名可能与 mapId 不一致）。
        FailClosedJsonReader mapReader = new FailClosedJsonReader();
        try (Stream<Path> files = Files.walk(mapsDir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .forEach(path -> mapReader.read(path, KnowledgeMap.class, KnowledgeSnapshotLoader::isValidMap)
                            .ifPresent(builder::putMap));
        } catch (IOException | SecurityException error) {
            throw fail("maps enumeration", error);
        }
    }

    /** KnowledgeMap 必需字段校验（fail-closed）。 */
    private static boolean isValidMap(com.gien.gits.knowledge.KnowledgeMap map) {
        if (map == null) {
            return false;
        }
        return hasText(map.mapId()) && hasText(map.name()) && hasText(map.status())
                && hasText(map.mapType()) && map.entrypoints() != null && hasText(map.routePolicyRef());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void loadAssets(InMemoryKnowledgeStore.Builder builder) {
        Path assetsDir = knowledgeRoot.resolve("assets");
        if (!Files.isDirectory(assetsDir)) {
            return;
        }
        FilesystemAssetCatalogReader assetReader = new FilesystemAssetCatalogReader(assetsDir);
        for (AssetManifest asset : assetReader.listAll()) {
            builder.putAsset(asset);
        }
    }

    private void loadContracts(InMemoryKnowledgeStore.Builder builder) {
        Path activationsDir = knowledgeRoot.resolve("activations");
        if (!Files.isDirectory(activationsDir)) {
            return;
        }
        FilesystemActivationContractReader contractReader =
                new FilesystemActivationContractReader(activationsDir);
        listJsonIds(activationsDir).forEach(id ->
                contractReader.find(id).ifPresent(builder::putContract));
    }

    private void loadPolicies(InMemoryKnowledgeStore.Builder builder) {
        Path routesDir = knowledgeRoot.resolve("routes");
        if (!Files.isDirectory(routesDir)) {
            return;
        }
        FilesystemRoutePolicyReader policyReader = new FilesystemRoutePolicyReader(routesDir);
        listJsonIds(routesDir).forEach(id ->
                policyReader.find(id).ifPresent(builder::putPolicy));
    }

    private void loadElements(InMemoryKnowledgeStore.Builder builder) {
        Path elementsDir = knowledgeRoot.resolve("elements");
        if (!Files.isDirectory(elementsDir)) {
            return;
        }
        FilesystemKnowledgeElementReader elementReader = new FilesystemKnowledgeElementReader(elementsDir);
        try (Stream<Path> kis = Files.list(elementsDir)) {
            kis.filter(Files::isDirectory)
                    .sorted()
                    .forEach(kiDir -> {
                        for (KnowledgeElement element : elementReader.listByKnowledgeItem(kiDir.getFileName().toString())) {
                            builder.putElement(element);
                        }
                    });
        } catch (IOException | SecurityException error) {
            throw fail("elements enumeration", error);
        }
    }

    /** 列出目录下 {@code *.json} 文件的去扩展名 ID。 */
    private Stream<String> listJsonIds(Path dir) {
        try {
            return Files.list(dir)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .map(path -> {
                        String name = path.getFileName().toString();
                        return name.substring(0, name.length() - ".json".length());
                    });
        } catch (IOException | SecurityException error) {
            throw fail("json enumeration in " + dir, error);
        }
    }

    private static IllegalStateException fail(String context, Exception error) {
        return new IllegalStateException("knowledge snapshot load failed at " + context + ": "
                + error.getMessage(), error);
    }
}
