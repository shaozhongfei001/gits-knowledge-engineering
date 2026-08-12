package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.common.handler.SharedObjectMapper;
import com.gien.gits.engagement.MeetingScript;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
public class AgendaItemListTypeHandler extends BaseTypeHandler<List<MeetingScript.AgendaItem>> {

    private static final ObjectMapper MAPPER = SharedObjectMapper.get();
    private static final TypeReference<List<MeetingScript.AgendaItem>> TYPE_REF = new TypeReference<>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<MeetingScript.AgendaItem> parameter, JdbcType jdbcType)
            throws SQLException {
        try { ps.setString(i, MAPPER.writeValueAsString(parameter)); }
        catch (JsonProcessingException e) { throw new SQLException("Failed to serialize AgendaItem list", e); }
    }

    @Override public List<MeetingScript.AgendaItem> getNullableResult(ResultSet rs, String c) throws SQLException { return parse(rs.getString(c)); }
    @Override public List<MeetingScript.AgendaItem> getNullableResult(ResultSet rs, int i) throws SQLException { return parse(rs.getString(i)); }
    @Override public List<MeetingScript.AgendaItem> getNullableResult(CallableStatement cs, int i) throws SQLException { return parse(cs.getString(i)); }

    private List<MeetingScript.AgendaItem> parse(String json) throws SQLException {
        if (json == null || json.isBlank()) return null;
        try { return MAPPER.readValue(json, TYPE_REF); }
        catch (JsonProcessingException e) { throw new SQLException("Failed to deserialize AgendaItem list", e); }
    }
}
