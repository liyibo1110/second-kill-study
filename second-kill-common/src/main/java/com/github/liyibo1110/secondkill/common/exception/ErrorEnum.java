package com.github.liyibo1110.secondkill.common.exception;

import lombok.Getter;

/**
 * 错误码枚举，所有错误码都要集中在这里，不能在业务代码里直接写字符串。
 * 注意这里面只有通用的框架级错误码，业务模块如果要定义自己的错误码，实现IErrorCode接口即可，不要修改ErrorEnum。
 * @author liyibo
 * @date 2026-06-22 13:31
 */
@Getter
public enum ErrorEnum implements IErrorCode {

    SUCCESS("success", "操作成功"),
    BAD_REQUEST("bad_request", "请求参数错误"),
    UNAUTHORIZED("unauthorized", "未登录或登录已过期"),
    FORBIDDEN("forbidden", "无访问权限"),
    NOT_FOUND("not_found", "资源不存在"),
    SYSTEM_ERROR("system_error", "系统繁忙，请稍后重试"),
    PARAM_EMPTY("param_empty", "参数值未传递"),
    PARAM_INVALID_VALUE("param_invalid_value", "参数值经验证无效"),
    MISSING_PARAMS("missing_params", "参数缺失"),
    OPERATION_FORBIDDEN("operation_forbidden", "操作被禁止"),
    NOT_LOGIN("not_login", "当前用户未登录"),
    LOGIN_FAILED("login_failed", "登录失败"),
    TOO_MANY_REQUESTS("too_many_requests", "当前请求人数较多，请稍后重试"),
    FETCH_LOCK_FAILED("fetch_lock_failed", "系统繁忙，请重试"),
    DATA_NOT_FOUND("data_not_found", "数据不存在");

    private final String code;
    private final String message;

    ErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
