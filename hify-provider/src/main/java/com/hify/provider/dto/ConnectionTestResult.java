package com.hify.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连通性测试结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 耗时（毫秒）
     */
    private Integer latencyMs;

    /**
     * 模型数量
     */
    private Integer modelCount;

    /**
     * 错误信息
     */
    private String errorMessage;
}
