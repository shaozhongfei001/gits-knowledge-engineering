package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * 文件系统知识地图读取适配器，对应合同 CTR-KMAP-001。
 *
 * <p>根地图文件 {@code ROOT_KNOWLEDGE_MAP.md} 使用 Markdown 前导 JSON 布局；
 * 按 {@code mapId} 加载的域地图支持 {@code <maps>/<dir>/DOMAIN_MAP.md} 布局。
 * 所有校验 fail-closed：内容不合法一律返回 {@link Optional#empty()}。</p>
 */
public final class FilesystemKnowledgeMapReader implements KnowledgeMapPort {

    private final Path mapsDir;
    private final FailClosedJsonReader reader;

    public FilesystemKnowledgeMapReader(Path mapsDir) {
        this.mapsDir = Objects.requireNonNull(mapsDir, "mapsDir");
        this.reader = new FailClosedJsonReader();
    }

    @Override
    public Optional<KnowledgeMap> loadRoot() {
        return readMap(mapsDir.resolve("ROOT_KNOWLEDGE_MAP.md"));
    }

    @Override
    public Optional<KnowledgeMap> load(String mapId) {
        if (!PathSafety.isSafeSegment(mapId)) {
            return Optional.empty();
        }
        // 按 mapId 定位：<maps>/<dir>/DOMAIN_MAP.md（mapId 小写目录约定），也兼容直接 mapId.md
        Path direct = mapsDir.resolve(mapId + ".md");
        if (PathSafety.isWithinBase(mapsDir, direct) && java.nio.file.Files.isRegularFile(direct)) {
            return readMap(direct);
        }
        Path nested = mapsDir.resolve(mapId.toLowerCase()).resolve("DOMAIN_MAP.md");
        if (PathSafety.isWithinBase(mapsDir, nested) && java.nio.file.Files.isRegularFile(nested)) {
            return readMap(nested);
        }
        return Optional.empty();
    }

    private Optional<KnowledgeMap> readMap(Path file) {
        return reader.read(file, KnowledgeMap.class, this::isValid);
    }

    /** 必需字段校验（fail-closed）：mapId、name、status、mapType、entrypoints、routePolicyRef。 */
    private boolean isValid(KnowledgeMap map) {
        if (map == null) {
            return false;
        }
        return hasText(map.mapId())
                && hasText(map.name())
                && hasText(map.status())
                && hasText(map.mapType())
                && map.entrypoints() != null
                && hasText(map.routePolicyRef());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
