package com.hify.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.common.dto.PageResult;
import com.hify.common.dto.Result;
import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.dto.CreateProviderRequest;
import com.hify.provider.dto.ProviderDetailVO;
import com.hify.provider.dto.UpdateProviderRequest;
import com.hify.provider.entity.Provider;
import com.hify.provider.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 提供商控制器
 */
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    /**
     * 创建提供商
     *
     * @param request 创建请求
     * @return 提供商信息
     */
    @PostMapping
    public Result<Provider> createProvider(@Valid @RequestBody CreateProviderRequest request) {
        Provider provider = providerService.createProvider(request);
        return Result.success(provider);
    }

    /**
     * 分页查询提供商列表
     *
     * @param page     页码，从1开始
     * @param pageSize 每页大小，默认20
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Provider>> listProviders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Provider> pageResult = providerService.listProviders(page, pageSize);
        return Result.success(PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        ));
    }

    /**
     * 获取提供商详情（包含模型配置和健康状态）
     *
     * @param id 提供商ID
     * @return 提供商详情
     */
    @GetMapping("/{id}")
    public Result<ProviderDetailVO> getProviderDetail(@PathVariable Long id) {
        ProviderDetailVO detail = providerService.getProviderDetail(id);
        return Result.success(detail);
    }

    /**
     * 更新提供商
     *
     * @param request 更新请求
     * @return 提供商信息
     */
    @PutMapping
    public Result<Provider> updateProvider(@Valid @RequestBody UpdateProviderRequest request) {
        Provider provider = providerService.updateProvider(request);
        return Result.success(provider);
    }

    /**
     * 删除提供商
     *
     * @param id 提供商ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return Result.success();
    }

    /**
     * 测试提供商连通性（根据ID）
     *
     * @param id 提供商ID
     * @return 测试结果
     */
    @PostMapping("/{id}/test-connection")
    public Result<ConnectionTestResult> testConnectionById(@PathVariable Long id) {
        ConnectionTestResult result = providerService.testConnectionById(id);
        return Result.success(result);
    }

    /**
     * 测试提供商连通性（传入配置）
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
