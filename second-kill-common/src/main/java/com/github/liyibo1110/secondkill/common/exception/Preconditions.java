package com.github.liyibo1110.secondkill.common.exception;

/**
 * 条件预校验器，是个工具类，未通过验证的条件，会返回BizException。
 * @author liyibo
 * @date 2026-06-22 13:43
 */
public final class Preconditions {

    private Preconditions() {}

    public static void checkArgument(boolean expression, String message) {
        if (!expression)
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE.getCode(), message);
    }

    public static void checkArgument(boolean expression, String errorCode, String errorMessage) {
        if (!expression)
            throw BizException.of(errorCode, errorMessage);
    }

    public static void checkArgument(boolean expression, IErrorCode errorCode) {
        if (!expression)
            throw BizException.of(errorCode);
    }

    public static void checkArgument(boolean expression, ErrorEnum error) {
        if (!expression)
            throw BizException.of(error);
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null)
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE);
        return reference;
    }

    public static <T> T checkNotNull(T reference, String message) {
        if (reference == null)
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE.getCode(), message);
        return reference;
    }

    public static <T> T checkNotNull(T reference, String errorCode, String errorMessage) {
        if (reference == null)
            throw BizException.of(errorCode, errorMessage);
        return reference;
    }

    public static <T> T checkNotNull(T reference, IErrorCode errorCode) {
        if (reference == null)
            throw BizException.of(errorCode);
        return reference;
    }

    public static <T> T checkNotNull(T reference, ErrorEnum error) {
        if (reference == null)
            throw BizException.of(error);
        return reference;
    }

    public static String checkNotBlank(String reference) {
        if (reference == null || reference.isEmpty())
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE);
        return reference;
    }

    public static String checkNotBlank(String reference, String message) {
        if (reference == null || reference.isEmpty())
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE.getCode(), message);
        return reference;
    }

    public static String checkNotBlank(String reference, String errorCode, String errorMessage) {
        if (reference == null || reference.isEmpty())
            throw BizException.of(errorCode, errorMessage);
        return reference;
    }

    public static String checkNotBlank(String reference, IErrorCode errorCode) {
        if (reference == null || reference.isEmpty())
            throw BizException.of(errorCode);
        return reference;
    }

    public static String checkNotBlank(String reference, ErrorEnum error) {
        if (reference == null || reference.isEmpty())
            throw BizException.of(error);
        return reference;
    }
}
