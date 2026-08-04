package com.gien.gits.ontology.model;

import java.time.LocalDateTime;

/**
 * Oracle系统索赔记录 — 从Oracle只读管道读取的索赔数据模型。
 */
public record OracleClaim(
    String claimId,
    String customerName,
    String claimType,
    String status,
    LocalDateTime claimDate,
    LocalDateTime lastUpdated,
    String sourceSystem
) {}
