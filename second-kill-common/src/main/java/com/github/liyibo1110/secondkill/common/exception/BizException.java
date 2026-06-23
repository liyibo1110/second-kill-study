package com.github.liyibo1110.secondkill.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * @author liyibo
 * @date 2026-06-22 13:37
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;
    private final String message;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public static BizException of(String errorCode, String errorMsg) {
        return new BizException(errorCode, errorMsg);
    }

    public static BizException of(IErrorCode errorCode) {
        return new BizException(errorCode.getCode(), errorCode.getMessage());
    }

    public static BizException of(ErrorEnum errorEnum) {
        return new BizException(errorEnum.getCode(), errorEnum.getMessage());
    }

    /**
     * 要注意这个重写，是直接返回this，而不会去填充堆栈。
     * BizException是预期内的业务异常（库存不足，活动未开始之类的），它不需要完整的堆栈信息。
     * JVM构造异常对象时，填充堆栈是最耗时的部分，秒杀场景下，高并发请求中大量用户会触发这个异常，可以省掉不必要的性能开销。
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
