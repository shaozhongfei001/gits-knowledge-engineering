package com.gien.gits.api.config;

import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.action.port.WritableRecordingConsentRepository;
import com.gien.gits.action.port.WritableTaskRepository;
import com.gien.gits.adapter.audit.LoggingAuditLogAdapter;
import com.gien.gits.adapter.persistence.v11.JdbcOpportunityRepository;
import com.gien.gits.adapter.persistence.v11.JdbcProductKnowledgeVersionRepository;
import com.gien.gits.adapter.persistence.v11.JdbcRecordingConsentRepository;
import com.gien.gits.adapter.persistence.v11.JdbcTaskRepository;
import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.api.service.*;
import com.gien.gits.api.service.report.CrmWritebackService;
import com.gien.gits.api.service.report.SemanticPatternExtractionStrategy;
import com.gien.gits.adapter.crm.HttpCrmWritebackChannel;
import com.gien.gits.adapter.crm.LoggingCrmWritebackChannel;
import com.gien.gits.adapter.dmn.DmnClaimReconciliationAdapter;
import com.gien.gits.adapter.dmn.FallbackClaimReconciliationAdapter;
import com.gien.gits.adapter.llm.MockLlmClient;
import com.gien.gits.adapter.llm.RealLlmClient;
import com.gien.gits.adapter.oracle.StubOracleSourceAdapter;
import com.gien.gits.adapter.persistence.scenario.JdbcOutreachScriptRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcMeetingScriptRepository;
import com.gien.gits.customerjourney.port.WritableCustomerJourneyRepository;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.ontology.port.ScenarioDataProvider;
import com.gien.gits.ontology.port.WritableBankRelationshipSnapshotRepository;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.ontology.port.WritableCreditFacilityRepository;
import com.gien.gits.ontology.port.WritableCustomerRepository;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableGroupRelationshipRepository;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;
import com.gien.gits.ontology.port.WritableLegalEntityRepository;
import com.gien.gits.ontology.port.WritableOperatingCaseRepository;
import com.gien.gits.ontology.port.WritableOpportunityRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.ontology.port.WritablePolicyRuleRepository;
import com.gien.gits.ontology.port.WritableProductKnowledgeVersionRepository;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.PostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;
import com.gien.gits.ontology.port.WritableProductCatalogRepository;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;
import com.gien.gits.ontology.port.WritableTransactionRecordRepository;
import com.gien.gits.ontology.port.OracleSourcePort;
import com.gien.gits.ontology.port.WritableTransactionRepository;
import com.gien.gits.evaluation.EvaluationPort;
import com.gien.gits.evaluation.DefaultEvaluator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 持续经营服务配置 — 注册所有业务服务Bean
 * 通过 engagement.scenario.enabled=true 开启（默认开启）
 */
@Configuration
@ConditionalOnProperty(name = "engagement.scenario.enabled", havingValue = "true", matchIfMissing = true)
public class EngagementConfig {

    @Bean
    public CustomerContextService customerContextService(
            WritableCustomerRepository customerRepo,
            WritableLegalEntityRepository legalEntityRepo,
            WritableGroupRelationshipRepository groupRelRepo,
            WritableBankRelationshipSnapshotRepository bankRelRepo,
            WritableCreditFacilityRepository creditFacilityRepo,
            WritableTransactionRecordRepository transactionRepo) {
        return new CustomerContextService(
            customerRepo, legalEntityRepo, groupRelRepo,
            bankRelRepo, creditFacilityRepo, transactionRepo);
    }

    @Bean
    public KycInsightService kycInsightService(
            WritableKycGapProfileRepository kycGapRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            WritableExternalEventRepository externalEventRepo,
            WritableProductCatalogRepository productCatalogRepo,
            WritablePolicyRuleRepository policyRuleRepo,
            ClaimReconciliationPort claimReconciliationPort,
            DomainEventPublisher domainEventPublisher,
            BusinessMetrics businessMetrics) {
        return new KycInsightService(
            kycGapRepo, factRecRepo, signalRepo,
            externalEventRepo, productCatalogRepo, policyRuleRepo,
            claimReconciliationPort, domainEventPublisher, businessMetrics);
    }

    // --- P16 G10: 审计日志 ---

    @Bean
    public AuditLogPort auditLogPort() {
        return new LoggingAuditLogAdapter();
    }

    // --- P11 G3: DMN决策引擎集成 ---

    @Bean
    public ClaimReconciliationPort claimReconciliationPort(BusinessMetrics businessMetrics) {
        // 默认使用FallbackClaimReconciliationAdapter(手写决策表逻辑)
        // 切换到DmnClaimReconciliationAdapter可启用DMN XML解析
        return new FallbackClaimReconciliationAdapter(businessMetrics);
    }

    // --- P11 G1: LLM语义提取引擎 ---

    @Bean
    public LlmClient llmClient(@Value("${engagement.llm.mode:mock}") String mode,
                                Environment env,
                                BusinessMetrics businessMetrics) {
        return switch (mode.toLowerCase()) {
            case "real" -> new RealLlmClient(
                    env.getProperty("engagement.llm.base-url", "https://api.openai.com"),
                    env.getProperty("engagement.llm.api-key", ""),
                    env.getProperty("engagement.llm.model", "gpt-4o-mini"),
                    // P16 G2: 连接超时5s，读取超时30s
                    Integer.parseInt(env.getProperty("engagement.llm.connect-timeout-ms", "5000")),
                    Integer.parseInt(env.getProperty("engagement.llm.read-timeout-ms", "30000")),
                    // P16 G2: 重试配置(3次，指数退避)
                    Integer.parseInt(env.getProperty("engagement.llm.retry.max-attempts", "3")),
                    Long.parseLong(env.getProperty("engagement.llm.retry.initial-delay-ms", "1000")),
                    Double.parseDouble(env.getProperty("engagement.llm.retry.backoff-multiplier", "2")),
                    // P16 G2: 熔断器配置(5次失败后开启，30s后半开)
                    Integer.parseInt(env.getProperty("engagement.llm.circuit-breaker.failure-threshold", "5")),
                    Long.parseLong(env.getProperty("engagement.llm.circuit-breaker.half-open-delay-ms", "30000")),
                    businessMetrics
            );
            default -> new MockLlmClient(businessMetrics);
        };
    }

    @Bean
    public SemanticPatternExtractionStrategy semanticPatternExtractionStrategy(LlmClient llmClient) {
        return new SemanticPatternExtractionStrategy(llmClient);
    }

    // --- P11 G4: CRM回写通道集成 ---

    @Bean
    @ConditionalOnProperty(name = "engagement.crm.mode", havingValue = "http")
    public CrmWritebackChannel httpCrmWritebackChannel(
            RestClient.Builder restClientBuilder,
            @Value("${engagement.crm.writeback-url:}") String writebackUrl,
            @Value("${engagement.crm.auth-token:}") String authToken,
            Environment env,
            BusinessMetrics businessMetrics) {
        // P16 G3: 超时(5s连接，10s读取)、重试(2次)
        return new HttpCrmWritebackChannel(
                restClientBuilder, writebackUrl, authToken,
                Integer.parseInt(env.getProperty("engagement.crm.connect-timeout-ms", "5000")),
                Integer.parseInt(env.getProperty("engagement.crm.read-timeout-ms", "10000")),
                Integer.parseInt(env.getProperty("engagement.crm.retry.max-attempts", "2")),
                Long.parseLong(env.getProperty("engagement.crm.retry.delay-ms", "500")),
                businessMetrics);
    }

    @Bean
    @ConditionalOnProperty(name = "engagement.crm.mode", havingValue = "logging", matchIfMissing = true)
    public CrmWritebackChannel loggingCrmWritebackChannel(BusinessMetrics businessMetrics) {
        return new LoggingCrmWritebackChannel(businessMetrics);
    }

    @Bean
    public CrmWritebackService crmWritebackService(CrmWritebackChannel crmWritebackChannel,
                                                    DomainEventPublisher domainEventPublisher) {
        return new CrmWritebackService(crmWritebackChannel, domainEventPublisher);
    }

    @Bean
    public PrevisitWorkflowService previsitWorkflowService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            WritableCommitmentRepository commitmentRepo) {
        return new PrevisitWorkflowService(
            customerContextService, kycInsightService, commitmentRepo);
    }

    @Bean
    public PostvisitProcessingService postvisitProcessingService(
            KycInsightService kycInsightService,
            WritableCommitmentRepository commitmentRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            WritablePostvisitAnalysisContentRepository postvisitContentRepo,
            SemanticPatternExtractionStrategy extractionStrategy) {
        return new PostvisitProcessingService(
            kycInsightService, commitmentRepo, factRecRepo, signalRepo, postvisitContentRepo, extractionStrategy);
    }

    @Bean
    public ReportGenerationService reportGenerationService(
            WritableRelationshipReportRepository reportRepo,
            WritableCommitmentRepository commitmentRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            CustomerContextService customerContextService,
            ContextInheritanceService contextInheritanceService,
            CustomerOperatingViewService customerOperatingViewService,
            CrmWritebackService crmWritebackService,
            LlmClient llmClient) {
        return new ReportGenerationService(
            reportRepo, commitmentRepo, factRecRepo,
            signalRepo, customerContextService, contextInheritanceService,
            customerOperatingViewService, crmWritebackService, llmClient);
    }

    @Bean
    public EngagementOrchestrator engagementOrchestrator(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            PrevisitWorkflowService previsitService,
            PostvisitProcessingService postvisitService,
            ReportGenerationService reportService,
            CustomerJourneyService journeyService,
            PostvisitAnalysisContentRepository analysisContentRepo,
            WritableCustomerJourneyRepository journeyRepo,
            WritableOperatingCaseRepository operatingCaseRepo,
            DomainEventPublisher domainEventPublisher,
            BusinessMetrics businessMetrics) {
        return new EngagementOrchestrator(
            customerContextService, kycInsightService,
            previsitService, postvisitService, reportService,
            journeyService, analysisContentRepo, journeyRepo, operatingCaseRepo,
            domainEventPublisher, businessMetrics);
    }

    @Bean
    @ConditionalOnProperty(name = "gits.seed.enabled", havingValue = "true", matchIfMissing = false)
    public ScenarioSeedDataService scenarioSeedDataService(
            WritableCustomerRepository customerRepo,
            WritableLegalEntityRepository legalEntityRepo,
            WritableGroupRelationshipRepository groupRelRepo,
            WritableBankRelationshipSnapshotRepository bankRelRepo,
            WritableCreditFacilityRepository creditFacilityRepo,
            WritableTransactionRecordRepository transactionRepo,
            WritableTransactionRepository transactionFlowRepo,
            WritableProductCatalogRepository productCatalogRepo,
            WritablePolicyRuleRepository policyRuleRepo,
            WritableExternalEventRepository externalEventRepo,
            WritableKycGapProfileRepository kycGapRepo,
            JdbcTemplate jdbcTemplate,
            ScenarioDataProvider dataProvider) {
        return new ScenarioSeedDataService(
            customerRepo, legalEntityRepo, groupRelRepo,
            bankRelRepo, creditFacilityRepo, transactionRepo,
            transactionFlowRepo, productCatalogRepo, policyRuleRepo,
            externalEventRepo, kycGapRepo, jdbcTemplate, dataProvider);
    }

    // --- P13 G6: Oracle只读管道 (默认不可用) ---

    @Bean
    @ConditionalOnProperty(name = "oracle.source.enabled", havingValue = "false", matchIfMissing = true)
    public OracleSourcePort stubOracleSourcePort() {
        return new StubOracleSourceAdapter();
    }

    // --- P9 Loop G2: 上下文继承服务 ---

    @Bean
    public ContextInheritanceService contextInheritanceService(
            WritablePostvisitAnalysisContentRepository postvisitContentRepo,
            WritablePrevisitReportContentRepository previsitContentRepo) {
        return new ContextInheritanceService(postvisitContentRepo, previsitContentRepo);
    }

    // --- P9 Loop G4+G5: 脚本生成服务 ---

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public JdbcOutreachScriptRepository jdbcOutreachScriptRepository(
            JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new JdbcOutreachScriptRepository(jdbcTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public JdbcMeetingScriptRepository jdbcMeetingScriptRepository(
            JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new JdbcMeetingScriptRepository(jdbcTemplate, objectMapper);
    }

    @Bean
    public OutreachScriptService outreachScriptService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            CustomerJourneyService journeyService,
            WritableOutreachScriptRepository outreachScriptRepo,
            LlmClient llmClient) {
        return new OutreachScriptService(customerContextService, kycInsightService, journeyService, outreachScriptRepo, llmClient);
    }

    @Bean
    public MeetingScriptService meetingScriptService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            CustomerJourneyService journeyService,
            WritableMeetingScriptRepository meetingScriptRepo,
            LlmClient llmClient) {
        return new MeetingScriptService(customerContextService, kycInsightService, journeyService, meetingScriptRepo, llmClient);
    }

    @Bean
    public PrevisitPreparationService previsitPreparationService(
            OutreachScriptService outreachScriptService,
            MeetingScriptService meetingScriptService,
            EngagementOrchestrator orchestrator) {
        return new PrevisitPreparationService(outreachScriptService, meetingScriptService, orchestrator);
    }

    // --- P9 Loop G6: 客户经营视图服务 ---

    @Bean
    public CustomerOperatingViewService customerOperatingViewService(
            com.gien.gits.ontology.port.CustomerRepository customerRepo,
            com.gien.gits.ontology.port.InteractionRepository interactionRepo,
            com.gien.gits.ontology.port.ClaimRepository claimRepo,
            com.gien.gits.ontology.port.KycGapProfileRepository kycGapRepo,
            com.gien.gits.ontology.port.OpportunitySignalRepository signalRepo,
            com.gien.gits.ontology.port.FactReconciliationRepository factRecRepo,
            com.gien.gits.customerjourney.port.CustomerJourneyRepository journeyRepo) {
        return new CustomerOperatingViewService(
            customerRepo, interactionRepo, claimRepo,
            kycGapRepo, signalRepo, factRecRepo, journeyRepo);
    }

    // --- P9 Loop G7: 产品匹配服务 ---

    @Bean
    public ProductMatchingService productMatchingService(
            com.gien.gits.ontology.port.TransactionRepository transactionRepo,
            WritableCustomerRepository customerRepo,
            KycInsightService kycInsightService) {
        return new ProductMatchingService(transactionRepo, customerRepo, kycInsightService);
    }

    // --- P14 Loop G5: Evaluation评分服务 ---

    @Bean
    public EvaluationPort evaluationPort() {
        return new DefaultEvaluator();
    }

    // ═══════════════════════════════════════════════════════════════
    // V1.1 新增服务Bean
    // ═══════════════════════════════════════════════════════════════

    // --- V1.1 JDBC仓储适配器 (仅新增的，其余在RepositoryConfig中注册) ---

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public WritableOpportunityRepository jdbcOpportunityRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcOpportunityRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public WritableTaskRepository jdbcTaskRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcTaskRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public WritableRecordingConsentRepository jdbcRecordingConsentRepository(JdbcTemplate jdbc) {
        return new JdbcRecordingConsentRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
    public WritableProductKnowledgeVersionRepository jdbcProductKnowledgeVersionRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcProductKnowledgeVersionRepository(jdbc, objectMapper);
    }

    // --- V1.1 业务服务 ---

    @Bean
    public CommitmentService commitmentService(WritableCommitmentRepository commitmentRepo) {
        return new CommitmentService(commitmentRepo);
    }

    @Bean
    public TaskService taskService(WritableTaskRepository taskRepo) {
        return new TaskService(taskRepo);
    }

    @Bean
    public OpportunityService opportunityService(WritableOpportunityRepository opportunityRepo) {
        return new OpportunityService(opportunityRepo);
    }

    @Bean
    public ExternalEventService externalEventService(WritableExternalEventRepository externalEventRepo) {
        return new ExternalEventService(externalEventRepo);
    }

    @Bean
    public ProductKnowledgeVersionService productKnowledgeVersionService(WritableProductKnowledgeVersionRepository versionRepo) {
        return new ProductKnowledgeVersionService(versionRepo);
    }

    @Bean
    public RecordingConsentService recordingConsentService(WritableRecordingConsentRepository consentRepo) {
        return new RecordingConsentService(consentRepo);
    }
}
