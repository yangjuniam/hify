package com.hify.provider.service;

import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.entity.Provider;

/**
 * 提供商服务接口
 */
public interface ProviderService {

    /**
     * 测试提供商连通性
     *
     * @param provider 提供商配置
     * @return 测试结果
     */
    ConnectionTestResult testConnection(Provider provider);
}
