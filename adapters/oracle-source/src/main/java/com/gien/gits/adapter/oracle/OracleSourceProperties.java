package com.gien.gits.adapter.oracle;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Oracle数据源配置属性。
 */
@ConfigurationProperties(prefix = "oracle.source")
public class OracleSourceProperties {

    private boolean enabled = false;
    private String jdbcUrl;
    private String username;
    private String password;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
