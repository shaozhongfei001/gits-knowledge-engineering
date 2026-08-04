package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.LegalEntity;
import com.gien.gits.ontology.GroupRelationship;
import com.gien.gits.ontology.BankRelationshipSnapshot;
import com.gien.gits.ontology.CreditFacility;
import com.gien.gits.ontology.TransactionRecord;
import com.gien.gits.ontology.ProductKnowledgeCard;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.RelationshipReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JSON辅助工具 — 处理CLOB字段与List<String>的转换
 */
final class JsonHelper {
    private JsonHelper() {}

    static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    static List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) return List.of();
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isEmpty()) return List.of();
            // Split on "," boundary between quoted strings
            String[] parts = trimmed.split("\",\\s*\"");
            return java.util.Arrays.stream(parts)
                .map(s -> s.replace("\"", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
        }
        return List.of();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
