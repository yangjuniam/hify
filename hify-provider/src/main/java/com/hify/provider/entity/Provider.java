package com.hify.provider.entity;

import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hify.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "provider", autoResultMap = true)
public class Provider extends BaseEntity {

    /**
     * 提供商展示名称
     */
    private String name;

    /**
     * 类型：OPENAI / ANTHROPIC / OLLAMA / AZURE_OPENAI / OPENAI_COMPATIBLE
     */
    private String type;

    /**
     * API 基础地址
     */
    private String baseUrl;

    /**
     * 鉴权配置，结构随 type 变化
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> authConfig;

    /**
     * 备注
     */
    private String description;

    /**
     * 是否启用：1 启用 0 禁用
     */
    private Integer enabled;
}
