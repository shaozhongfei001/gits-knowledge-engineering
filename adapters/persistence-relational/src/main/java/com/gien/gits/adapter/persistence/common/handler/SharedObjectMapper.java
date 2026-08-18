package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 共享 ObjectMapper 单例 — 供所有 TypeHandler 复用。
 * <p>避免每个 TypeHandler 各自创建 ObjectMapper 实例，减少内存占用并确保序列化配置一致。</p>
 */
public final class SharedObjectMapper {

    private SharedObjectMapper() {}

    private static final ObjectMapper INSTANCE = createInstance();

    private static ObjectMapper createInstance() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static ObjectMapper get() {
        return INSTANCE;
    }
}
