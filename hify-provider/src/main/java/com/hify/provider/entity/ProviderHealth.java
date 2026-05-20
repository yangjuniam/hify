package com.hify.provider.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("provider_health")
public class ProviderHealth implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 provider.id
     */
    private Long providerId;

    /**
     * 状态：UP / DOWN / DEGRADED / UNKNOWN
     */
    private String status;

    /**
     * 最后探测时间
     */
    private LocalDateTime lastCheckAt;

    /**
     * 最后成功时间
     */
    private LocalDateTime lastSuccessAt;

    /**
     * 连续失败次数
     */
    private Integer failCount;

    /**
     * 最近一次延迟（ms）
     */
    private Integer latencyMs;

    /**
     * 最近失败原因
     */
    private String errorMessage;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
