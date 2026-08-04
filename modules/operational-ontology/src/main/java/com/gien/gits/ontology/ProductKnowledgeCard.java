package com.gien.gits.ontology;

import java.time.Instant;
import java.util.List;

/**
 * 产品知识卡 — 银行产品的结构化知识
 */
public record ProductKnowledgeCard(
        String productId,
        String name,
        String definition,
        List<String> keyConditions,
        List<String> requiredMaterials,
        List<String> riskPoints,
        String trigger,
        List<String> prohibitedPhrases,
        String evidenceSource,
        Instant createdAt) {

    public ProductKnowledgeCard {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        keyConditions = List.copyOf(keyConditions != null ? keyConditions : List.of());
        requiredMaterials = List.copyOf(requiredMaterials != null ? requiredMaterials : List.of());
        riskPoints = List.copyOf(riskPoints != null ? riskPoints : List.of());
        prohibitedPhrases = List.copyOf(prohibitedPhrases != null ? prohibitedPhrases : List.of());
        if (createdAt == null) createdAt = Instant.now();
    }

    /** 兼容旧构造器（无审计字段） */
    public ProductKnowledgeCard(String productId, String name, String definition,
                                List<String> keyConditions, List<String> requiredMaterials,
                                List<String> riskPoints, String trigger,
                                List<String> prohibitedPhrases, String evidenceSource) {
        this(productId, name, definition, keyConditions, requiredMaterials, riskPoints,
             trigger, prohibitedPhrases, evidenceSource, Instant.now());
    }
}
