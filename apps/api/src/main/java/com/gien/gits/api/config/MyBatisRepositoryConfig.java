package com.gien.gits.api.config;

import com.gien.gits.adapter.persistence.foundation.action.mapper.RecordingConsentMapper;
import com.gien.gits.adapter.persistence.foundation.action.mapper.TaskMapper;
import com.gien.gits.adapter.persistence.foundation.action.service.MyBatisRecordingConsentService;
import com.gien.gits.adapter.persistence.foundation.action.service.MyBatisTaskService;
import com.gien.gits.adapter.persistence.foundation.engagement.mapper.MeetingScriptMapper;
import com.gien.gits.adapter.persistence.foundation.engagement.mapper.OutreachScriptMapper;
import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PostvisitAnalysisContentMapper;
import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PrevisitReportContentMapper;
import com.gien.gits.adapter.persistence.foundation.engagement.service.MyBatisMeetingScriptService;
import com.gien.gits.adapter.persistence.foundation.engagement.service.MyBatisOutreachScriptService;
import com.gien.gits.adapter.persistence.foundation.engagement.service.MyBatisPostvisitAnalysisContentService;
import com.gien.gits.adapter.persistence.foundation.engagement.service.MyBatisPrevisitReportContentService;
import com.gien.gits.adapter.persistence.foundation.journey.mapper.CustomerJourneyMapper;
import com.gien.gits.adapter.persistence.foundation.journey.service.MyBatisCustomerJourneyService;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.*;
import com.gien.gits.adapter.persistence.foundation.ontology.service.*;
import com.gien.gits.action.port.WritableRecordingConsentRepository;
import com.gien.gits.action.port.WritableTaskRepository;
import com.gien.gits.customerjourney.port.WritableCustomerJourneyRepository;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;
import com.gien.gits.ontology.port.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 仓储 Bean 注册 — 通过 {@code gits.persistence.mode=mybatis} 激活。
 *
 * <p>当 {@code gits.persistence.mode} 为 {@code mybatis} 时，
 * 本配置类注册所有 MyBatis Service Bean 替代 JDBC 实现。</p>
 *
 * <p>包结构遵循 {@code mybatis-integration-spec.md} 两层架构：</p>
 * <ul>
 *   <li>foundation/ontology — 领域基建 CRUD</li>
 *   <li>foundation/action — 行动模块</li>
 *   <li>foundation/engagement — 交互模块</li>
 *   <li>foundation/journey — 旅程模块</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "mybatis")
public class MyBatisRepositoryConfig {

    // ═══════════════════════════════════════════════════════════════
    // foundation/ontology
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public WritableExternalEventRepository mybatisExternalEventRepository(ExternalEventMapper mapper) {
        return new MyBatisExternalEventService(mapper);
    }

    @Bean
    public WritableOpportunitySignalRepository mybatisOpportunitySignalRepository(OpportunitySignalMapper mapper) {
        return new MyBatisOpportunitySignalService(mapper);
    }

    @Bean
    public WritableEvidenceVersionLinkRepository mybatisEvidenceVersionLinkRepository(EvidenceVersionLinkMapper mapper) {
        return new MyBatisEvidenceVersionLinkService(mapper);
    }

    @Bean
    public WritableOpportunityRepository mybatisOpportunityRepository(OpportunityMapper mapper) {
        return new MyBatisOpportunityService(mapper);
    }

    @Bean
    public WritableBankRelationshipSnapshotRepository mybatisBankRelationshipSnapshotRepository(BankRelationshipSnapshotMapper mapper) {
        return new MyBatisBankRelationshipSnapshotService(mapper);
    }

    @Bean
    public WritableTransactionRepository mybatisTransactionRepository(TransactionMapper mapper) {
        return new MyBatisTransactionService(mapper);
    }

    @Bean
    public WritableTransactionRecordRepository mybatisTransactionRecordRepository(TransactionRecordMapper mapper) {
        return new MyBatisTransactionRecordService(mapper);
    }

    @Bean
    public WritableCustomerRepository mybatisCustomerRepository(CustomerMapper mapper) {
        return new MyBatisCustomerService(mapper);
    }

    @Bean
    public WritableCreditFacilityRepository mybatisCreditFacilityRepository(CreditFacilityMapper mapper) {
        return new MyBatisCreditFacilityService(mapper);
    }

    @Bean
    public WritableLegalEntityRepository mybatisLegalEntityRepository(LegalEntityMapper mapper) {
        return new MyBatisLegalEntityService(mapper);
    }

    @Bean
    public WritableGroupRelationshipRepository mybatisGroupRelationshipRepository(GroupRelationshipMapper mapper) {
        return new MyBatisGroupRelationshipService(mapper);
    }

    @Bean
    public WritableClaimRepository mybatisClaimRepository(ClaimMapper mapper) {
        return new MyBatisClaimService(mapper);
    }

    @Bean
    public WritableClaimLifecycleRepository mybatisClaimLifecycleRepository(ClaimLifecycleEventMapper mapper) {
        return new MyBatisClaimLifecycleService(mapper);
    }

    @Bean
    public WritableKycGapProfileRepository mybatisKycGapProfileRepository(KycGapProfileMapper mapper) {
        return new MyBatisKycGapProfileService(mapper);
    }

    @Bean
    public WritablePolicyRuleRepository mybatisPolicyRuleRepository(PolicyRuleMapper mapper) {
        return new MyBatisPolicyRuleService(mapper);
    }

    @Bean
    public WritableProductCatalogRepository mybatisProductCatalogRepository(ProductKnowledgeCardMapper mapper) {
        return new MyBatisProductCatalogService(mapper);
    }

    @Bean
    public WritableProductKnowledgeVersionRepository mybatisProductKnowledgeVersionRepository(ProductKnowledgeVersionMapper mapper) {
        return new MyBatisProductKnowledgeVersionService(mapper);
    }

    @Bean
    public WritableInteractionRepository mybatisInteractionRepository(InteractionMapper mapper) {
        return new MyBatisInteractionService(mapper);
    }

    @Bean
    public WritableCommitmentRepository mybatisCommitmentRepository(CommitmentMapper mapper) {
        return new MyBatisCommitmentService(mapper);
    }

    @Bean
    public WritableOperatingCaseRepository mybatisOperatingCaseRepository(OperatingCaseMapper mapper) {
        return new MyBatisOperatingCaseService(mapper);
    }

    @Bean
    public WritableFactReconciliationRepository mybatisFactReconciliationRepository(FactReconciliationCaseMapper mapper) {
        return new MyBatisFactReconciliationService(mapper);
    }

    @Bean
    public WritableRelationshipReportRepository mybatisRelationshipReportRepository(RelationshipReportMapper mapper) {
        return new MyBatisRelationshipReportService(mapper);
    }

    // ═══════════════════════════════════════════════════════════════
    // foundation/engagement
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public WritablePrevisitReportContentRepository mybatisPrevisitReportContentRepository(PrevisitReportContentMapper mapper) {
        return new MyBatisPrevisitReportContentService(mapper);
    }

    @Bean
    public WritableMeetingScriptRepository mybatisMeetingScriptRepository(MeetingScriptMapper mapper) {
        return new MyBatisMeetingScriptService(mapper);
    }

    @Bean
    public WritableOutreachScriptRepository mybatisOutreachScriptRepository(OutreachScriptMapper mapper) {
        return new MyBatisOutreachScriptService(mapper);
    }

    @Bean
    public WritablePostvisitAnalysisContentRepository mybatisPostvisitAnalysisContentRepository(PostvisitAnalysisContentMapper mapper) {
        return new MyBatisPostvisitAnalysisContentService(mapper);
    }

    // ═══════════════════════════════════════════════════════════════
    // foundation/action
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public WritableTaskRepository mybatisTaskRepository(TaskMapper mapper) {
        return new MyBatisTaskService(mapper);
    }

    @Bean
    public WritableRecordingConsentRepository mybatisRecordingConsentRepository(RecordingConsentMapper mapper) {
        return new MyBatisRecordingConsentService(mapper);
    }

    // ═══════════════════════════════════════════════════════════════
    // foundation/journey
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public WritableCustomerJourneyRepository mybatisCustomerJourneyRepository(CustomerJourneyMapper mapper) {
        return new MyBatisCustomerJourneyService(mapper);
    }
}
