package com.hify.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 更新提供商请求
 */
@Data
public class UpdateProviderRequest {

    /**
     * ID
     */
    @NotNull(message = "提供商ID不能为空")
    private Long id;

    /**
     * 提供商展示名称
     */
    @NotBlank(message = "提供商名称不能为空")
    @Size(max = 50, message = "提供商名称长度不能超过50")
    private String name;

    /**
     * 类型：OPENAI / ANTHROPIC / OLLAMA / AZURE_OPENAI / OPENAI_COMPATIBLE
     */
    @NotBlank(message = "提供商类型不能为空")
    private String type;

    /**
     * API 基础地址
     */
    @NotBlank(message = "API 基础地址不能为空")
    private String baseUrl;

    /**
     * 鉴权配置
     */
    private Map<String, Object> authConfig;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200")
    private String description;

    /**
     * 是否启用：1 启用 0 禁用
     */
    private Integer enabled;
}
