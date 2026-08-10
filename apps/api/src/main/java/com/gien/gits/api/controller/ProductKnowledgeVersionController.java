package com.gien.gits.api.controller;

import com.gien.gits.api.service.ProductKnowledgeVersionService;
import com.gien.gits.ontology.domain.ProductKnowledgeVersion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 产品知识版本控制器 — 产品信息版本化管理
 */
@RestController
@RequestMapping("/api/v1/product-knowledge")
public class ProductKnowledgeVersionController {

    private final ProductKnowledgeVersionService versionService;

    public ProductKnowledgeVersionController(ProductKnowledgeVersionService versionService) {
        this.versionService = Objects.requireNonNull(versionService);
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ProductKnowledgeVersion> getByVersionId(@PathVariable String versionId) {
        return versionService.findByVersionId(versionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "productId")
    public ResponseEntity<List<ProductKnowledgeVersion>> getByProductId(@RequestParam String productId) {
        return ResponseEntity.ok(versionService.findByProductId(productId));
    }

    @GetMapping(params = "productId", headers = "X-Latest=true")
    public ResponseEntity<ProductKnowledgeVersion> getLatestByProductId(@RequestParam String productId) {
        return versionService.findLatestByProductId(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest/{productId}")
    public ResponseEntity<ProductKnowledgeVersion> getLatest(@PathVariable String productId) {
        return versionService.findLatestByProductId(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "category")
    public ResponseEntity<List<ProductKnowledgeVersion>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(versionService.findByCategory(category));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ProductKnowledgeVersion>> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(versionService.findRecentVersions(limit));
    }

    @PostMapping
    public ResponseEntity<ProductKnowledgeVersion> create(@RequestBody ProductKnowledgeVersion version) {
        return ResponseEntity.status(HttpStatus.CREATED).body(versionService.create(version));
    }
}
