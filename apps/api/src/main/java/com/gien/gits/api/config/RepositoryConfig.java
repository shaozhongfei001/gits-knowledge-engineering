package com.gien.gits.api.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gien.gits.adapter.persistence.JdbcClaimRepository;
import com.gien.gits.adapter.persistence.JdbcInteractionRepository;
import com.gien.gits.adapter.persistence.JdbcOperatingCaseRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcCustomerJourneyRepository;

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
    public JdbcOperatingCaseRepository jdbcOperatingCaseRepository(JdbcTemplate jdbc) {
        return new JdbcOperatingCaseRepository(jdbc);
    }

    @Bean
    public JdbcInteractionRepository jdbcInteractionRepository(JdbcTemplate jdbc) {
        return new JdbcInteractionRepository(jdbc);
    }

    @Bean
    public JdbcClaimRepository jdbcClaimRepository(JdbcTemplate jdbc) {
        return new JdbcClaimRepository(jdbc);
    }

    @Bean
    public JdbcCustomerJourneyRepository jdbcCustomerJourneyRepository(JdbcTemplate jdbc) {
        return new JdbcCustomerJourneyRepository(jdbc);
    }
}
