package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.common.handler.SharedObjectMapper;
import com.gien.gits.engagement.PrevisitReportContent.ProductScheme;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * List&lt;ProductScheme&gt; 专用 JSON 类型处理器。
 * 避免通用 JsonTypeHandler 对 List 泛型产生 LinkedHashMap。
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ProductSchemeListTypeHandler extends BaseTypeHandler<List<ProductScheme>> {

    private static final ObjectMapper MAPPER = SharedObjectMapper.get();
    private static final TypeReference<List<ProductScheme>> TYPE = new TypeReference<>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<ProductScheme> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize List<ProductScheme> to JSON", e);
        }
    }

    @Override
    public List<ProductScheme> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<ProductScheme> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<ProductScheme> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<ProductScheme> parseJson(String json) throws SQLException {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to List<ProductScheme>", e);
        }
    }
}
