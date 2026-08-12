package com.gien.gits.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.JdbcAuditTraceRepository;
import com.gien.gits.adapter.persistence.JdbcCrmWritebackCommandRepository;
import com.gien.gits.adapter.persistence.JdbcEvidenceVersionLinkRepository;
import com.gien.gits.adapter.persistence.JdbcHumanGateRepository;
import com.gien.gits.ontology.port.AuditTraceRepository;
import com.gien.gits.ontology.port.EvidenceVersionLinkRepository;
import com.gien.gits.ontology.port.HumanGateRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * V1.1 extension repository beans.
 *
 * <p>Registers JDBC-based repositories for V1.1 controllers.
 * Each bean uses {@code @ConditionalOnMissingBean} so that MyBatis
 * implementations (from {@code MyBatisRepositoryConfig}) take precedence
 * when {@code gits.persistence.mode=mybatis}.</p>
 */
@Configuration
public class V11RepositoryConfig {

    @Bean
    @ConditionalOnMissingBean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public HumanGateRepository jdbcHumanGateRepository(
            NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcHumanGateRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditTraceRepository jdbcAuditTraceRepository(
            NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcAuditTraceRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public com.gien.gits.adapter.persistence.JdbcCrmWritebackCommandRepository jdbcCrmWritebackCommandRepository(
            NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        return new JdbcCrmWritebackCommandRepository(jdbc, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EvidenceVersionLinkRepository jdbcEvidenceVersionLinkRepository(
            NamedParameterJdbcTemplate jdbc) {
        return new JdbcEvidenceVersionLinkRepository(jdbc);
    }
}
