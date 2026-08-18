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

    /**
     * 从文件原文中提取 JSON 段（fail-closed）。
     *
     * <p>支持的布局：
     * <ul>
     *   <li>纯 JSON：内容以 {@code {} 开头；</li>
     *   <li>Markdown 前导单行内联：{@code ---{"json"}---}；</li>
     *   <li>Markdown 前导标准块：{@code ---\n{"json"}\n---}。</li>
     * </ul>
     * 任何无法识别的布局（无 JSON、分隔符缺失、未闭合）一律返回 {@link Optional#empty()}。</p>
     *
     * @param content 文件原文
     * @return 提取出的 JSON 字符串；无法识别返回空
     */
    static Optional<String> extract(String content) {
        // 空内容：直接视为无有效 JSON。
        if (content == null) {
            return Optional.empty();
        }
        // 去除首尾空白，便于统一判断前缀。
        String trimmed = content.strip();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        // ── 布局一：纯 JSON 文件（route/activation 用）────────────────────────
        // 以 `{` 开头即视为整段 JSON，直接返回。
        if (trimmed.startsWith("{")) {
            return Optional.of(trimmed);
        }

        // ── 布局二/三：Markdown 前导元数据 ────────────────────────────────────
        // 前导元数据必须以 `---` 开头；否则布局无法识别。
        if (!trimmed.startsWith("---")) {
            return Optional.empty();
        }
        // 去掉开头的 `---`，剩下的是 `{json}...`（单行内联）或 `\n{json}\n---`（标准块）。
        String body = trimmed.substring(3);

        // 单行内联变体：`---{"json"}---`，body 直接以 `{` 开头。
        if (body.startsWith("{")) {
            // 找到 JSON 对象匹配的收尾 `}`（忽略字符串内花括号），截取完整 JSON。
            int end = findClosingJson(body);
            if (end < 0) {
                // JSON 未闭合 → 非法布局。
                return Optional.empty();
            }
            return Optional.of(body.substring(0, end));
        }

        // 标准块变体：`---\n{json}\n---`，找第二个 `---` 作为 JSON 结束边界。
        int secondDash = body.indexOf("---");
        if (secondDash < 0) {
            // 缺少收尾分隔符 → 非法布局。
            return Optional.empty();
        }
        // 两个 `---` 之间即为 JSON 内容，去除首尾空白后返回。
        String json = body.substring(0, secondDash).strip();
        return json.isEmpty() ? Optional.empty() : Optional.of(json);
    }

    /**
     * 在单行内联布局中找到 JSON 对象匹配的收尾 {@code } 的位置（含该字符）。
     *
     * <p>逐字符扫描并跟踪三态：是否在字符串内（inString）、是否转义（escaped）、
     * 以及花括号嵌套深度（depth）。当深度归零时即找到匹配的收尾。字符串内的花括号
     * 不参与嵌套计数，避免误判。</p>
     *
     * @param body 以 {@code {} 开头的 JSON 段（可能尾随更多文本）
     * @return 收尾 {@code }} 的下标 + 1；若未闭合返回 -1
     */
    private static int findClosingJson(String body) {
        boolean inString = false;   // 是否处于双引号字符串内部
        boolean escaped = false;    // 字符串内前一个字符是否为反斜杠（转义）
        int depth = 0;              // 花括号嵌套深度
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            // 字符串内部：只处理转义与字符串结束，花括号不参与嵌套。
            if (inString) {
                if (escaped) {
                    // 转义字符后：当前字符是字面量，清除转义态。
                    escaped = false;
                } else if (c == '\\') {
                    // 遇到反斜杠：进入转义态，下一字符视为字面量。
                    escaped = true;
                } else if (c == '"') {
                    // 未转义的双引号：字符串结束。
                    inString = false;
                }
                continue;
            }
            // 字符串外部：
            if (c == '"') {
                // 进入字符串。
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    // 找到与开头 `{` 匹配的收尾。
                    return i + 1;
                }
            }
        }
        // 扫描完仍未闭合。
        return -1;
    }
}
