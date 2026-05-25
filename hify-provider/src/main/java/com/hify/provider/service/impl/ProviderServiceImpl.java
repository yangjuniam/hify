package com.hify.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hify.common.exception.BizException;
import com.hify.common.exception.ErrorCode;
import com.hify.common.exception.LlmApiException;
import com.hify.common.http.LlmHttpClient;
import com.hify.provider.constant.ProviderType;
import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.dto.CreateProviderRequest;
import com.hify.provider.dto.ProviderDetailVO;
import com.hify.provider.dto.UpdateProviderRequest;
import com.hify.provider.entity.ModelConfig;
import com.hify.provider.entity.Provider;
import com.hify.provider.entity.ProviderHealth;
import com.hify.provider.mapper.ModelConfigMapper;
import com.hify.provider.mapper.ProviderHealthMapper;
import com.hify.provider.mapper.ProviderMapper;
import com.hify.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供商服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderMapper providerMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final ProviderHealthMapper providerHealthMapper;
    private final LlmHttpClient llmHttpClient;
    private final ObjectMapper objectMapper;

    private static final int TEST_TIMEOUT_MS = 10000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Provider createProvider(CreateProviderRequest request) {
        Provider provider = new Provider();
        BeanUtils.copyProperties(request, provider);
        providerMapper.insert(provider);

        ProviderHealth health = new ProviderHealth();
        health.setProviderId(provider.getId());
        health.setStatus("UNKNOWN");
        health.setFailCount(0);
        providerHealthMapper.insert(health);

        return provider;
    }

    @Override
    public Page<Provider> listProviders(int page, int pageSize) {
        Page<Provider> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Provider> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Provider::getCreatedAt);
        return providerMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public ProviderDetailVO getProviderDetail(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提供商不存在");
        }
        return buildProviderDetail(provider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Provider updateProvider(UpdateProviderRequest request) {
        Provider provider = providerMapper.selectById(request.getId());
        if (provider == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提供商不存在");
        }

        provider.setName(request.getName());
        provider.setType(request.getType());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setAuthConfig(request.getAuthConfig());
        provider.setDescription(request.getDescription());
        provider.setEnabled(request.getEnabled());
        providerMapper.updateById(provider);
        return provider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提供商不存在");
        }

        LambdaQueryWrapper<ModelConfig> modelWrapper = new LambdaQueryWrapper<>();
        modelWrapper.eq(ModelConfig::getProviderId, id);
        modelConfigMapper.delete(modelWrapper);

        LambdaQueryWrapper<ProviderHealth> healthWrapper = new LambdaQueryWrapper<>();
        healthWrapper.eq(ProviderHealth::getProviderId, id);
        providerHealthMapper.delete(healthWrapper);

        providerMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConnectionTestResult testConnectionById(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提供商不存在");
        }

        ConnectionTestResult result = testConnection(provider);

        LambdaQueryWrapper<ProviderHealth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderHealth::getProviderId, id);
        ProviderHealth health = providerHealthMapper.selectOne(wrapper);

        if (health == null) {
            health = new ProviderHealth();
            health.setProviderId(id);
            health.setStatus(result.getSuccess() ? "UP" : "DOWN");
            health.setLastCheckAt(LocalDateTime.now());
            health.setLastSuccessAt(result.getSuccess() ? LocalDateTime.now() : null);
            health.setFailCount(result.getSuccess() ? 0 : 1);
            health.setLatencyMs(result.getLatencyMs());
            health.setErrorMessage(result.getErrorMessage());
            providerHealthMapper.insert(health);
        } else {
            health.setStatus(result.getSuccess() ? "UP" : "DOWN");
            health.setLastCheckAt(LocalDateTime.now());
            if (result.getSuccess()) {
                health.setLastSuccessAt(LocalDateTime.now());
                health.setFailCount(0);
            } else {
                health.setFailCount(health.getFailCount() + 1);
            }
            health.setLatencyMs(result.getLatencyMs());
            health.setErrorMessage(result.getErrorMessage());
            providerHealthMapper.updateById(health);
        }

        return result;
    }

    @Override
    public ConnectionTestResult testConnection(Provider provider) {
        String type = provider.getType();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            ConnectionTestResult result = switch (type) {
                case ProviderType.OPENAI, ProviderType.OPENAI_COMPATIBLE, ProviderType.AZURE_OPENAI ->
                    testOpenAiCompatible(provider);
                case ProviderType.ANTHROPIC -> testAnthropic(provider);
                case ProviderType.OLLAMA -> testOllama(provider);
                default -> ConnectionTestResult.builder()
                    .success(false)
                    .errorMessage("不支持的提供商类型: " + type)
                    .build();
            };

            stopWatch.stop();
            if (result.getLatencyMs() == null) {
                result.setLatencyMs((int) stopWatch.getTotalTimeMillis());
            }
            return result;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("测试连接失败: provider={}, error={}", provider.getName(), e.getMessage());
            return ConnectionTestResult.builder()
                .success(false)
                .latencyMs((int) stopWatch.getTotalTimeMillis())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    private ConnectionTestResult testOpenAiCompatible(Provider provider) {
        String baseUrl = provider.getBaseUrl();
        String apiKey = getApiKey(provider);

        String url = baseUrl.endsWith("/") ? baseUrl + "v1/models" : baseUrl + "/v1/models";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        String response = llmHttpClient.get(url, headers, TEST_TIMEOUT_MS);
        int modelCount = parseOpenAiModelCount(response);

        return ConnectionTestResult.builder()
            .success(true)
            .modelCount(modelCount)
            .build();
    }

    private ConnectionTestResult testAnthropic(Provider provider) {
        String baseUrl = provider.getBaseUrl();
        String apiKey = getApiKey(provider);

        String url = baseUrl.endsWith("/") ? baseUrl + "v1/models" : baseUrl + "/v1/models";

        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", apiKey);
        headers.put("anthropic-version", "2023-06-01");

        String response = llmHttpClient.get(url, headers, TEST_TIMEOUT_MS);
        int modelCount = parseAnthropicModelCount(response);

        return ConnectionTestResult.builder()
            .success(true)
            .modelCount(modelCount)
            .build();
    }

    private ConnectionTestResult testOllama(Provider provider) {
        String baseUrl = provider.getBaseUrl();

        String url = baseUrl.endsWith("/") ? baseUrl + "api/tags" : baseUrl + "/api/tags";

        String response = llmHttpClient.get(url, null, TEST_TIMEOUT_MS);
        int modelCount = parseOllamaModelCount(response);

        return ConnectionTestResult.builder()
            .success(true)
            .modelCount(modelCount)
            .build();
    }

    @Override
    public boolean isModelConfigAvailable(Long modelConfigId) {
        ModelConfig modelConfig = modelConfigMapper.selectById(modelConfigId);
        if (modelConfig == null || !Integer.valueOf(1).equals(modelConfig.getEnabled())) {
            return false;
        }

        Provider provider = providerMapper.selectById(modelConfig.getProviderId());
        return provider != null && Integer.valueOf(1).equals(provider.getEnabled());
    }

    private ProviderDetailVO buildProviderDetail(Provider provider) {
        ProviderDetailVO vo = new ProviderDetailVO();
        BeanUtils.copyProperties(provider, vo);
        vo.setAuthConfig(maskAuthConfig(provider.getAuthConfig()));

        LambdaQueryWrapper<ModelConfig> modelWrapper = new LambdaQueryWrapper<>();
        modelWrapper.eq(ModelConfig::getProviderId, provider.getId());
        vo.setModelConfigs(modelConfigMapper.selectList(modelWrapper));

        LambdaQueryWrapper<ProviderHealth> healthWrapper = new LambdaQueryWrapper<>();
        healthWrapper.eq(ProviderHealth::getProviderId, provider.getId());
        ProviderHealth health = providerHealthMapper.selectOne(healthWrapper);
        if (health == null) {
            health = new ProviderHealth();
            health.setProviderId(provider.getId());
            health.setStatus("UNKNOWN");
            health.setFailCount(0);
        }
        vo.setHealth(health);

        return vo;
    }

    private String getApiKey(Provider provider) {
        Map<String, Object> authConfig = provider.getAuthConfig();
        if (authConfig == null || authConfig.get("apiKey") == null) {
            throw new LlmApiException(LlmApiException.ErrorType.AUTH_FAILED, "未配置 API Key");
        }
        return (String) authConfig.get("apiKey");
    }

    private Map<String, Object> maskAuthConfig(Map<String, Object> authConfig) {
        if (authConfig == null) {
            return null;
        }
        Map<String, Object> masked = new HashMap<>(authConfig);
        if (masked.containsKey("apiKey")) {
            String apiKey = (String) masked.get("apiKey");
            if (apiKey.length() > 8) {
                masked.put("apiKey", apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4));
            } else {
                masked.put("apiKey", "****");
            }
        }
        return masked;
    }

    private int parseOpenAiModelCount(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");
            return data != null && data.isArray() ? data.size() : 0;
        } catch (Exception e) {
            log.warn("解析 OpenAI 模型列表失败: {}", e.getMessage());
            return 0;
        }
    }

    private int parseAnthropicModelCount(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");
            return data != null && data.isArray() ? data.size() : 0;
        } catch (Exception e) {
            log.warn("解析 Anthropic 模型列表失败: {}", e.getMessage());
            return 0;
        }
    }

    private int parseOllamaModelCount(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode models = root.get("models");
            return models != null && models.isArray() ? models.size() : 0;
        } catch (Exception e) {
            log.warn("解析 Ollama 模型列表失败: {}", e.getMessage());
            return 0;
        }
    }
}
