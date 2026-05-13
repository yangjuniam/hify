package com.hify.common.exception;

import lombok.Getter;

@Getter
public class LlmApiException extends RuntimeException {

    private final ErrorType errorType;
    private final int statusCode;

    public LlmApiException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.statusCode = errorType.getDefaultCode();
    }

    public LlmApiException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.statusCode = errorType.getDefaultCode();
    }

    public LlmApiException(ErrorType errorType, int statusCode, String message) {
        super(message);
        this.errorType = errorType;
        this.statusCode = statusCode;
    }

    public enum ErrorType {
        TIMEOUT(408, "请求超时"),
        AUTH_FAILED(401, "认证失败"),
        RATE_LIMITED(429, "请求限流"),
        NETWORK_ERROR(500, "网络错误"),
        API_ERROR(500, "API调用失败");

        private final int defaultCode;
        private final String message;

        ErrorType(int defaultCode, String message) {
            this.defaultCode = defaultCode;
            this.message = message;
        }

        public int getDefaultCode() {
            return defaultCode;
        }

        public String getMessage() {
            return message;
        }
    }
}