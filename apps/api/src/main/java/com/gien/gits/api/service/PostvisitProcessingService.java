package com.gien.gits.api.service;

import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.api.service.report.*;
import com.gien.gits.engagement.*;
import com.gien.gits.ontology.*;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * 访后处理服务 — 会议记录处理 + 互动提取 + 事实对账 + 访后分析(R4)
 * 转录提取委托给 TranscriptExtractionStrategy
 */
public class PostvisitProcessingService {

    private final KycInsightService kycInsightService;
    private final WritableCommitmentRepository commitmentRepo;
    private final WritableFactReconciliationRepository factRecRepo;
    private final WritableOpportunitySignalRepository signalRepo;
    private final WritablePostvisitAnalysisContentRepository postvisitContentRepo;
    private final TranscriptExtractionStrategy extractionStrategy;

    public PostvisitProcessingService(
            KycInsightService kycInsightService,
            WritableCommitmentRepository commitmentRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            WritablePostvisitAnalysisContentRepository postvisitContentRepo,
            TranscriptExtractionStrategy extractionStrategy) {
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
        this.commitmentRepo = Objects.requireNonNull(commitmentRepo);
        this.factRecRepo = Objects.requireNonNull(factRecRepo);
        this.signalRepo = Objects.requireNonNull(signalRepo);
        this.postvisitContentRepo = Objects.requireNonNull(postvisitContentRepo);
        this.extractionStrategy = Objects.requireNonNull(extractionStrategy);
    }

    /**
     * 处理会议记录 — 委托策略提取结构化信息
     * 核心逻辑: AT-001 "3000万语义识别"
     */
    public MeetingTranscript processTranscript(
            String journeyId, String rawContent, String operatingCaseId) {

        if (rawContent == null || rawContent.isBlank()) {
            return new MeetingTranscript(
                "TR-" + UUID.randomUUID().toString().substring(0, 8),
                journeyId, rawContent, List.of(), List.of("无转录内容，跳过提取"), Instant.now());
        }

        List<InteractionExtraction> extractions = extractionStrategy.extract(rawContent);
        List<String> qualityNotes = validateExtractions(extractions);

        return new MeetingTranscript(
            "TR-" + UUID.randomUUID().toString().substring(0, 8),
            journeyId, rawContent, extractions, qualityNotes, Instant.now());
    }

    /**
     * 生成访后分析 (R4) — 基于提取结果 + 事实对账
     */
    @Transactional
    public PostvisitAnalysisContent generatePostvisitAnalysis(
            String journeyId, String operatingCaseId, MeetingTranscript transcript) {

        List<InteractionExtraction> keyFindings = transcript.extractions();

        // 识别机会信号 — 禁令#7: OpportunitySignal≠Opportunity
        List<PostvisitAnalysisContent.OpportunitySignalItem> signals = keyFindings.stream()
            .filter(e -> e.type() == InteractionExtraction.ExtractionType.OPPORTUNITY_SIGNAL)
            .map(e -> new PostvisitAnalysisContent.OpportunitySignalItem(
                e.claimType().name(), e.content(), e.speaker(),
                e.confidence(), true))  // notOpportunityYet = true (禁令#7)
            .toList();

        // 提取承诺
        List<PostvisitAnalysisContent.CommitmentItem> commitments = keyFindings.stream()
            .filter(e -> e.type() == InteractionExtraction.ExtractionType.CUSTOMER_COMMITMENT
                      || e.type() == InteractionExtraction.ExtractionType.BANK_COMMITMENT)
            .map(e -> new PostvisitAnalysisContent.CommitmentItem(
                e.type().name(), e.content(), e.speaker(), null))
            .toList();

        // 事实对账 — 对需要校验的提取项创建对账
        List<PostvisitAnalysisContent.FactReconciliationItem> recItems = keyFindings.stream()
            .filter(InteractionExtraction::requiresReconciliation)
            .map(e -> new PostvisitAnalysisContent.FactReconciliationItem(
                e.content(), e.evidenceRef(), e.content(),
                "需四维校验: 可用额度→项目主体→客户表达→事实来源",
                e.nextQuestion()))
            .toList();

        // 为每个需要校验的提取创建事实对账记录
        for (InteractionExtraction e : keyFindings) {
            if (e.requiresReconciliation()) {
                kycInsightService.createReconciliation(
                    operatingCaseId, e.content(),
                    "待确认", e.content(), "待外部验证",
                    List.of("OpportunitySignal≠Opportunity", "Claim≠Fact"),
                    "需人工确认后才能转为结构化Insight",
                    List.of("直接将'3000万'视为授信需求", "跳过事实校验"),
                    e.nextQuestion());
            }
        }

        // 为机会信号创建OpportunitySignal记录
        for (InteractionExtraction e : keyFindings) {
            if (e.type() == InteractionExtraction.ExtractionType.OPPORTUNITY_SIGNAL) {
                kycInsightService.detectSignal(
                    operatingCaseId, journeyId,
                    OpportunitySignal.SignalType.FINANCING_NEED,
                    e.content(), OpportunitySignal.SignalSourceType.INTERACTION,
                    e.evidenceRef(), e.confidence(), e.evidenceRef());
            }
        }

        // 为承诺创建Commitment记录
        for (PostvisitAnalysisContent.CommitmentItem c : commitments) {
            Commitment commitment = new Commitment(
                UUID.randomUUID(), operatingCaseId, journeyId,
                c.commitmentType().equals("CUSTOMER_COMMITMENT")
                    ? Commitment.CommitmentType.CUSTOMER_COMMITMENT
                    : Commitment.CommitmentType.BANK_COMMITMENT,
                c.content(), c.owner(), null,
                Commitment.CommitmentStatus.OPEN, null,
                Instant.now(), null);
            commitmentRepo.save(commitment);
        }

        List<String> followUpActions = deriveFollowUpActions(keyFindings, recItems);
        String nextStep = deriveNextStep(signals, recItems);

        PostvisitAnalysisContent result = new PostvisitAnalysisContent(
            "R4-" + UUID.randomUUID().toString().substring(0, 8),
            journeyId, "访后分析摘要",
            keyFindings, signals, commitments, recItems,
            followUpActions, nextStep);

        // P9 Loop G1: 持久化访后分析内容，供上下文继承使用
        postvisitContentRepo.save(result, operatingCaseId);

        return result;
    }

    // --- 私有辅助方法 ---

    private List<String> validateExtractions(List<InteractionExtraction> extractions) {
        List<String> notes = new ArrayList<>();
        for (InteractionExtraction e : extractions) {
            if (e.notFact()) {
                notes.add("WARNING: " + e.objectId() + " — Claim≠Fact, 需事实对账");
            }
            if (e.requiresReconciliation()) {
                notes.add("ACTION: " + e.objectId() + " — 需四维校验");
            }
        }
        return notes;
    }

    private List<String> deriveFollowUpActions(
            List<InteractionExtraction> keyFindings,
            List<PostvisitAnalysisContent.FactReconciliationItem> recItems) {
        List<String> actions = new ArrayList<>();
        actions.add("完成事实对账 (" + recItems.size() + "项)");
        actions.add("更新客户关系报告");
        actions.add("准备CRM回写 (需人工确认)");
        for (InteractionExtraction e : keyFindings) {
            if (e.nextQuestion() != null) {
                actions.add("跟进: " + e.nextQuestion());
            }
        }
        return actions;
    }

    private String deriveNextStep(
            List<PostvisitAnalysisContent.OpportunitySignalItem> signals,
            List<PostvisitAnalysisContent.FactReconciliationItem> recItems) {
        if (!recItems.isEmpty()) {
            return "优先完成事实对账, 确认信号真实性后再决定下一步";
        }
        if (!signals.isEmpty()) {
            return "信号待确认, 等待事实对账结果";
        }
        return "继续常规经营跟进";
    }
}
