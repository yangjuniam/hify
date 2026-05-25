package com.hify.agent.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hify.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent")
public class Agent extends BaseEntity {

    private String name;

    private String description;

    private String systemPrompt;

    private Long modelConfigId;

    private BigDecimal temperature;

    private Integer maxTokens;

    private Integer maxContextTurns;

    private Integer enabled;
}
