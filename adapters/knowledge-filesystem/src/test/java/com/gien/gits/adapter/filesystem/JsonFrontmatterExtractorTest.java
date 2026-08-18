package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class JsonFrontmatterExtractorTest {

    @Test
    void extractsPlainJson() {
        Optional<String> result = JsonFrontmatterExtractor.extract("{\"a\":1}");
        assertTrue(result.isPresent());
        assertEquals("{\"a\":1}", result.orElseThrow());
    }

    @Test
    void extractsStandardFrontmatter() {
        String content = "---\n{\"a\":1,\"b\":\"x\"}\n---\n\n# title\n";
        Optional<String> result = JsonFrontmatterExtractor.extract(content);
        assertTrue(result.isPresent());
        assertEquals("{\"a\":1,\"b\":\"x\"}", result.orElseThrow());
    }

    @Test
    void extractsSingleLineInlineFrontmatter() {
        String content = "---{\"a\":1}\n---\n\n# title";
        Optional<String> result = JsonFrontmatterExtractor.extract(content);
        assertTrue(result.isPresent());
        assertEquals("{\"a\":1}", result.orElseThrow());
    }

    @Test
    void handlesJsonWithNestedBracesInSingleLineInline() {
        String content = "---{\"nested\":{\"k\":\"v\"},\"arr\":[{\"x\":1}]}---";
        Optional<String> result = JsonFrontmatterExtractor.extract(content);
        assertTrue(result.isPresent());
        assertEquals("{\"nested\":{\"k\":\"v\"},\"arr\":[{\"x\":1}]}", result.orElseThrow());
    }

    @Test
    void returnsEmptyForNullAndBlank() {
        assertTrue(JsonFrontmatterExtractor.extract(null).isEmpty());
        assertTrue(JsonFrontmatterExtractor.extract("   ").isEmpty());
    }

    @Test
    void returnsEmptyForUnrecognizedLayout() {
        assertTrue(JsonFrontmatterExtractor.extract("just text, no json").isEmpty());
    }

    @Test
    void returnsEmptyForIncompleteFrontmatter() {
        assertTrue(JsonFrontmatterExtractor.extract("---\n{\"a\":1}").isEmpty());
    }
}
