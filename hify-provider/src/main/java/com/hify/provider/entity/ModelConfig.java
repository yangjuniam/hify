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
@TableName(value = "model_config", autoResultMap = true)
public class ModelConfig extends BaseEntity {

    /**
     * 所属提供商 id
     */
    private Long providerId;

    /**
     * 模型展示名称，如 GPT-4o
     */
    private String name;

    /**
     * 调用时传给 API 的模型标识，Azure 为 deployment name
     */
    private String modelId;

    /**
     * 上下文窗口大小（token）
     */
    private Integer contextSize;

    /**
     * 模型级扩展参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraParams;

    /**
     * 是否启用
     */
    private Integer enabled;
}
