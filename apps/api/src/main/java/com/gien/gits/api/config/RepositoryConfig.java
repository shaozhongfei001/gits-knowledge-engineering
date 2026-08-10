package com.gien.gits.api.config;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gien.gits.adapter.persistence.JdbcBankRelationshipSnapshotRepository;
import com.gien.gits.adapter.persistence.JdbcClaimRepository;
import com.gien.gits.adapter.persistence.v11.JdbcCommitmentRepository;
import com.gien.gits.adapter.persistence.JdbcCreditFacilityRepository;
import com.gien.gits.adapter.persistence.JdbcCustomerRepository;
import com.gien.gits.adapter.persistence.v11.JdbcExternalEventRepository;
import com.gien.gits.adapter.persistence.JdbcFactReconciliationRepository;
import com.gien.gits.adapter.persistence.JdbcGroupRelationshipRepository;
import com.gien.gits.adapter.persistence.JdbcInteractionRepository;
import com.gien.gits.adapter.persistence.v11.JdbcKycGapProfileRepository;
import com.gien.gits.adapter.persistence.JdbcLegalEntityRepository;
import com.gien.gits.adapter.persistence.JdbcOperatingCaseRepository;
import com.gien.gits.adapter.persistence.JdbcOpportunitySignalRepository;
import com.gien.gits.adapter.persistence.JdbcPolicyRuleRepository;
import com.gien.gits.adapter.persistence.JdbcProductCatalogRepository;
import com.gien.gits.adapter.persistence.JdbcRelationshipReportRepository;
import com.gien.gits.adapter.persistence.JdbcTransactionRecordRepository;
import com.gien.gits.adapter.persistence.JdbcTransactionRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcCustomerJourneyRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcPostvisitAnalysisContentRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcPrevisitReportContentRepository;
import com.gien.gits.customerjourney.port.WritableCustomerJourneyRepository;
import com.gien.gits.ontology.port.WritableBankRelationshipSnapshotRepository;
import com.gien.gits.ontology.port.WritableClaimRepository;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.ontology.port.WritableCreditFacilityRepository;
import com.gien.gits.ontology.port.WritableCustomerRepository;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableGroupRelationshipRepository;
import com.gien.gits.ontology.port.WritableInteractionRepository;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;
import com.gien.gits.ontology.port.WritableLegalEntityRepository;
import com.gien.gits.ontology.port.WritableOperatingCaseRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.ontology.port.WritablePolicyRuleRepository;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;
import com.gien.gits.ontology.port.WritableProductCatalogRepository;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;
import com.gien.gits.ontology.port.WritableTransactionRecordRepository;
import com.gien.gits.ontology.port.WritableTransactionRepository;

/**
 * Registers all JDBC repository beans.
 * These are plain POJOs (not Spring Data) — manually wired here.
 */
@Configuration
public class RepositoryConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public WritableOperatingCaseRepository jdbcOperatingCaseRepository(JdbcTemplate jdbc) {
        return new JdbcOperatingCaseRepository(jdbc);
    }

    @Bean
    public WritableInteractionRepository jdbcInteractionRepository(JdbcTemplate jdbc) {
        return new JdbcInteractionRepository(jdbc);
    }

    @Bean
    public WritableClaimRepository jdbcClaimRepository(JdbcTemplate jdbc) {
        return new JdbcClaimRepository(jdbc);
    }

    @Bean
    public WritableCustomerJourneyRepository jdbcCustomerJourneyRepository(JdbcTemplate jdbc) {
        return new JdbcCustomerJourneyRepository(jdbc);
    }

    // --- 持续经营场景仓储 ---

    @Bean
    public WritableCustomerRepository jdbcCustomerRepository(JdbcTemplate jdbc) {
        return new JdbcCustomerRepository(jdbc);
    }

    @Bean
    public WritableLegalEntityRepository jdbcLegalEntityRepository(JdbcTemplate jdbc) {
        return new JdbcLegalEntityRepository(jdbc);
    }

    @Bean
    public WritableGroupRelationshipRepository jdbcGroupRelationshipRepository(JdbcTemplate jdbc) {
        return new JdbcGroupRelationshipRepository(jdbc);
    }

    @Bean
    public WritableBankRelationshipSnapshotRepository jdbcBankRelationshipSnapshotRepository(JdbcTemplate jdbc) {
        return new JdbcBankRelationshipSnapshotRepository(jdbc);
    }

    @Bean
    public WritableCreditFacilityRepository jdbcCreditFacilityRepository(JdbcTemplate jdbc) {
        return new JdbcCreditFacilityRepository(jdbc);
    }

    @Bean
    public WritableTransactionRecordRepository jdbcTransactionRecordRepository(JdbcTemplate jdbc) {
        return new JdbcTransactionRecordRepository(jdbc);
    }

    @Bean
    public WritableProductCatalogRepository jdbcProductCatalogRepository(JdbcTemplate jdbc) {
        return new JdbcProductCatalogRepository(jdbc);
    }

    @Bean
    public WritablePolicyRuleRepository jdbcPolicyRuleRepository(JdbcTemplate jdbc) {
        return new JdbcPolicyRuleRepository(jdbc);
    }

    @Bean
    public WritableExternalEventRepository jdbcExternalEventRepository(JdbcTemplate jdbc) {
        return new JdbcExternalEventRepository(jdbc);
    }

    @Bean
    public WritableKycGapProfileRepository jdbcKycGapProfileRepository(JdbcTemplate jdbc) {
        return new JdbcKycGapProfileRepository(jdbc);
    }

    @Bean
    public WritableFactReconciliationRepository jdbcFactReconciliationRepository(JdbcTemplate jdbc) {
        return new JdbcFactReconciliationRepository(jdbc);
    }

    @Bean
    public WritableOpportunitySignalRepository jdbcOpportunitySignalRepository(JdbcTemplate jdbc) {
        return new JdbcOpportunitySignalRepository(jdbc);
    }

    @Bean
    public WritableCommitmentRepository jdbcCommitmentRepository(JdbcTemplate jdbc) {
        return new JdbcCommitmentRepository(jdbc);
    }

    @Bean
    public WritableRelationshipReportRepository jdbcRelationshipReportRepository(JdbcTemplate jdbc) {
        return new JdbcRelationshipReportRepository(jdbc);
    }

    // --- P9 Loop G1: 访后分析 + 访前报告内容仓储 ---

    @Bean
    public WritablePostvisitAnalysisContentRepository jdbcPostvisitAnalysisContentRepository(
            JdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcPostvisitAnalysisContentRepository(jdbc, objectMapper);
    }

    @Bean
    public WritablePrevisitReportContentRepository jdbcPrevisitReportContentRepository(
            JdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcPrevisitReportContentRepository(jdbc, objectMapper);
    }

    // --- P9 Loop G7: 交易流水仓储 ---

    @Bean
    public WritableTransactionRepository jdbcTransactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }
}
