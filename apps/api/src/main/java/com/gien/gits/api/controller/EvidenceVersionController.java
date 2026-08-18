package com.gien.gits.api.controller;

import com.gien.gits.api.dto.EvidenceVersionDto;
import com.gien.gits.ontology.domain.EvidenceVersionLink;
import com.gien.gits.ontology.port.EvidenceVersionLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evidences")
public class EvidenceVersionController {

    private static final Logger log = LoggerFactory.getLogger(EvidenceVersionController.class);

    private final EvidenceVersionLinkRepository evidenceVersionLinkRepository;

    public EvidenceVersionController(EvidenceVersionLinkRepository evidenceVersionLinkRepository) {
        this.evidenceVersionLinkRepository = evidenceVersionLinkRepository;
    }

    @GetMapping("/{evidenceId}/versions")
    public ResponseEntity<List<EvidenceVersionDto>> listEvidenceVersions(@PathVariable String evidenceId) {
        log.info("Listing evidence versions: evidenceId={}", evidenceId);
        var versions = evidenceVersionLinkRepository.findVersionChain(evidenceId);
        var dtos = versions.stream().map(EvidenceVersionDto::from).toList();
        return ResponseEntity.ok(dtos);
    }
}
