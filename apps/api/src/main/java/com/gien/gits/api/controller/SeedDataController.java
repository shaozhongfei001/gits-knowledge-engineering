package com.gien.gits.api.controller;

import com.gien.gits.api.dto.SeedDataLoadResponse;
import com.gien.gits.api.dto.SeedDataStatusResponse;
import com.gien.gits.api.service.ScenarioSeedDataService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 种子数据控制器 — 场景种子数据加载与状态查询
 */
@RestController
@RequestMapping("/api/v1/seed-data")
@ConditionalOnProperty(name = "gits.seed.enabled", havingValue = "true")
public class SeedDataController {

    private final ScenarioSeedDataService seedDataService;

    public SeedDataController(ScenarioSeedDataService seedDataService) {
        this.seedDataService = Objects.requireNonNull(seedDataService);
    }

    @PostMapping("/load")
    public ResponseEntity<SeedDataLoadResponse> loadSeedData() {
        if (seedDataService.isLoaded()) {
            return ResponseEntity.ok(new SeedDataLoadResponse("ALREADY_LOADED", "Seed data already exists"));
        }
        seedDataService.loadAll();
        return ResponseEntity.ok(new SeedDataLoadResponse("LOADED", "Scenario seed data loaded successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<SeedDataStatusResponse> checkSeedStatus() {
        return ResponseEntity.ok(new SeedDataStatusResponse(seedDataService.isLoaded()));
    }
}
