package com.hify.provider.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.dto.CreateProviderRequest;
import com.hify.provider.dto.ProviderDetailVO;
import com.hify.provider.dto.UpdateProviderRequest;
import com.hify.provider.entity.Provider;

/**
 * 提供商服务接口
 */
public interface ProviderService {

    /**
     * 创建提供商
     *
     * @param request 创建请求
     * @return 提供商信息
     */
    Provider createProvider(CreateProviderRequest request);

    /**
     * 分页查询提供商列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Provider> listProviders(int page, int pageSize);

    /**
     * 获取提供商详情（包含模型配置和健康状态）
     *
     * @param id 提供商ID
     * @return 提供商详情
     */
    ProviderDetailVO getProviderDetail(Long id);

    /**
     * 更新提供商
     *
     * @param request 更新请求
     * @return 提供商信息
     */
    Provider updateProvider(UpdateProviderRequest request);

    /**
     * 删除提供商
     *
     * @param id 提供商ID
     */
    void deleteProvider(Long id);

    /**
     * 测试提供商连通性（根据ID）
     *
     * @param id 提供商ID
     * @return 测试结果
     */
    ConnectionTestResult testConnectionById(Long id);

    /**
     * 测试提供商连通性（传入配置）
     *
     * @param provider 提供商配置
     * @return 测试结果
     */
    ConnectionTestResult testConnection(Provider provider);
}
