package com.gien.gits.semantic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SemanticPackageTest {

    private static final byte[] ONTOLOGY = "@prefix owl: <http://www.w3.org/2002/07/owl#> .".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SHAPES = "@prefix sh: <http://www.w3.org/ns/shacl#> .".getBytes(StandardCharsets.UTF_8);

    @Test
    void validConstruction() {
        SemanticPackage pkg = new SemanticPackage("gits-core", "1.0.0", ONTOLOGY, SHAPES);

        assertEquals("gits-core", pkg.packageId());
        assertEquals("1.0.0", pkg.version());
        assertArrayEquals(ONTOLOGY, pkg.ontologyTurtle());
        assertArrayEquals(SHAPES, pkg.shaclTurtle());
    }

    @Test
    void blankPackageIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage("  ", "1.0.0", ONTOLOGY, SHAPES));
    }

    @Test
    void blankVersionRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage("gits-core", "  ", ONTOLOGY, SHAPES));
    }

    @Test
    void nullPackageIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage(null, "1.0.0", ONTOLOGY, SHAPES));
    }

    @Test
    void nullVersionRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SemanticPackage("gits-core", null, ONTOLOGY, SHAPES));
    }
}
