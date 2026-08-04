package com.gien.gits.api.dto;

/**
 * KYC缺口画像创建响应 — 返回新创建画像的标识与状态
 *
 * @param profileId 画像唯一标识
 * @param status    创建状态
 */
public record KycGapProfileCreatedResponse(
        String profileId,
        String status) {}
