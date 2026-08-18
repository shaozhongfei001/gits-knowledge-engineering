package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 文件系统四类资产清单读取适配器，对应合同 CTR-ASSET-001。
 *
 * <p>扫描资产根目录下的子目录（foundational-data / knowledge-rules / process-tools /
 * runtime-feedback）中的 {@code *.md} 文件，解析 Markdown 前导 JSON。
 * 单个文件解析失败按 fail-closed 跳过，不抛异常；列表接口返回空数组而非 {@code null}。</p>
 */
public final class FilesystemAssetCatalogReader implements AssetCatalogPort {

    private static final List<String> ASSET_DIRS = List.of(
            "foundational-data", "knowledge-rules", "process-tools", "runtime-feedback");

    private final Path assetsDir;
    private final FailClosedJsonReader reader;

    public FilesystemAssetCatalogReader(Path assetsDir) {
        this.assetsDir = Objects.requireNonNull(assetsDir, "assetsDir");
        this.reader = new FailClosedJsonReader();
    }

    @Override
    public Optional<AssetManifest> find(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            return Optional.empty();
        }
        return listAll().stream()
                .filter(asset -> asset.assetId().equals(assetId))
                .findFirst();
    }

    @Override
    public List<AssetManifest> listByDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return List.of();
        }
        return listAll().stream()
                .filter(asset -> domain.equals(asset.domain()))
                .toList();
    }

    @Override
    public List<AssetManifest> listAll() {
        List<AssetManifest> result = new ArrayList<>();
        for (String dirName : ASSET_DIRS) {
            Path dir = assetsDir.resolve(dirName);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(path -> path.getFileName().toString().endsWith(".md"))
                        .sorted()
                        .forEach(path -> reader.read(path, AssetManifest.class, this::isValid)
                                .ifPresent(result::add));
            } catch (IOException | SecurityException error) {
                // fail-closed：单个目录不可读则跳过
            }
        }
        return List.copyOf(result);
    }

    /** 必需字段校验（fail-closed）：assetId、assetType、name、domain、status、source、governance。 */
    private boolean isValid(AssetManifest asset) {
        if (asset == null) {
            return false;
        }
        return hasText(asset.assetId())
                && hasText(asset.assetType())
                && hasText(asset.name())
                && hasText(asset.domain())
                && hasText(asset.status())
                && asset.source() != null
                && hasText(asset.source().uri())
                && asset.governance() != null
                && hasText(asset.governance().owner());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
