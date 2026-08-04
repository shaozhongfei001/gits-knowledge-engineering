package com.gien.gits.api.controller;

import com.gien.gits.api.dto.ExternalEventCreatedResponse;
import com.gien.gits.api.dto.PolicyRuleCreatedResponse;
import com.gien.gits.api.dto.ProductCreatedResponse;
import com.gien.gits.api.service.KycInsightService;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.ProductKnowledgeCard;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 知识与规则控制器 — 产品目录、政策规则与外部事件管理
 */
@RestController
@RequestMapping("/api/v1/engagement")
public class KnowledgeRuleController {

    private final KycInsightService kycInsightService;

    public KnowledgeRuleController(KycInsightService kycInsightService) {
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
    }

    @GetMapping("/product")
    public ResponseEntity<List<ProductKnowledgeCard>> listProducts() {
        return ResponseEntity.ok(kycInsightService.getAllProducts());
    }

    @PostMapping("/product")
    public ResponseEntity<ProductCreatedResponse> createProduct(@RequestBody ProductKnowledgeCard card) {
        kycInsightService.saveProduct(card);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ProductCreatedResponse(card.productId(), "CREATED"));
    }

    @GetMapping("/policy-rule")
    public ResponseEntity<List<PolicyRule>> listPolicyRules() {
        return ResponseEntity.ok(kycInsightService.getAllPolicyRules());
    }

    @PostMapping("/policy-rule")
    public ResponseEntity<PolicyRuleCreatedResponse> createPolicyRule(@RequestBody PolicyRule rule) {
        kycInsightService.savePolicyRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new PolicyRuleCreatedResponse(rule.ruleId(), "CREATED"));
    }

    @GetMapping("/external-event")
    public ResponseEntity<List<ExternalEvent>> listRecentEvents(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(kycInsightService.getRecentExternalEvents(limit));
    }

    @PostMapping("/external-event")
    public ResponseEntity<ExternalEventCreatedResponse> createExternalEvent(@RequestBody ExternalEvent event) {
        kycInsightService.saveExternalEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ExternalEventCreatedResponse(event.eventId(), "CREATED"));
    }
}
