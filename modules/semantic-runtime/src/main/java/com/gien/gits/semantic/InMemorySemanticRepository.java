package com.gien.gits.semantic;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Default in-memory implementation of {@link SemanticRepositoryPort}.
 *
 * <p>Stores the loaded {@link SemanticPackage} in memory and performs a minimal,
 * dependency-light structural well-formedness check on candidate Turtle. It is
 * fail-closed: any null, empty, or structurally malformed candidate (or a call
 * made before any package is loaded) yields {@code conforms=false} with a
 * non-conformance report and never throws.
 *
 * <p>This implementation intentionally does not perform SHACL validation; the
 * authoritative SHACL validation lives in the Jena adapter module. Here we only
 * assert that the candidate is structurally plausible Turtle so that callers
 * get a deterministic, non-throwing result.
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
     * Minimal hand-rolled Turtle well-formedness check. Returns a non-conformance
     * report string when the candidate is structurally implausible, or {@code null}
     * when it passes. The check is intentionally conservative: it only verifies
     * balanced delimiters, terminated string literals, and the presence of at
     * least one statement terminator. It is not a full Turtle parser.
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
     * Skips a Turtle string literal starting at index {@code i} (the opening quote)
     * and returns the index of the closing quote, or {@code -1} if unterminated.
     * Handles both plain and triple-quoted literals.
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
