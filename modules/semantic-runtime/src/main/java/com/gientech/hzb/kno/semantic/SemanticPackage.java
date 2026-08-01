package com.gientech.hzb.kno.semantic;

import java.util.Arrays;

public record SemanticPackage(String packageId, String version, byte[] ontologyTurtle, byte[] shaclTurtle) {
    public SemanticPackage {
        if (packageId == null || packageId.isBlank() || version == null || version.isBlank()) {
            throw new IllegalArgumentException("packageId and version are required");
        }
        ontologyTurtle = Arrays.copyOf(ontologyTurtle, ontologyTurtle.length);
        shaclTurtle = Arrays.copyOf(shaclTurtle, shaclTurtle.length);
    }

    @Override public byte[] ontologyTurtle() { return Arrays.copyOf(ontologyTurtle, ontologyTurtle.length); }
    @Override public byte[] shaclTurtle() { return Arrays.copyOf(shaclTurtle, shaclTurtle.length); }
}
