package com.gien.gits.adapter.persistence.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * List&lt;String&gt; ↔ JSON 数组字符串 类型处理器。
 * <p>数据库列存储 JSON 数组格式如 <code>["tag1","tag2"]</code>。</p>
 *
 * <p>此类放在 handler 包而非 typehandler 包，避免被 type-handlers-package 自动扫描。
 * 因为自动扫描时会与 UuidListJsonTypeHandler 冲突（两者都映射 List 类型）。
 * 在 XML 中通过 typeHandler 属性显式引用。</p>
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringListJsonTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, toJsonArray(parameter));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseStringList(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseStringList(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseStringList(cs.getString(columnIndex));
    }

    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) return List.of();
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isEmpty()) return List.of();
            String[] parts = trimmed.split("\",\\s*\"");
            return Arrays.stream(parts)
                .map(s -> s.replace("\"", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
        }
        return List.of();
    }
}
