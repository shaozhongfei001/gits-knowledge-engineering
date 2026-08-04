package com.gien.gits.api.controller;

import com.gien.gits.api.dto.JourneyCompleteResponse;
import com.gien.gits.api.dto.JourneyStartResponse;
import com.gien.gits.api.dto.NewEvidenceResponse;
import com.gien.gits.api.dto.PostvisitExecutionResponse;
import com.gien.gits.api.dto.PrevisitExecutionResponse;
import com.gien.gits.api.service.EngagementOrchestrator;
import com.gien.gits.api.service.MeetingScriptService;
import com.gien.gits.api.service.OutreachScriptService;
import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.port.OutreachScriptRepository;
import com.gien.gits.engagement.port.MeetingScriptRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 旅程管理控制器 — 持续经营旅程生命周期
 */
@RestController
@RequestMapping("/api/v1/engagement/journey")
public class EngagementJourneyController {

    private final EngagementOrchestrator orchestrator;
    private final OutreachScriptService outreachScriptService;
    private final MeetingScriptService meetingScriptService;
    private final OutreachScriptRepository outreachScriptRepository;
    private final MeetingScriptRepository meetingScriptRepository;

    public EngagementJourneyController(
            EngagementOrchestrator orchestrator,
            OutreachScriptService outreachScriptService,
            MeetingScriptService meetingScriptService,
            OutreachScriptRepository outreachScriptRepository,
            MeetingScriptRepository meetingScriptRepository) {
        this.orchestrator = Objects.requireNonNull(orchestrator);
        this.outreachScriptService = Objects.requireNonNull(outreachScriptService);
        this.meetingScriptService = Objects.requireNonNull(meetingScriptService);
        this.outreachScriptRepository = Objects.requireNonNull(outreachScriptRepository);
        this.meetingScriptRepository = Objects.requireNonNull(meetingScriptRepository);
    }

    @PostMapping("/start")
    public ResponseEntity<JourneyStartResponse> startJourney(@RequestBody StartJourneyRequest request) {
        CustomerJourney journey = orchestrator.startEngagementJourney(request.customerId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new JourneyStartResponse(
                journey.journeyId().toString(),
                journey.customerId(),
                journey.phase().name(),
                journey.startedAt().toString()));
    }

    @PostMapping("/{journeyId}/previsit")
    public ResponseEntity<PrevisitExecutionResponse> executePrevisit(
            @PathVariable String journeyId,
            @RequestBody PrevisitRequest request) {
        EngagementOrchestrator.PrevisitWorkflowResult result = orchestrator.executePrevisitPhase(
            journeyId, request.customerId(), request.operatingCaseId(), request.visitObjective());
        return ResponseEntity.ok(new PrevisitExecutionResponse(
            result.previsitReport(), result.battleCard()));
    }

    @PostMapping("/{journeyId}/postvisit")
    public ResponseEntity<PostvisitExecutionResponse> executePostvisit(
            @PathVariable String journeyId,
            @RequestBody PostvisitRequest request) {
        EngagementOrchestrator.PostvisitWorkflowResult result = orchestrator.executePostvisitPhase(
            journeyId, request.operatingCaseId(), request.customerId(), request.rawTranscript());
        return ResponseEntity.ok(new PostvisitExecutionResponse(
            result.transcript().transcriptId(),
            result.analysis().analysisId(),
            result.internalReport().reportId().toString(),
            result.crmReport().reportId().toString(),
            result.crmCommands().size(),
            true));
    }

    @PostMapping("/{journeyId}/new-evidence")
    public ResponseEntity<NewEvidenceResponse> handleNewEvidence(
            @PathVariable String journeyId,
            @RequestBody NewEvidenceRequest request) {
        EngagementOrchestrator.NewEvidenceWorkflowResult result = orchestrator.handleNewEvidence(
            journeyId, request.operatingCaseId(), request.customerId(),
            request.evidenceDescription(), request.previousReportId());
        return ResponseEntity.ok(new NewEvidenceResponse(
            result.updatedReport().reportId().toString(),
            result.nextPrevisitReport().reportId().toString()));
    }

    @PostMapping("/{journeyId}/complete")
    public ResponseEntity<JourneyCompleteResponse> completeJourney(@PathVariable String journeyId) {
        orchestrator.completeJourney(journeyId);
        return ResponseEntity.ok(new JourneyCompleteResponse("COMPLETED", journeyId));
    }

    // --- P9 Loop G4: 外联脚本生成 ---

    @PostMapping("/outreach-script")
    public ResponseEntity<OutreachScript> generateOutreachScript(
            @RequestBody OutreachScriptRequest request) {
        OutreachScript script = outreachScriptService.generateScript(
            request.customerId(), request.rmId(),
            request.operatingCaseId(), request.journeyId(),
            OutreachChannel.valueOf(request.channel()));
        return ResponseEntity.ok(script);
    }

    // --- P9 Loop G5: 会面脚本生成 ---

    @PostMapping("/meeting-script")
    public ResponseEntity<MeetingScript> generateMeetingScript(
            @RequestBody MeetingScriptRequest request) {
        MeetingScript script = meetingScriptService.generateScript(
            request.customerId(), request.rmId(),
            request.operatingCaseId(), request.journeyId());
        return ResponseEntity.ok(script);
    }

    // --- P10: 脚本持久化查询 ---

    @GetMapping("/outreach-scripts")
    public ResponseEntity<List<OutreachScript>> listOutreachScripts(
            @RequestParam String customerId) {
        return ResponseEntity.ok(outreachScriptRepository.findByCustomerId(customerId));
    }

    @GetMapping("/meeting-scripts")
    public ResponseEntity<List<MeetingScript>> listMeetingScripts(
            @RequestParam String customerId) {
        return ResponseEntity.ok(meetingScriptRepository.findByCustomerId(customerId));
    }

    // ========== 请求DTO ==========

    public record StartJourneyRequest(String customerId) {}
    public record PrevisitRequest(String customerId, String operatingCaseId, String visitObjective) {}
    public record PostvisitRequest(String customerId, String operatingCaseId, String rawTranscript) {}
    public record NewEvidenceRequest(String customerId, String operatingCaseId, String evidenceDescription, String previousReportId) {}
    public record OutreachScriptRequest(String customerId, String rmId, String operatingCaseId, String journeyId, String channel) {}
    public record MeetingScriptRequest(String customerId, String rmId, String operatingCaseId, String journeyId) {}
}
