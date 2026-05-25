package com.hify.agent.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAgentRequest {

    @NotBlank(message = "Agent 名称不能为空")
    @Size(max = 100, message = "Agent 名称长度不能超过100")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    private String systemPrompt;

    @NotNull(message = "模型配置不能为空")
    private Long modelConfigId;

    @DecimalMin(value = "0.00", message = "温度参数不能小于0")
    @DecimalMax(value = "1.00", message = "温度参数不能大于1")
    private BigDecimal temperature = new BigDecimal("0.70");

    @Min(value = 1, message = "最大输出 token 数不能小于1")
    @Max(value = 200000, message = "最大输出 token 数不能超过200000")
    private Integer maxTokens = 2048;

    @Min(value = 1, message = "上下文轮数不能小于1")
    @Max(value = 100, message = "上下文轮数不能超过100")
    private Integer maxContextTurns = 10;

    private Integer enabled = 1;
}
