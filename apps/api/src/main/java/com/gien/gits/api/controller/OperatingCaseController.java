package com.gien.gits.api.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gien.gits.ontology.port.WritableOperatingCaseRepository;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.OperatingCase;

/**
 * 运营案例(OperatingCase)REST控制器——创建、查询
 */
@RestController
@RequestMapping("/api/case")
public class OperatingCaseController {

    private final WritableOperatingCaseRepository caseRepo;

    public OperatingCaseController(WritableOperatingCaseRepository caseRepo) {
        this.caseRepo = caseRepo;
    }

    /** 创建运营案例 */
    @PostMapping
    public ResponseEntity<OperatingCaseResponse> createCase(@RequestBody CreateCaseRequest req) {
        UUID caseId = req.caseId() != null ? req.caseId() : UUID.randomUUID();
        Instant now = Instant.now();
        OperatingCase operatingCase = new OperatingCase(
                caseId,
                CaseType.valueOf(req.caseType()),
                req.status() != null ? req.status() : CaseStatus.OPEN,
                req.purpose(),
                req.validFrom() != null ? req.validFrom() : now,
                req.validTo(),
                now,
                req.createdBy()
        );
        caseRepo.save(operatingCase);
        return ResponseEntity.ok(toResponse(operatingCase));
    }

    /** 按ID查询运营案例 */
    @GetMapping("/{caseId}")
    public ResponseEntity<OperatingCaseResponse> getCase(@PathVariable UUID caseId) {
        return caseRepo.findById(caseId)
                .map(oc -> ResponseEntity.ok(toResponse(oc)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 请求/响应DTO ──────────────────────────────────────────────

    /** 创建案例请求——不直接暴露域对象 */
    public record CreateCaseRequest(
            UUID caseId,
            String caseType,
            CaseStatus status,
            String purpose,
            Instant validFrom,
            Instant validTo,
            String createdBy) {}

    /** 查询案例响应——不直接暴露域对象 */
    public record OperatingCaseResponse(
            UUID caseId,
            String caseType,
            CaseStatus status,
            String purpose,
            Instant validFrom,
            Instant validTo,
            Instant recordedAt,
            String createdBy) {}

    private static OperatingCaseResponse toResponse(OperatingCase oc) {
        return new OperatingCaseResponse(
                oc.caseId(), oc.caseType().name(), oc.status(), oc.purpose(),
                oc.validFrom(), oc.validTo(), oc.recordedAt(), oc.createdBy());
    }
}