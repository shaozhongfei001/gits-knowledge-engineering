package com.gien.gits.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * G4.5 数据库配置测试 — 验证 H2 数据源、Flyway 迁移和索引
 */
@SpringBootTest
class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ═══════════════════════════════════════════════════════════════
    // 1. H2 Datasource Configuration
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("H2 数据源在默认 profile 下正确配置")
    void h2DatasourceConfiguredCorrectly() {
        assertNotNull(dataSource, "DataSource must be injected");
        assertInstanceOf(HikariDataSource.class, dataSource,
            "DataSource must be HikariDataSource");

        HikariDataSource hikari = (HikariDataSource) dataSource;
        assertTrue(hikari.getJdbcUrl().contains("h2"),
            "JDBC URL must contain 'h2' for default profile");
        assertTrue(hikari.getJdbcUrl().contains("MODE=MySQL"),
            "H2 must run in MySQL compatibility mode");
    }

    @Test
    @DisplayName("HikariCP 连接池配置正确")
    void hikariPoolConfigured() {
        assertInstanceOf(HikariDataSource.class, dataSource);
        HikariDataSource hikari = (HikariDataSource) dataSource;

        assertTrue(hikari.getMaximumPoolSize() > 0,
            "Maximum pool size must be positive");
        assertTrue(hikari.getMinimumIdle() > 0,
            "Minimum idle must be positive");
        assertTrue(hikari.getMaximumPoolSize() >= hikari.getMinimumIdle(),
            "Maximum pool size must be >= minimum idle");
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. Flyway Migrations Run Successfully
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Flyway 迁移成功执行")
    void flywayMigrationsRunSuccessfully() {
        // In H2 MySQL mode, flyway_schema_history may be created with lowercase
        // Check both cases
        Integer upperCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'FLYWAY_SCHEMA_HISTORY'",
            Integer.class);

        assertNotNull(upperCount, "Flyway schema history check must return result");
        assertTrue(upperCount > 0,
            "flyway_schema_history table must exist (Flyway ran successfully)");
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. All Expected Tables Exist After Migration
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("所有预期表在迁移后存在")
    void allExpectedTablesExist() {
        Set<String> tables = getTableNames();

        // V001: operational_ontology_core
        assertTrue(tables.contains("OPERATING_CASE"), "operating_case table must exist");
        assertTrue(tables.contains("INTERACTION"), "interaction table must exist");
        assertTrue(tables.contains("CLAIM"), "claim table must exist");
        assertTrue(tables.contains("EVIDENCE"), "evidence table must exist");
        assertTrue(tables.contains("HUMAN_CONFIRMATION"), "human_confirmation table must exist");
        assertTrue(tables.contains("CONTROLLED_ACTION"), "controlled_action table must exist");
        assertTrue(tables.contains("ACTION_RECEIPT"), "action_receipt table must exist");

        // V002: interaction enriched + customer journey scenario
        assertTrue(tables.contains("CUSTOMER_JOURNEY"), "customer_journey table must exist");
        assertTrue(tables.contains("INSIGHT_CLAIM"), "insight_claim table must exist");
        assertTrue(tables.contains("PRODUCT_CANDIDATE_CLAIM"), "product_candidate_claim table must exist");
        assertTrue(tables.contains("PREVISIT_REPORT"), "previsit_report table must exist");
        assertTrue(tables.contains("POSTVISIT_ANALYSIS"), "postvisit_analysis table must exist");
        assertTrue(tables.contains("INTERACTION_PARTICIPANT"), "interaction_participant table must exist");

        // V003: customer context
        assertTrue(tables.contains("CUSTOMER"), "customer table must exist");
        assertTrue(tables.contains("LEGAL_ENTITY"), "legal_entity table must exist");
        assertTrue(tables.contains("GROUP_RELATIONSHIP"), "group_relationship table must exist");
        assertTrue(tables.contains("BANK_RELATIONSHIP_SNAPSHOT"), "bank_relationship_snapshot table must exist");
        assertTrue(tables.contains("CREDIT_FACILITY"), "credit_facility table must exist");
        assertTrue(tables.contains("TRANSACTION_LEDGER"), "transaction_ledger table must exist");

        // V004: knowledge rules
        assertTrue(tables.contains("PRODUCT_CATALOG"), "product_catalog table must exist");
        assertTrue(tables.contains("POLICY_RULE"), "policy_rule table must exist");
        assertTrue(tables.contains("EXTERNAL_EVENT"), "external_event table must exist");

        // V005: operating enrichment
        assertTrue(tables.contains("FACT_RECONCILIATION_CASE"), "fact_reconciliation_case table must exist");
        assertTrue(tables.contains("OPPORTUNITY_SIGNAL"), "opportunity_signal table must exist");
        assertTrue(tables.contains("COMMITMENT"), "commitment table must exist");
        assertTrue(tables.contains("RELATIONSHIP_REPORT"), "relationship_report table must exist");

        // V007: postvisit analysis and previsit report content
        assertTrue(tables.contains("POSTVISIT_ANALYSIS_CONTENT"), "postvisit_analysis_content table must exist");
        assertTrue(tables.contains("PREVISIT_REPORT_CONTENT"), "previsit_report_content table must exist");

        // V008: transaction
        assertTrue(tables.contains("TRANSACTION"), "transaction table must exist");

        // V009: outreach and meeting script
        assertTrue(tables.contains("OUTREACH_SCRIPT"), "outreach_script table must exist");
        assertTrue(tables.contains("MEETING_SCRIPT"), "meeting_script table must exist");
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. Indexes Exist on Key Columns
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("关键列上存在索引")
    void indexesExistOnKeyColumns() {
        Set<String> indexNames = getIndexNames();

        // V010 production indexes
        assertTrue(indexNames.contains("IDX_CUSTOMER_JOURNEY_CUSTOMER_ID"),
            "customer_journey.customer_id index must exist");
        assertTrue(indexNames.contains("IDX_CUSTOMER_JOURNEY_PHASE"),
            "customer_journey.phase index must exist");
        assertTrue(indexNames.contains("IDX_INTERACTION_CASE_ID"),
            "interaction.case_id index must exist");
        assertTrue(indexNames.contains("IDX_INTERACTION_OCCURRED_AT"),
            "interaction.occurred_at index must exist");
        assertTrue(indexNames.contains("IDX_CLAIM_CASE_ID"),
            "claim.case_id index must exist");
        assertTrue(indexNames.contains("IDX_CLAIM_CLAIM_STATUS"),
            "claim.claim_status index must exist");
        assertTrue(indexNames.contains("IDX_OPERATING_CASE_CASE_TYPE"),
            "operating_case.case_type index must exist");
        assertTrue(indexNames.contains("IDX_OPERATING_CASE_STATUS"),
            "operating_case.status index must exist");
        assertTrue(indexNames.contains("IDX_INSIGHT_CLAIM_OPERATING_CASE_ID"),
            "insight_claim.operating_case_id index must exist");
        assertTrue(indexNames.contains("IDX_PRODUCT_CANDIDATE_CLAIM_OPERATING_CASE_ID"),
            "product_candidate_claim.operating_case_id index must exist");
        assertTrue(indexNames.contains("IDX_PREVISIT_REPORT_OPERATING_CASE_ID"),
            "previsit_report.operating_case_id index must exist");
        assertTrue(indexNames.contains("IDX_POSTVISIT_ANALYSIS_OPERATING_CASE_ID"),
            "postvisit_analysis.operating_case_id index must exist");
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════════════════════════════

    private Set<String> getTableNames() {
        return jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class).stream().collect(Collectors.toSet());
    }

    private Set<String> getIndexNames() {
        return jdbcTemplate.queryForList(
            "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA = 'PUBLIC'",
            String.class).stream().collect(Collectors.toSet());
    }
}
