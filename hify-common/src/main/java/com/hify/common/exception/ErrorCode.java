package com.hify.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),

    PARAM_ERROR(1001, "参数错误"),
    PARAM_INVALID(1002, "参数不合法"),
    PARAM_EMPTY(1003, "参数不能为空"),

    UNAUTHORIZED(1010, "未授权"),
    TOKEN_EXPIRED(1011, "Token 已过期"),
    TOKEN_INVALID(1012, "Token 不合法"),

    FORBIDDEN(1020, "无访问权限"),

    NOT_FOUND(1030, "资源不存在"),

    SYSTEM_ERROR(1050, "系统内部错误"),
    SYSTEM_BUSY(1051, "系统繁忙，请稍后重试"),

    OPERATION_FAILED(1060, "操作失败"),
    OPERATION_NOT_ALLOWED(1061, "不允许的操作"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
