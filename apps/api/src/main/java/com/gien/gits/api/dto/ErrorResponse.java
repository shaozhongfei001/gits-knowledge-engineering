package com.gien.gits.api.dto;

/**
 * 统一错误响应DTO。
 *
 * @param errorCode 错误码
 * @param message   错误描述
 * @param timestamp 错误发生时间（ISO格式）
 */
public record ErrorResponse(
        String errorCode,
        String message,
        String timestamp
) {}
