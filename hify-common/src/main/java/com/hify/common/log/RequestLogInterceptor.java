package com.hify.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求日志拦截器
 *
 * 记录每个请求的 method、path、status、耗时。
 * 请求进入时生成 traceId 放入 MDC，请求结束时清理 MDC。
 * 慢请求（>1s）记录为 WARN 级别。
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = generateTraceId();
        MDC.put(TRACE_ID, traceId);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String method = request.getMethod();
                String path = request.getRequestURI();
                int status = response.getStatus();

                if (duration > 1000) {
                    log.warn("SLOW REQUEST - {} {} status={} duration={}ms traceId={}",
                        method, path, status, duration, MDC.get(TRACE_ID));
                } else {
                    log.info("{} {} status={} duration={}ms traceId={}",
                        method, path, status, duration, MDC.get(TRACE_ID));
                }
            }
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}