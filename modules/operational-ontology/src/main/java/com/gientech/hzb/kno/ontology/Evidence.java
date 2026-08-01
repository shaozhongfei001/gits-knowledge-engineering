package com.gientech.hzb.kno.ontology;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public record Evidence(UUID evidenceId, URI sourceUri, String sourceVersion, String locator, String contentHash, String permissionLabel) {
    public Evidence {
        Objects.requireNonNull(evidenceId, "evidenceId");
        Objects.requireNonNull(sourceUri, "sourceUri");
        if (sourceVersion == null || sourceVersion.isBlank() || locator == null || locator.isBlank()
                || contentHash == null || contentHash.isBlank() || permissionLabel == null || permissionLabel.isBlank()) {
            throw new IllegalArgumentException("sourceVersion, locator, contentHash and permissionLabel are required");
        }
    }
}
