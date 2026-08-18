package com.gien.gits.adapter.filesystem;

import java.util.Optional;

/**
 * 从两类文件布局中提取 JSON：
 *
 * <ul>
 *   <li>纯 JSON 文件（route / activation）。</li>
 *   <li>Markdown 前导元数据文件，两种变体：
 *       <pre>---{"json"}---</pre>（单行内联）或
 *       <pre>---\n{"json"}\n---</pre>（标准 frontmatter）。</li>
 * </ul>
 *
 * <p>任何无法识别的布局均返回 {@link Optional#empty()}（fail-closed），不抛异常。</p>
 */
final class JsonFrontmatterExtractor {

    private JsonFrontmatterExtractor() {}

    static Optional<String> extract(String content) {
        if (content == null) {
            return Optional.empty();
        }
        String trimmed = content.strip();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        if (trimmed.startsWith("{")) {
            // 纯 JSON
            return Optional.of(trimmed);
        }

        // Markdown frontmatter：--- {json} ---  或  ---\n{json}\n---
        if (!trimmed.startsWith("---")) {
            return Optional.empty();
        }

        String body = trimmed.substring(3);
        if (body.startsWith("{")) {
            // ---{"json"}--- 或 ---{"json"}--- 尾随 ---
            int end = findClosingJson(body);
            if (end < 0) {
                return Optional.empty();
            }
            return Optional.of(body.substring(0, end));
        }

        // ---\n{json}\n--- 标准 frontmatter
        int secondDash = body.indexOf("---");
        if (secondDash < 0) {
            return Optional.empty();
        }
        String json = body.substring(0, secondDash).strip();
        return json.isEmpty() ? Optional.empty() : Optional.of(json);
    }

    private static int findClosingJson(String body) {
        // 找到匹配的收尾 }，忽略字符串内的花括号
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
        }
        return -1;
    }
}
