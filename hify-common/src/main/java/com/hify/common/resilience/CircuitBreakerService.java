package com.hify.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 熔断器服务
 *
 * 为每个 LLM 提供商管理独立的熔断器实例。当某个提供商的请求失败率超过阈值时，
 * 熔断器会打开，后续请求会快速失败，避免级联故障。
 */
@Slf4j
@Service
public class CircuitBreakerService {

    private final CircuitBreakerRegistry registry;
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public CircuitBreakerService() {
        this.registry = CircuitBreakerRegistry.of(buildDefaultConfig());
    }

    private CircuitBreakerConfig buildDefaultConfig() {
        return CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();
    }

    /**
     * 获取或创建指定提供商的熔断器
     *
     * @param providerName 提供商名称（如 openai、claude、gemini）
     * @return 熔断器实例
     */
    public CircuitBreaker getCircuitBreaker(String providerName) {
        return circuitBreakers.computeIfAbsent(providerName, name -> {
            CircuitBreaker circuitBreaker = registry.circuitBreaker(name);
            circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.info("Circuit breaker state changed: {} -> {}",
                    name, event.getStateTransition()));

            log.info("Created circuit breaker for provider: {}", name);
            return circuitBreaker;
        });
    }

    /**
     * 获取指定提供商的熔断器状态
     *
     * @param providerName 提供商名称
     * @return 熔断器状态字符串
     */
    public String getState(String providerName) {
        CircuitBreaker circuitBreaker = circuitBreakers.get(providerName);
        if (circuitBreaker == null) {
            return "UNKNOWN";
        }
        return circuitBreaker.getState().name();
    }
}
