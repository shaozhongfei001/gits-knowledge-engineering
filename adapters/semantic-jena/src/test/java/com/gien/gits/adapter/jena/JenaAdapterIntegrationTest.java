package com.gien.gits.adapter.jena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.semantic.SemanticPackage;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SemanticPackageIntegrationTest {

    private static final byte[] ONTOLOGY = "@prefix owl: <http://www.w3.org/2002/07/owl#> .".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SHAPES = "@prefix sh: <http://www.w3.org/ns/shacl#> .".getBytes(StandardCharsets.UTF_8);

    @Test
    void packageWithRealTurtleContent() {
        String turtleContent = """
                @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix gits: <http://gien.com/gits/ontology#> .

                gits:Claim a owl:Class ;
                    rdfs:label "Claim" .
                """;

        SemanticPackage pkg = new SemanticPackage("gits-ontology", "1.0.0",
                turtleContent.getBytes(StandardCharsets.UTF_8), SHAPES);

        assertEquals("gits-ontology", pkg.packageId());
        assertNotNull(pkg.ontologyTurtle());
        assertNotNull(pkg.shaclTurtle());
    }

    @Test
    void packageWithRealShapesContent() {
        String shapesContent = """
                @prefix sh: <http://www.w3.org/ns/shacl#> .
                @prefix gits: <http://gien.com/gits/ontology#> .

                gits:ClaimShape a sh:NodeShape ;
                    sh:targetClass gits:Claim ;
                    sh:property [
                        sh:path gits:status ;
                        sh:minCount 1 ;
                    ] .
                """;

        SemanticPackage pkg = new SemanticPackage("gits-shapes", "1.0.0",
                ONTOLOGY, shapesContent.getBytes(StandardCharsets.UTF_8));

        assertNotNull(pkg.shaclTurtle());
        assertTrue(pkg.shaclTurtle().length > 0);
    }

    @Test
    void packageEquality() {
        SemanticPackage pkg1 = new SemanticPackage("eq-pkg", "1.0.0", ONTOLOGY, SHAPES);
        SemanticPackage pkg2 = new SemanticPackage("eq-pkg", "1.0.0", ONTOLOGY, SHAPES);

        assertEquals(pkg1.packageId(), pkg2.packageId());
        assertEquals(pkg1.version(), pkg2.version());
    }
}
