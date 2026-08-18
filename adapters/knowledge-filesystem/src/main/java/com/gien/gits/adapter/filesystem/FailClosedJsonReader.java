package com.gien.gits.adapter.filesystem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Fail-closed JSON 文件读取器。
 *
 * <p>任何失败（文件缺失、读取异常、JSON 解析失败、必需字段校验失败）都统一返回
 * {@link Optional#empty()}，不抛异常、不返回部分/非法对象。</p>
 */
final class FailClosedJsonReader {

    private final ObjectMapper mapper;

    FailClosedJsonReader() {
        this.mapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .build();
    }

    <T> Optional<T> read(Path file, Class<T> type, Predicate<T> valid) {
        String content;
        try {
            content = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException | SecurityException error) {
            return Optional.empty();
        }

        Optional<String> json = JsonFrontmatterExtractor.extract(content);
        if (json.isEmpty()) {
            return Optional.empty();
        }

        T value;
        try {
            value = mapper.readValue(json.get(), type);
        } catch (IOException error) {
            return Optional.empty();
        }

        if (value == null || !valid.test(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
