package com.gien.gits.engagement;

import java.util.List;
import java.util.Objects;

/**
 * 60秒作战卡 — R2格式 (移动端快速查看)
 */
public record QuickBattleCard(
        String cardId,
        String customerName,
        String visitObjective,
        String customerTier,
        String riskLevel,
        List<String> keyPoints,
        List<String> productHints,
        List<String> dontForget,
        String bottomLine) {

    public QuickBattleCard {
        Objects.requireNonNull(cardId, "cardId");
        keyPoints = List.copyOf(keyPoints != null ? keyPoints : List.of());
        productHints = List.copyOf(productHints != null ? productHints : List.of());
        dontForget = List.copyOf(dontForget != null ? dontForget : List.of());
    }
}
