package com.gien.gits.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/architecture")
public class ArchitectureStatusController {

    @GetMapping("/status")
    ArchitectureStatus status() {
        return new ArchitectureStatus("GITS-KNO-DEV-PACKAGE-V0.1", "DEV_PACKAGE_CANDIDATE", false, false);
    }

    record ArchitectureStatus(String packageId, String state, boolean productionReady, boolean frozen) {}
}
