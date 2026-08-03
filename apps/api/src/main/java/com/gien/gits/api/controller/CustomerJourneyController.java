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

import com.gien.gits.adapter.persistence.scenario.JdbcCustomerJourneyRepository;
import com.gien.gits.api.service.CustomerJourneyService;
import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.customerjourney.JourneyPhase;

/**
 * 客户旅程REST控制器——开户、查询、推进
 */
@RestController
@RequestMapping("/api/journey")
public class CustomerJourneyController {

    private final CustomerJourneyService journeyService;
    private final JdbcCustomerJourneyRepository journeyRepo;

    public CustomerJourneyController(CustomerJourneyService journeyService,
                                     JdbcCustomerJourneyRepository journeyRepo) {
        this.journeyService = journeyService;
        this.journeyRepo = journeyRepo;
    }

    /** M17: 开户——创建CustomerJourney */
    @PostMapping("/open")
    public ResponseEntity<CustomerJourney> openJourney(@RequestBody OpenJourneyRequest req) {
        CustomerJourney journey = journeyService.openJourney(
                req.operatingCaseId(), req.customerId(), req.customerName(), req.signalDescription());
        return ResponseEntity.ok(journey);
    }

    /** 查询旅程详情 */
    @GetMapping("/{journeyId}")
    public ResponseEntity<CustomerJourney> getJourney(@PathVariable UUID journeyId) {
        CustomerJourney journey = journeyService.findJourneyById(journeyId);
        if (journey == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(journey);
    }

    /** 推进旅程阶段 */
    @PostMapping("/{journeyId}/advance")
    public ResponseEntity<CustomerJourney> advancePhase(@PathVariable UUID journeyId,
                                                         @RequestBody AdvancePhaseRequest req) {
        // 先查询当前旅程
        CustomerJourney current = journeyService.findJourneyById(journeyId);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        // 校验阶段推进是否合法（只允许相邻阶段推进）
        JourneyPhase target = req.targetPhase();
        if (!isValidTransition(current.phase(), target)) {
            return ResponseEntity.badRequest().build();
        }
        // 持久化阶段变更
        journeyRepo.updateJourneyPhase(journeyId, target);
        // 返回更新后的旅程
        CustomerJourney updated = journeyService.findJourneyById(journeyId);
        return ResponseEntity.ok(updated);
    }

    private boolean isValidTransition(JourneyPhase from, JourneyPhase to) {
        return switch (from) {
            case KYC_COLLECT -> to == JourneyPhase.INSIGHT_ANALYSIS;
            case INSIGHT_ANALYSIS -> to == JourneyPhase.PRODUCT_MATCHING;
            case PRODUCT_MATCHING -> to == JourneyPhase.PREVISIT_PREP;
            case PREVISIT_PREP -> to == JourneyPhase.POSTVISIT_REVIEW;
            case POSTVISIT_REVIEW -> to == JourneyPhase.COMPLETED;
            case COMPLETED -> false;
        };
    }

    // ── 请求DTO ──────────────────────────────────────────────

    public record OpenJourneyRequest(
            UUID operatingCaseId,
            String customerId,
            String customerName,
            String signalDescription) {}

    public record AdvancePhaseRequest(JourneyPhase targetPhase) {}
}