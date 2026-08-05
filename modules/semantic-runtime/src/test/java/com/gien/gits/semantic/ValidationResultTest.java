package com.gien.gits.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationResultTest {

    @Test
    void conformingResult() {
        SemanticRepositoryPort.ValidationResult result =
                new SemanticRepositoryPort.ValidationResult(true, "All checks passed", "1.0.0");

        assertTrue(result.conforms());
        assertEquals("All checks passed", result.reportText());
        assertEquals("1.0.0", result.semanticPackageVersion());
    }

    @Test
    void nonConformingResult() {
        SemanticRepositoryPort.ValidationResult result =
                new SemanticRepositoryPort.ValidationResult(false, "Violation found", "2.0.0");

        assertFalse(result.conforms());
        assertEquals("Violation found", result.reportText());
        assertEquals("2.0.0", result.semanticPackageVersion());
    }

    @Test
    void nullReportTextIsAllowed() {
        SemanticRepositoryPort.ValidationResult result =
                new SemanticRepositoryPort.ValidationResult(true, null, "0.1.0");

        assertTrue(result.conforms());
        assertEquals(null, result.reportText());
    }
}
