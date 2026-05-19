package com.hify.provider.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hify.common.exception.LlmApiException;
import com.hify.common.http.LlmHttpClient;
import com.hify.provider.constant.ProviderType;
import com.hify.provider.dto.ConnectionTestResult;
import com.hify.provider.entity.Provider;
import com.hify.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供商服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final LlmHttpClient llmHttpClient;
    private final ObjectMapper objectMapper;

    private static final int TEST_TIMEOUT_MS = 10000;

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

    private String getApiKey(Provider provider) {
        Map<String, Object> authConfig = provider.getAuthConfig();
        if (authConfig == null || authConfig.get("apiKey") == null) {
            throw new LlmApiException(LlmApiException.ErrorType.AUTH_FAILED, "未配置 API Key");
        }
        return (String) authConfig.get("apiKey");
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
