package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.port.KnowledgeWikiPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 知识地图渲染适配器（P22 G3：LLM 优先读图）。
 *
 * <p>从 {@link InMemoryKnowledgeStore} 内存快照渲染 LLM 可读受控知识地图（Markdown）。
 * 渲染内容仅基于已有合同（KnowledgeMap / AssetManifest / KnowledgeElement）字段，
 * 含权威标注（AUTHORITATIVE）。fail-closed：未知 ID / 无匹配内容返回空字符串，不返回 null。</p>
 */
public final class KnowledgeWikiFilesystemAdapter implements KnowledgeWikiPort {

    /** 权威标注，标识渲染内容源自受控权威源。 */
    private static final String AUTHORITY_TAG = "[AUTHORITATIVE]";

    private final InMemoryKnowledgeStore store;

    public KnowledgeWikiFilesystemAdapter(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public String renderMap(String scope) {
        KnowledgeMap map = resolveMap(scope);
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(safe(map.name())).append(" ").append(AUTHORITY_TAG).append('\n');
        sb.append("- mapId: `").append(safe(map.mapId())).append("`\n");
        sb.append("- version: `").append(safe(map.version())).append("`\n");
        sb.append("- status: `").append(safe(map.status())).append("`\n");
        sb.append("- mapType: `").append(safe(map.mapType())).append("`\n");
        sb.append("- defaultPolicy: `").append(safe(map.defaultPolicy())).append("`\n");

        appendEntrypoints(sb, map);
        appendDomains(sb, map);
        appendKnowledgeItemsNavigation(sb);

        return sb.toString();
    }

    @Override
    public String renderKnowledgeItem(String kiId) {
        if (isBlank(kiId)) {
            return "";
        }
        List<KnowledgeElement> elements = store.elements().values().stream()
                .filter(element -> kiId.equals(element.knowledgeItemId()))
                .sorted(Comparator.comparing(KnowledgeElement::elementId))
                .toList();
        if (elements.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("# Knowledge Item `").append(kiId).append("` ").append(AUTHORITY_TAG).append('\n');
        for (KnowledgeElement element : elements) {
            appendElementSummary(sb, element);
        }
        return sb.toString();
    }

    @Override
    public String renderElement(String elementId) {
        if (isBlank(elementId)) {
            return "";
        }
        KnowledgeElement element = store.elements().get(elementId);
        if (element == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("# Element `").append(safe(element.elementId())).append("` ").append(AUTHORITY_TAG).append('\n');
        sb.append("- name: ").append(safe(element.name())).append('\n');
        sb.append("- kind: `").append(safe(element.kind())).append("`\n");
        sb.append("- knowledgeItemId: `").append(safe(element.knowledgeItemId())).append("`\n");
        sb.append("- source: ").append(safe(element.source() == null ? null : element.source().sourceRef()))
                .append(" (").append(safe(element.source() == null ? null : element.source().authority())).append(")\n");
        sb.append("- status: `").append(safe(element.status())).append("`\n");
        if (element.content() != null && !element.content().isBlank()) {
            sb.append("\n").append(safe(element.content())).append('\n');
        }
        return sb.toString();
    }

    // ── 私有渲染辅助 ─────────────────────────────────────────────────────

    /**
     * 解析渲染范围对应的知识地图。
     *
     * <p>scope 可精确匹配地图 ID 或域 ID（遍历所有地图的 domains）；空白/未知回退根地图
     * （ROOT）。fail-closed：根地图也不存在时返回 {@code null}。</p>
     */
    private KnowledgeMap resolveMap(String scope) {
        if (!isBlank(scope)) {
            KnowledgeMap byMapId = store.maps().get(scope);
            if (byMapId != null) {
                return byMapId;
            }
            KnowledgeMap byDomain = store.maps().values().stream()
                    .filter(map -> map.domains() != null
                            && map.domains().stream().anyMatch(domain -> scope.equals(domain.domainId())))
                    .findFirst()
                    .orElse(null);
            if (byDomain != null) {
                return byDomain;
            }
        }
        return rootFallback();
    }

    /**
     * 回退根地图：优先 {@code ROOT} 键，其次按 {@code mapType=ROOT} 匹配，最后任取一张地图。
     * fail-closed：快照为空时返回 {@code null}。
     */
    private KnowledgeMap rootFallback() {
        KnowledgeMap root = store.rootMap();
        if (root != null) {
            return root;
        }
        return store.maps().values().stream()
                .filter(map -> "ROOT".equalsIgnoreCase(map.mapType()))
                .findFirst()
                .orElseGet(() -> store.maps().values().stream().findFirst().orElse(null));
    }

    private void appendEntrypoints(StringBuilder sb, KnowledgeMap map) {
        if (map.entrypoints() == null) {
            return;
        }
        sb.append("\n## 场景入口（Entrypoints）\n");
        if (map.entrypoints().tasks() != null && !map.entrypoints().tasks().isEmpty()) {
            sb.append("- tasks: ");
            sb.append(map.entrypoints().tasks().stream()
                    .map(task -> "`" + safe(task) + "`")
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append('\n');
        }
        if (map.entrypoints().roles() != null && !map.entrypoints().roles().isEmpty()) {
            sb.append("- roles: ");
            sb.append(map.entrypoints().roles().stream()
                    .map(role -> "`" + safe(role) + "`")
                    .collect(java.util.stream.Collectors.joining(", ")));
            sb.append('\n');
        }
    }

    private void appendDomains(StringBuilder sb, KnowledgeMap map) {
        if (map.domains() == null || map.domains().isEmpty()) {
            return;
        }
        sb.append("\n## 知识域（Domains）\n");
        for (KnowledgeMap.Domain domain : map.domains()) {
            sb.append("- `").append(safe(domain.domainId())).append("` ")
                    .append(safe(domain.name()));
            if (domain.purpose() != null && !domain.purpose().isBlank()) {
                sb.append(" — ").append(safe(domain.purpose()));
            }
            sb.append('\n');
        }
    }

    /**
     * 追加全量 KI→KE 分层导航。
     *
     * <p>将快照中全部知识要素按 knowledgeItemId 分组、按 ID 排序，形成
     * 知识条目（KI）→ 知识要素（KE）导航树，供 LLM 定位加载范围。</p>
     */
    private void appendKnowledgeItemsNavigation(StringBuilder sb) {
        Map<String, List<KnowledgeElement>> byKi = new java.util.LinkedHashMap<>();
        store.elements().values().stream()
                .sorted(Comparator.comparing(KnowledgeElement::knowledgeItemId)
                        .thenComparing(KnowledgeElement::elementId))
                .forEach(element -> byKi
                        .computeIfAbsent(element.knowledgeItemId(), k -> new ArrayList<>())
                        .add(element));
        if (byKi.isEmpty()) {
            return;
        }
        sb.append("\n## 知识条目导航（KI → KE）\n");
        for (Map.Entry<String, List<KnowledgeElement>> entry : byKi.entrySet()) {
            sb.append("- **KI `").append(safe(entry.getKey())).append("`**\n");
            for (KnowledgeElement element : entry.getValue()) {
                sb.append("  - `").append(safe(element.elementId())).append("` ")
                        .append(safe(element.name()))
                        .append(" [").append(safe(element.kind())).append("]\n");
            }
        }
    }

    private void appendElementSummary(StringBuilder sb, KnowledgeElement element) {
        sb.append("- `").append(safe(element.elementId())).append("` ")
                .append(safe(element.name()))
                .append(" [").append(safe(element.kind())).append("]");
        if (element.content() != null && !element.content().isBlank()) {
            sb.append(" — ").append(safe(element.content()));
        }
        sb.append('\n');
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
