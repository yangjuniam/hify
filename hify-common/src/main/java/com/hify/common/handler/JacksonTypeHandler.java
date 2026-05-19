package com.hify.common.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.lang.reflect.Field;

@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JacksonTypeHandler extends AbstractJsonTypeHandler<Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<?> type;

    public JacksonTypeHandler(Class<?> type) {
        super(type);
        this.type = type;
    }

    public JacksonTypeHandler(Class<?> type, Field field) {
        super(type, field);
        this.type = type;
    }

    @SneakyThrows
    @Override
    public Object parse(String json) {
        return objectMapper.readValue(json, type);
    }

    @SneakyThrows
    @Override
    public String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
