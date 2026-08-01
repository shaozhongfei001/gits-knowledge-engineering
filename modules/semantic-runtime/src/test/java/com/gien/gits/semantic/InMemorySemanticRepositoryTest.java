package com.gien.gits.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class InMemorySemanticRepositoryTest {

    private static final byte[] ONTOLOGY = turtle(
            "@prefix gits: <https://gientech.com/gits/kno/> .",
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .",
            "gits:CoreOntology a owl:Ontology .");

    private static final byte[] SHAPES = turtle(
            "@prefix sh: <http://www.w3.org/ns/shacl#> .",
            "@prefix gits: <https://gientech.com/gits/kno/> .",
            "gits:OperatingCaseShape a sh:NodeShape .");

    private static byte[] turtle(String... lines) {
        return String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void loadThenValidateValidCandidateConformsAndVersionMatches() {
        InMemorySemanticRepository repo = new InMemorySemanticRepository();
        repo.load(new SemanticPackage("gits-core", "0.1.0", ONTOLOGY, SHAPES));

        byte[] candidate = turtle(
                "@prefix gits: <https://gientech.com/gits/kno/> .",
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .",
                "gits:OperatingCase a owl:Class .");

        SemanticRepositoryPort.ValidationResult result = repo.validate(candidate);
        assertTrue(result.conforms(), () -> "expected conforms=true, report=" + result.reportText());
        assertEquals("0.1.0", result.semanticPackageVersion());
    }

    @Test
    void validateNullCandidateIsFailClosedWithoutException() {
        InMemorySemanticRepository repo = new InMemorySemanticRepository();
        repo.load(new SemanticPackage("gits-core", "0.1.0", ONTOLOGY, SHAPES));

        SemanticRepositoryPort.ValidationResult nullResult = repo.validate(null);
        assertFalse(nullResult.conforms());
        assertEquals("0.1.0", nullResult.semanticPackageVersion());

        SemanticRepositoryPort.ValidationResult emptyResult = repo.validate(new byte[0]);
        assertFalse(emptyResult.conforms());
    }

    @Test
    void validateMalformedCandidateIsFailClosedWithoutException() {
        InMemorySemanticRepository repo = new InMemorySemanticRepository();
        repo.load(new SemanticPackage("gits-core", "0.1.0", ONTOLOGY, SHAPES));

        SemanticRepositoryPort.ValidationResult result =
                repo.validate(turtle("@prefix gits: <https://gientech.com/gits/kno/> .",
                        "gits:OperatingCase rdfs:label \"unterminated ."));
        assertFalse(result.conforms(), () -> "expected conforms=false, report=" + result.reportText());
    }

    @Test
    void semanticPackageRejectsBlankPackageIdOrVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage("  ", "0.1.0", ONTOLOGY, SHAPES));
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage("gits-core", "", ONTOLOGY, SHAPES));
    }
}
