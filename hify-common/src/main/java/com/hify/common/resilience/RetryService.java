package com.hify.common.resilience;

import com.hify.common.exception.LlmApiException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重试服务
 *
 * 为 LLM API 调用提供重试策略：
 * - 网络超时：重试 2 次，间隔 1s
 * - 限流：退避重试（2s、4s），最多 2 次
 * - 认证失败：不重试
 * - 其他错误：不重试
 */
@Slf4j
@Service
public class RetryService {

    private final RetryRegistry registry;
    private final ConcurrentHashMap<String, Retry> retries = new ConcurrentHashMap<>();

    public RetryService() {
        this.registry = RetryRegistry.of(buildDefaultConfig());
    }

    private RetryConfig buildDefaultConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnException(this::shouldRetry)
            .build();
    }

    /**
     * 判断异常是否应该重试
     *
     * @param throwable 异常
     * @return true 应该重试，false 不应该重试
     */
    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof LlmApiException) {
            LlmApiException llmException = (LlmApiException) throwable;
            switch (llmException.getErrorType()) {
                case TIMEOUT:
                    return true;
                case RATE_LIMITED:
                    return true;
                case AUTH_FAILED:
                    log.warn("Auth failed, no retry: {}", llmException.getMessage());
                    return false;
                default:
                    return false;
            }
        }
        if (throwable instanceof java.net.SocketTimeoutException) {
            return true;
        }
        if (throwable instanceof java.net.ConnectException) {
            return true;
        }
        return false;
    }

    /**
     * 获取或创建重试器
     *
     * @param name 重试器名称
     * @return 重试器实例
     */
    public Retry getRetry(String name) {
        return retries.computeIfAbsent(name, n -> {
            Retry retry = registry.retry(n);
            retry.getEventPublisher()
                .onRetry(event -> log.debug("Retry attempt {}: {}", event.getNumberOfRetryAttempts(), name))
                .onSuccess(event -> log.debug("Retry successful after {} attempts: {}", event.getNumberOfRetryAttempts(), name))
                .onError(event -> log.warn("Retry failed after {} attempts: {}", event.getNumberOfRetryAttempts(), name, event.getLastThrowable()));
            log.info("Created retry for: {}", name);
            return retry;
        });
    }
}
