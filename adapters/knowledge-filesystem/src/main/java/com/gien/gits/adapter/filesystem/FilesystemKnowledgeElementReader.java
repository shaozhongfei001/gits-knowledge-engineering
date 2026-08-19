package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.port.KnowledgeElementPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 文件系统知识要素读取适配器，对应合同 CTR-KELEM-001。
 *
 * <p>扫描知识条目子目录 {@code <elements>/<KI-ID>/} 下的 {@code KE-*.md} 文件，
 * 解析 Markdown 前导 JSON。单个文件解析失败按 fail-closed 跳过，不抛异常；
 * 列表接口返回空数组而非 {@code null}；路径越界由 {@link PathSafety} 防护。</p>
 */
public final class FilesystemKnowledgeElementReader implements KnowledgeElementPort {

    private final Path elementsDir;
    private final FailClosedJsonReader reader;

    public FilesystemKnowledgeElementReader(Path elementsDir) {
        this.elementsDir = Objects.requireNonNull(elementsDir, "elementsDir");
        this.reader = new FailClosedJsonReader();
    }

    @Override
    public Optional<KnowledgeElement> find(String elementId) {
        if (elementId == null || elementId.isBlank()) {
            return Optional.empty();
        }
        // elementId 形如 KE-009-01，所属 KI 未知，需遍历所有子目录
        return listAll().stream()
                .filter(element -> element.elementId().equals(elementId))
                .findFirst();
    }

    @Override
    public List<KnowledgeElement> listByKnowledgeItem(String knowledgeItemId) {
        if (knowledgeItemId == null || knowledgeItemId.isBlank()
                || !PathSafety.isSafeSegment(knowledgeItemId)) {
            return List.of();
        }
        Path kiDir = elementsDir.resolve(knowledgeItemId);
        if (!PathSafety.isWithinBase(elementsDir, kiDir) || !Files.isDirectory(kiDir)) {
            return List.of();
        }
        List<KnowledgeElement> result = new ArrayList<>();
        try (Stream<Path> files = Files.list(kiDir)) {
            files.filter(path -> path.getFileName().toString().startsWith("KE-")
                            && path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .forEach(path -> readElement(path).ifPresent(result::add));
        } catch (IOException | SecurityException error) {
            // fail-closed：单个目录不可读则跳过
        }
        return List.copyOf(result);
    }

    @Override
    public List<KnowledgeElement> listAll() {
        List<KnowledgeElement> result = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(elementsDir)) {
            dirs.filter(Files::isDirectory)
                    .sorted()
                    .forEach(kiDir -> result.addAll(listByKnowledgeItem(kiDir.getFileName().toString())));
        } catch (IOException | SecurityException error) {
            // fail-closed：根目录不可读则返回空
        }
        return List.copyOf(result);
    }

    private Optional<KnowledgeElement> readElement(Path file) {
        return reader.read(file, KnowledgeElement.class, this::isValid);
    }

    /** 必需字段校验（fail-closed）：elementId、name、kind、knowledgeItemId、content、source、status。 */
    private boolean isValid(KnowledgeElement element) {
        if (element == null) {
            return false;
        }
        return hasText(element.schemaVersion())
                && hasText(element.elementId())
                && hasText(element.name())
                && hasText(element.kind())
                && hasText(element.knowledgeItemId())
                && hasText(element.content())
                && element.source() != null
                && hasText(element.source().sourceRef())
                && hasText(element.source().authority())
                && hasText(element.status());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
