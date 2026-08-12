package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.common.handler.SharedObjectMapper;
import com.gien.gits.engagement.OutreachScript;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
public class TalkingPointListTypeHandler extends BaseTypeHandler<List<OutreachScript.TalkingPoint>> {

    private static final ObjectMapper MAPPER = SharedObjectMapper.get();
    private static final TypeReference<List<OutreachScript.TalkingPoint>> TYPE_REF = new TypeReference<>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<OutreachScript.TalkingPoint> parameter, JdbcType jdbcType)
            throws SQLException {
        try { ps.setString(i, MAPPER.writeValueAsString(parameter)); }
        catch (JsonProcessingException e) { throw new SQLException("Failed to serialize TalkingPoint list", e); }
    }

    @Override public List<OutreachScript.TalkingPoint> getNullableResult(ResultSet rs, String c) throws SQLException { return parse(rs.getString(c)); }
    @Override public List<OutreachScript.TalkingPoint> getNullableResult(ResultSet rs, int i) throws SQLException { return parse(rs.getString(i)); }
    @Override public List<OutreachScript.TalkingPoint> getNullableResult(CallableStatement cs, int i) throws SQLException { return parse(cs.getString(i)); }

    private List<OutreachScript.TalkingPoint> parse(String json) throws SQLException {
        if (json == null || json.isBlank()) return null;
        try { return MAPPER.readValue(json, TYPE_REF); }
        catch (JsonProcessingException e) { throw new SQLException("Failed to deserialize TalkingPoint list", e); }
    }
}
