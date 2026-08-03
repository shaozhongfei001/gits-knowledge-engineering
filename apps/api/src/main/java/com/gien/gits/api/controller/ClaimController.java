package com.gien.gits.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gien.gits.adapter.persistence.JdbcClaimRepository;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;

/**
 * 主张(Claim)REST控制器——创建、查询、状态变更
 */
@RestController
@RequestMapping("/api/claim")
public class ClaimController {

    private final JdbcClaimRepository claimRepo;

    public ClaimController(JdbcClaimRepository claimRepo) {
        this.claimRepo = claimRepo;
    }

    /** 创建主张 */
    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestBody Claim claim) {
        claimRepo.save(claim);
        return ResponseEntity.ok(claim);
    }

    /** 按ID查询主张 */
    @GetMapping("/{claimId}")
    public ResponseEntity<Claim> getClaim(@PathVariable UUID claimId) {
        return claimRepo.findById(claimId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 更新主张状态——人工确认推进 */
    @PostMapping("/{claimId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID claimId,
                                              @RequestBody UpdateStatusRequest req) {
        claimRepo.updateStatus(claimId, req.newStatus());
        return ResponseEntity.ok().build();
    }

    // ── 请求DTO ──────────────────────────────────────────────

    public record UpdateStatusRequest(ClaimStatus newStatus) {}
}