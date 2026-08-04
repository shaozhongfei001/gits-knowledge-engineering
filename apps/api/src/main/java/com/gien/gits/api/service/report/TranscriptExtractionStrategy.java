package com.gien.gits.api.service.report;

import com.gien.gits.engagement.InteractionExtraction;

import java.util.List;

/**
 * 转录提取策略接口 — 从会议记录中提取结构化信息
 * 不同提取方式（语义模式、关键词、AI辅助）可替换实现
 */
public interface TranscriptExtractionStrategy {

    /**
     * 从原始转录内容中提取结构化信息
     * @param rawContent 原始转录文本
     * @return 提取结果列表
     */
    List<InteractionExtraction> extract(String rawContent);
}
