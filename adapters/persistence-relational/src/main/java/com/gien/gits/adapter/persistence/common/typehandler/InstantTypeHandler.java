package com.gien.gits.adapter.persistence.common.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Instant ↔ SQL Timestamp 类型处理器。
 */
@MappedTypes(Instant.class)
@MappedJdbcTypes(JdbcType.TIMESTAMP)
public class InstantTypeHandler extends BaseTypeHandler<Instant> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTimestamp(i, Timestamp.from(parameter));
    }

    @Override
    public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts == null ? null : ts.toInstant();
    }

    @Override
    public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnIndex);
        return ts == null ? null : ts.toInstant();
    }

    @Override
    public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp ts = cs.getTimestamp(columnIndex);
        return ts == null ? null : ts.toInstant();
    }
}
