package com.hify.provider.controller;

import com.hify.common.dto.Result;
import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.entity.Provider;
import com.hify.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供商控制器
 */
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    /**
     * 测试提供商连通性
     *
     * @param provider 提供商配置
     * @return 测试结果
     */
    @PostMapping("/test-connection")
    public Result<ConnectionTestResult> testConnection(@RequestBody Provider provider) {
        ConnectionTestResult result = providerService.testConnection(provider);
        return Result.success(result);
    }
}
