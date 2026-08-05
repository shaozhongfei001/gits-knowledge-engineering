package com.gien.gits.adapter.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.Industry;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class OracleCustomerMappingTest {

    private Customer createDefaultCustomer() {
        return new Customer(
                "CUST-001", "Acme Corp", "Acme", "USCC-123",
                LocalDate.of(2000, 1, 1), 1000000L,
                "MANUFACTURING", "北京市", "LARGE", "KEY",
                LocalDate.of(2020, 1, 1), "RM-001", "张经理", "朝阳支行",
                false, "LISTED", "LOW",
                List.of("贸易融资"), List.of("FX"), "长期合作客户");
    }

    @Test
    void customerConstruction() {
        Customer customer = createDefaultCustomer();

        assertEquals("CUST-001", customer.customerId());
        assertEquals("Acme Corp", customer.customerName());
        assertEquals(Industry.MANUFACTURING, customer.industry());
        assertNotNull(customer.createdAt());
    }

    @Test
    void customerRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(
                "  ", "Name", "Short", "USCC",
                LocalDate.now(), 0L, "FINANCE", "北京", "LARGE", "KEY",
                LocalDate.now(), "RM-1", "Name", "Branch",
                false, "LISTED", "LOW", null, null, null));
    }

    @Test
    void customerRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(
                "C-1", "  ", "Short", "USCC",
                LocalDate.now(), 0L, "FINANCE", "北京", "LARGE", "KEY",
                LocalDate.now(), "RM-1", "Name", "Branch",
                false, "LISTED", "LOW", null, null, null));
    }

    @Test
    void industryEnumValues() {
        assertEquals(10, Industry.values().length);
    }

    @Test
    void customerNullListsDefaultToEmpty() {
        Customer customer = new Customer(
                "C-1", "Name", "Short", "USCC",
                LocalDate.now(), 0L, "FINANCE", "北京", "LARGE", "KEY",
                LocalDate.now(), "RM-1", "Name", "Branch",
                false, "LISTED", "LOW", null, null, null);

        assertNotNull(customer.mainProducts());
        assertNotNull(customer.coreTags());
        assertTrue(customer.mainProducts().isEmpty());
        assertTrue(customer.coreTags().isEmpty());
    }
}
