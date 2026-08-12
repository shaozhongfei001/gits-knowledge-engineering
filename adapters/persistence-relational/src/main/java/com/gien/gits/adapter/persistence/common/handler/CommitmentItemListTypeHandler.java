package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.common.handler.SharedObjectMapper;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JSON type handler for List<PostvisitAnalysisContent.CommitmentItem>.
 * Uses TypeReference to preserve generic type information for Jackson deserialization.
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CommitmentItemListTypeHandler extends BaseTypeHandler<List<PostvisitAnalysisContent.CommitmentItem>> {

    private static final ObjectMapper MAPPER = SharedObjectMapper.get();
    private static final TypeReference<List<PostvisitAnalysisContent.CommitmentItem>> TYPE_REF = new TypeReference<>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<PostvisitAnalysisContent.CommitmentItem> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize CommitmentItem list to JSON", e);
        }
    }

    @Override
    public List<PostvisitAnalysisContent.CommitmentItem> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<PostvisitAnalysisContent.CommitmentItem> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<PostvisitAnalysisContent.CommitmentItem> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<PostvisitAnalysisContent.CommitmentItem> parseJson(String json) throws SQLException {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to CommitmentItem list", e);
        }
    }
}
