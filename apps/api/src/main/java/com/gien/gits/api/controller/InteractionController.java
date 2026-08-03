package com.gien.gits.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gien.gits.adapter.persistence.JdbcInteractionRepository;
import com.gien.gits.ontology.Interaction;

/**
 * 交互记录REST控制器——创建、查询
 */
@RestController
@RequestMapping("/api/interaction")
public class InteractionController {

    private final JdbcInteractionRepository interactionRepo;

    public InteractionController(JdbcInteractionRepository interactionRepo) {
        this.interactionRepo = interactionRepo;
    }

    /** 创建交互记录 */
    @PostMapping
    public ResponseEntity<Interaction> createInteraction(@RequestBody Interaction interaction) {
        interactionRepo.save(interaction);
        return ResponseEntity.ok(interaction);
    }

    /** 按ID查询交互 */
    @GetMapping("/{interactionId}")
    public ResponseEntity<Interaction> getInteraction(@PathVariable UUID interactionId) {
        return interactionRepo.findById(interactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 按案例ID查询交互列表 */
    @GetMapping
    public ResponseEntity<List<Interaction>> listByCaseId(@RequestParam UUID caseId) {
        List<Interaction> interactions = interactionRepo.findByCaseId(caseId);
        return ResponseEntity.ok(interactions);
    }
}