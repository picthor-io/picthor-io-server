package io.picthor.data.mapper.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.picthor.data.entity.DirectoryStats;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.*;

public class JsonbDirectoryStatsTypeHandler extends BaseTypeHandler<DirectoryStats> {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
    }

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, DirectoryStats parameter, JdbcType jdbcType
    ) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.OTHER);
        } else {
            try {
                PGobject jsonObject = new PGobject();
                jsonObject.setType("jsonb");
                jsonObject.setValue(MAPPER.writeValueAsString(parameter));
                ps.setObject(i, jsonObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public DirectoryStats getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        return parseStats(string);
    }

    @Override
    public DirectoryStats getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        return parseStats(string);
    }

    @Override
    public DirectoryStats getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        return parseStats(string);
    }

    private DirectoryStats parseStats(String string) {
        if (string == null) {
            return null;
        }
        try {
            return MAPPER.readValue(string, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
