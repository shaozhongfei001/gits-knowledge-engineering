package com.gien.gits.adapter.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OracleSourcePropertiesTest {

    @Test
    void defaultProperties() {
        OracleSourceProperties props = new OracleSourceProperties();
        assertFalse(props.isEnabled());
        assertEquals(null, props.getJdbcUrl());
        assertEquals(null, props.getUsername());
        assertEquals(null, props.getPassword());
    }

    @Test
    void customProperties() {
        OracleSourceProperties props = new OracleSourceProperties();
        props.setEnabled(true);
        props.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:xe");
        props.setUsername("system");
        props.setPassword("secret");

        assertTrue(props.isEnabled());
        assertEquals("jdbc:oracle:thin:@localhost:1521:xe", props.getJdbcUrl());
        assertEquals("system", props.getUsername());
        assertEquals("secret", props.getPassword());
    }

    @Test
    void nullJdbcUrlIsAllowed() {
        OracleSourceProperties props = new OracleSourceProperties();
        props.setJdbcUrl(null);
        assertEquals(null, props.getJdbcUrl());
    }
}
