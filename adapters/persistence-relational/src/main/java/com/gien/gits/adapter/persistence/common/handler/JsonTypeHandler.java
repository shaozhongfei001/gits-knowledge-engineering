package com.gien.gits.adapter.persistence.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.common.handler.SharedObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用 JSON 类型处理器 — 将任意 Java 对象序列化为 JSON 字符串存储。
 *
 * <p>使用方式：在 Mapper XML 的 resultMap 中指定 typeHandler + javaType，
 * MyBatis 会通过 javaType 推断泛型参数并调用带 Class 参数的构造函数。</p>
 *
 * <p>此处理器不在 type-handlers-package 扫描范围内，
 * 避免自动注册时因缺少无参构造函数而失败。</p>
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ObjectMapper MAPPER = SharedObjectMapper.get();
    private final Class<T> type;

    public JsonTypeHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize " + type.getSimpleName() + " to JSON", e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private T parseJson(String json) throws SQLException {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to " + type.getSimpleName(), e);
        }
    }
}
