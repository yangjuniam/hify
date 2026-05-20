package com.hify.provider.dto;

import com.hify.provider.entity.ModelConfig;
import com.hify.provider.entity.ProviderHealth;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 提供商详情 VO
 */
@Data
public class ProviderDetailVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 提供商展示名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * API 基础地址
     */
    private String baseUrl;

    /**
     * 鉴权配置（隐藏敏感信息）
     */
    private Map<String, Object> authConfig;

    /**
     * 备注
     */
    private String description;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 模型配置列表
     */
    private List<ModelConfig> modelConfigs;

    /**
     * 健康状态
     */
    private ProviderHealth health;
}
