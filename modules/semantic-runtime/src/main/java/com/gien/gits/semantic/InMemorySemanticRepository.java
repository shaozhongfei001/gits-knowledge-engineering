package com.gien.gits.semantic;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * {@link SemanticRepositoryPort} 的默认内存实现。
 *
 * <p>将已加载的 {@link SemanticPackage} 保存在内存中，并对候选 Turtle 执行最小化、
 * 轻依赖的结构性良构性检查。其行为严格 fail-closed：任何 null、空或结构异常的候选
 * （或在任何包加载之前发起调用）都会返回 {@code conforms=false} 及不合规报告，并且
 * 从不抛异常。
 *
 * <p>本实现刻意不执行 SHACL 校验；权威的 SHACL 校验位于 Jena 适配器模块。这里仅断言
 * 候选内容在结构上是合理的 Turtle，从而让调用方得到一个确定、不抛异常的结果。
 */
public final class InMemorySemanticRepository implements SemanticRepositoryPort {

    private static final String NOT_LOADED = "NOT_LOADED";

    private volatile SemanticPackage loaded;
    private volatile String version = NOT_LOADED;

    @Override
    public void load(SemanticPackage semanticPackage) {
        Objects.requireNonNull(semanticPackage, "semanticPackage");
        this.loaded = semanticPackage;
        this.version = semanticPackage.version();
    }

    @Override
    public ValidationResult validate(byte[] candidateTurtle) {
        String currentVersion = this.version;
        if (NOT_LOADED.equals(currentVersion)) {
            return new ValidationResult(false, "no semantic package loaded", currentVersion);
        }
        if (candidateTurtle == null || candidateTurtle.length == 0) {
            return new ValidationResult(false, "candidate turtle is null or empty", currentVersion);
        }
        String text = new String(candidateTurtle, StandardCharsets.UTF_8);
        String report = checkWellFormedness(text);
        if (report != null) {
            return new ValidationResult(false, report, currentVersion);
        }
        return new ValidationResult(true, "candidate turtle is structurally well-formed", currentVersion);
    }

    /** Visible for tests. */
    SemanticPackage loadedPackage() {
        return loaded;
    }

    /**
     * 最小化的手写 Turtle 良构性检查。
     *
     * <p>当候选内容在结构上不合理时返回不合规报告字符串，通过时返回 {@code null}。
     * 该检查刻意保守：仅核验分隔符是否配对、字符串字面量是否闭合，以及是否存在至少一个
     * 语句终止符。它并不是一个完整的 Turtle 解析器。</p>
     */
    private static String checkWellFormedness(String text) {
        int parens = 0;
        int brackets = 0;
        boolean hasStatementTerminator = false;
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"': {
                    int next = consumeString(text, i);
                    if (next < 0) {
                        return "unterminated string literal in candidate turtle";
                    }
                    i = next;
                    break;
                }
                case '(':
                    parens++;
                    break;
                case ')':
                    parens--;
                    if (parens < 0) {
                        return "unbalanced closing parenthesis in candidate turtle";
                    }
                    break;
                case '[':
                    brackets++;
                    break;
                case ']':
                    brackets--;
                    if (brackets < 0) {
                        return "unbalanced closing bracket in candidate turtle";
                    }
                    break;
                case '.':
                    hasStatementTerminator = true;
                    break;
                default:
                    break;
            }
        }
        if (parens != 0) {
            return "unbalanced parentheses in candidate turtle";
        }
        if (brackets != 0) {
            return "unbalanced brackets in candidate turtle";
        }
        if (!hasStatementTerminator) {
            return "no statement terminator '.' in candidate turtle";
        }
        return null;
    }

    /**
     * 跳过从下标 {@code i}（开引号）开始的 Turtle 字符串字面量。
     *
     * <p>返回闭合引号的下标；若未闭合则返回 {@code -1}。同时处理普通引号（`"..."`）
     * 与三引号（`"""..."""`）两种字面量。</p>
     */
    private static int consumeString(String text, int i) {
        int n = text.length();
        if (i + 2 < n && text.charAt(i + 1) == '"' && text.charAt(i + 2) == '"') {
            int close = text.indexOf("\"\"\"", i + 3);
            return close < 0 ? -1 : close + 2;
        }
        int j = i + 1;
        while (j < n) {
            char c = text.charAt(j);
            if (c == '\\' && j + 1 < n) {
                j += 2;
                continue;
            }
            if (c == '"') {
                return j;
            }
            j++;
        }
        return -1;
    }
}
