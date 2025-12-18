package com.finance.erp.common.exception;

import lombok.Getter;

/**
 * 业务逻辑异常
 * 当遇到 "余额不足"、"借贷不平" 等逻辑错误时抛出此异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}