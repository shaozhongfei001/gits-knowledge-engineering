package com.gien.gits.adapter.oracle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.gien.gits.ontology.port.OracleSourcePort;

import javax.sql.DataSource;

/**
 * Oracle数据源自动配置 — 仅在oracle.source.enabled=true时激活。
 * 创建Oracle JDBC数据源和JdbcOracleSourceAdapter Bean。
 */
@Configuration
@ConditionalOnProperty(name = "oracle.source.enabled", havingValue = "true")
public class OracleSourceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OracleSourceAutoConfiguration.class);

    @Bean
    public DataSource oracleDataSource(OracleSourceProperties properties) {
        log.info("Configuring Oracle DataSource: url={}", properties.getJdbcUrl());
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(properties.getJdbcUrl());
        ds.setUsername(properties.getUsername());
        ds.setPassword(properties.getPassword());
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        return ds;
    }

    @Bean
    public OracleSourcePort oracleSourcePort(DataSource oracleDataSource) {
        return new JdbcOracleSourceAdapter(oracleDataSource, true);
    }
}
