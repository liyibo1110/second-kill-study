package com.github.liyibo1110.secondkill.common.exception;

import lombok.Getter;

/**
 * 参数校验异常。
 * @author liyibo
 * @date 2026-06-22 13:46
 */
@Getter
public class ValidationException extends RuntimeException {

    private final String code;
    private final String message;

    public ValidationException(String message) {
        super(message);
        this.code = ErrorEnum.PARAM_INVALID_VALUE.getCode();
        this.message = message;
    }

    public ValidationException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
