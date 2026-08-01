package com.gien.gits.adapter.jena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.semantic.SemanticPackage;
import com.gien.gits.semantic.SemanticRepositoryPort;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JenaSemanticRepositoryAdapterTest {

    private static final byte[] ONTOLOGY = turtle(
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .",
            "@prefix ex: <http://example.com/> .",
            "ex:Ontology a owl:Ontology .");

    private static final byte[] SHAPES = turtle(
            "@prefix sh: <http://www.w3.org/ns/shacl#> .",
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
            "@prefix ex: <http://example.com/> .",
            "ex:ThingShape a sh:NodeShape ;",
            "    sh:targetClass ex:Thing ;",
            "    sh:property [",
            "        sh:path ex:name ;",
            "        sh:minCount 1 ;",
            "        sh:datatype xsd:string ;",
            "    ] .");

    private static byte[] turtle(String... lines) {
        return String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    }

    private static JenaSemanticRepositoryAdapter loadedAdapter() {
        JenaSemanticRepositoryAdapter adapter = new JenaSemanticRepositoryAdapter();
        adapter.load(new SemanticPackage("ex-core", "0.2.0", ONTOLOGY, SHAPES));
        return adapter;
    }

    @Test
    void loadThenValidateConformingCandidateConformsAndVersionMatches() {
        JenaSemanticRepositoryAdapter adapter = loadedAdapter();

        byte[] candidate = turtle(
                "@prefix ex: <http://example.com/> .",
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
                "ex:thing1 a ex:Thing ;",
                "    ex:name \"Alpha\" .");

        SemanticRepositoryPort.ValidationResult result = adapter.validate(candidate);
        assertTrue(result.conforms(), () -> "expected conforms=true, report=" + result.reportText());
        assertEquals("0.2.0", result.semanticPackageVersion());
    }

    @Test
    void validateNonConformingCandidateReportsViolationWithoutException() {
        JenaSemanticRepositoryAdapter adapter = loadedAdapter();

        byte[] candidate = turtle(
                "@prefix ex: <http://example.com/> .",
                "ex:thing2 a ex:Thing .");

        SemanticRepositoryPort.ValidationResult result = adapter.validate(candidate);
        assertFalse(result.conforms(), () -> "expected conforms=false, report=" + result.reportText());
        assertTrue(result.reportText() != null && !result.reportText().isBlank(),
                () -> "expected non-empty report, got=" + result.reportText());
        assertEquals("0.2.0", result.semanticPackageVersion());
    }

    @Test
    void validateWrongDatatypeCandidateIsNonConforming() {
        JenaSemanticRepositoryAdapter adapter = loadedAdapter();

        byte[] candidate = turtle(
                "@prefix ex: <http://example.com/> .",
                "ex:thing3 a ex:Thing ;",
                "    ex:name 42 .");

        SemanticRepositoryPort.ValidationResult result = adapter.validate(candidate);
        assertFalse(result.conforms(), () -> "expected conforms=false, report=" + result.reportText());
        assertTrue(result.reportText() != null && !result.reportText().isBlank());
    }

    @Test
    void validateNullCandidateIsFailClosedWithoutCrash() {
        JenaSemanticRepositoryAdapter adapter = loadedAdapter();

        SemanticRepositoryPort.ValidationResult result = adapter.validate(null);
        assertFalse(result.conforms(), () -> "expected conforms=false, report=" + result.reportText());
        assertTrue(result.reportText() != null && !result.reportText().isBlank());
        assertEquals("0.2.0", result.semanticPackageVersion());
    }

    @Test
    void validateEmptyCandidateIsFailClosedWithoutCrash() {
        JenaSemanticRepositoryAdapter adapter = loadedAdapter();

        SemanticRepositoryPort.ValidationResult result = adapter.validate(new byte[0]);
        assertFalse(result.conforms(), () -> "expected conforms=false, report=" + result.reportText());
        assertTrue(result.reportText() != null && !result.reportText().isBlank());
        assertEquals("0.2.0", result.semanticPackageVersion());
    }
}
