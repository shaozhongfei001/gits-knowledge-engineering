package com.gien.gits.semantic;

public interface SemanticRepositoryPort {
    void load(SemanticPackage semanticPackage);

    ValidationResult validate(byte[] candidateTurtle);

    record ValidationResult(boolean conforms, String reportText, String semanticPackageVersion) {}
}
