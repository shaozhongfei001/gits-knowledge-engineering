package com.gien.gits.knowledge;

import java.util.List;

/**
 * 知识要素（Knowledge Element）领域模型，对应合同 CTR-KELEM-001
 * (specs/knowledge-architecture/schemas/knowledge-element.schema.json)。
 *
 * <p>知识要素是知识条目（KI）下的原子要素，按生产方式分类
 * （K-Type-F 事实 / K-Type-R 规则 / K-Type-P 话术方法 / K-Type-E 综合研判 / K-Type-M 量化模型）。
 * 仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record KnowledgeElement(
        String schemaVersion,
        String elementId,
        String name,
        String kind,
        String knowledgeItemId,
        String content,
        Source source,
        List<String> relatedRules,
        String status) {

    public KnowledgeElement {
        relatedRules = orEmpty(relatedRules);
    }

    private static List<String> orEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Source(
            String sourceRef,
            String authority) {}
}
