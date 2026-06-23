package com.github.liyibo1110.secondkill.common.result;

import com.github.liyibo1110.secondkill.common.exception.ErrorEnum;
import com.github.liyibo1110.secondkill.common.exception.IErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Optional;

/**
 * 统一API的响应结果。
 * 所有接口的返回值，统一用Result来包装，结构固定为code、message、data和success这4个字段。
 *
 * 以下是设计要点：
 * 1、code字段用字符串，而不是数字，因为字符串具有可读性。
 * 2、success字段是为了让前端直接判断result.succuess，而不是result.code === 'success'
 *
 * @author liyibo
 * @date 2026-06-22 13:19
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SUCCESS_CODE = "success";
    private static final String SUCCESS_MESSAGE = "操作成功";

    private String code;
    private String message;
    private T data;
    private Boolean success;

    private Result(String code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    /**
     * 成功 + 无message + 无data
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, true);
    }

    /**
     * 成功 + 无message + 有data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, true);
    }

    /**
     * 成功 + 有message + 有data
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(SUCCESS_CODE, message, data, true);
    }

    /**
     * 失败 + 无code + 有message + 无data
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ErrorEnum.SYSTEM_ERROR.getCode(), message, null, false);
    }

    /**
     * 失败 + 有code + 有Message + 无data
     */
    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null, false);
    }

    /**
     * 失败 + 无code + 无Message + 无data
     */
    public static <T> Result<T> fail(ErrorEnum errorEnum) {
        return new Result<>(errorEnum.getCode(), errorEnum.getMessage(), null, false);
    }

    /**
     * 失败 + 无code + 有Message + 无data
     */
    public static <T> Result<T> fail(ErrorEnum errorEnum, String message) {
        return new Result<>(errorEnum.getCode(), message, null, false);
    }

    /**
     * 失败 + 有code + 有Message + 无data
     */
    public static <T> Result<T> fail(IErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    /**
     * result如果为null，也返回false。
     */
    public static boolean isSuccess(Result<?> result) {
        return Optional.ofNullable(result)
                .map(Result::getSuccess)
                .orElse(Boolean.FALSE);
    }

    /**
     * result如果为null，则返回true。
     */
    public static boolean isFailed(Result<?> result) {
        return !isSuccess(result);
    }
}
