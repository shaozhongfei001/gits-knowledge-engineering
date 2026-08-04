package com.gien.gits.api.controller;

import com.gien.gits.api.dto.KycGapProfileCreatedResponse;
import com.gien.gits.api.dto.SignalConfirmResponse;
import com.gien.gits.api.dto.SignalDismissResponse;
import com.gien.gits.api.service.KycInsightService;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.OpportunitySignal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * KYC与洞察控制器 — KYC缺口画像与机会信号管理
 */
@RestController
@RequestMapping("/api/v1/engagement")
public class KycInsightController {

    private final KycInsightService kycInsightService;

    public KycInsightController(KycInsightService kycInsightService) {
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
    }

    @GetMapping("/kyc/{customerId}/gap-profile")
    public ResponseEntity<KycGapProfile> getKycGapProfile(@PathVariable String customerId) {
        return kycInsightService.getKycGapProfile(customerId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/kyc/gap-profile")
    public ResponseEntity<KycGapProfileCreatedResponse> createKycGapProfile(@RequestBody KycGapProfile profile) {
        kycInsightService.saveKycGapProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new KycGapProfileCreatedResponse(profile.profileId(), "CREATED"));
    }

    @GetMapping("/signal/{operatingCaseId}")
    public ResponseEntity<List<OpportunitySignal>> getSignals(@PathVariable String operatingCaseId) {
        return ResponseEntity.ok(kycInsightService.getSignalsByCase(operatingCaseId));
    }

    @PostMapping("/signal/{signalId}/confirm")
    public ResponseEntity<SignalConfirmResponse> confirmSignal(@PathVariable String signalId) {
        kycInsightService.confirmSignal(UUID.fromString(signalId));
        return ResponseEntity.ok(new SignalConfirmResponse(signalId, "CONFIRMED"));
    }

    @PostMapping("/signal/{signalId}/dismiss")
    public ResponseEntity<SignalDismissResponse> dismissSignal(@PathVariable String signalId) {
        kycInsightService.dismissSignal(UUID.fromString(signalId));
        return ResponseEntity.ok(new SignalDismissResponse(signalId, "DISMISSED"));
    }
}
