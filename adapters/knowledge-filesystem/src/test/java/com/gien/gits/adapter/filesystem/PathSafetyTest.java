package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathSafetyTest {

    @Test
    void acceptsPlainSegment() {
        assertTrue(PathSafety.isSafeSegment("AC-PREVISIT-001"));
        assertTrue(PathSafety.isSafeSegment("SP-02"));
    }

    @Test
    void rejectsTraversalAndAbsolute() {
        assertFalse(PathSafety.isSafeSegment("../../etc/passwd"));
        assertFalse(PathSafety.isSafeSegment("../secret"));
        assertFalse(PathSafety.isSafeSegment("/etc/passwd"));
        assertFalse(PathSafety.isSafeSegment("a/b"));
        assertFalse(PathSafety.isSafeSegment("a\\b"));
        assertFalse(PathSafety.isSafeSegment(".."));
        assertFalse(PathSafety.isSafeSegment("."));
    }

    @Test
    void rejectsBlankAndNull() {
        assertFalse(PathSafety.isSafeSegment(""));
        assertFalse(PathSafety.isSafeSegment("   "));
        assertFalse(PathSafety.isSafeSegment(null));
    }

    @Test
    void candidateOutsideBaseIsRejected() {
        Path base = Path.of("/repo/activations").toAbsolutePath().normalize();
        assertTrue(PathSafety.isWithinBase(base, base.resolve("AC-001.json")));
        assertFalse(PathSafety.isWithinBase(base, Path.of("/etc/passwd")));
    }
}
