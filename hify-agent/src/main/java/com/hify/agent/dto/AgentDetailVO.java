package com.hify.agent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AgentDetailVO {

    private Long id;

    private String name;

    private String description;

    private String systemPrompt;

    private Long modelConfigId;

    private BigDecimal temperature;

    private Integer maxTokens;

    private Integer maxContextTurns;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
