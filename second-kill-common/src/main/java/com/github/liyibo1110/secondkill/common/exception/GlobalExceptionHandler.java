package com.github.liyibo1110.secondkill.common.exception;

import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，统一拦截所有异常，并转换为标准的Result结构返回给前端。
 *
 * 注意这个涉及到了Sentinel的熔断机制：Sentinel判断一个请求是否失败，看的是HTTP状态码，即只对5xx的状态码计入熔断指标，
 * 因此业务异常在这里会返回4xx，这样不会被Sentinel计入。
 *
 * 这里面覆盖了10种异常类型，最后一个是兜底的Exception，只有这个会返回500。
 * @author liyibo
 * @date 2026-06-22 13:49
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBizException(BizException e) {
        StructuredLog.warn(log)
                .message("BizException")
                .put("code", e.getCode())
                .put("message", e.getMessage())
                .log();
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(ValidationException e) {
        StructuredLog.warn(log)
                .message("ValidationException")
                .put("code", e.getCode())
                .put("message", e.getMessage())
                .log();
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        StructuredLog.warn(log)
                .message("IllegalArgumentException")
                .put("detail", e.getMessage())
                .log();
        return Result.fail(ErrorEnum.PARAM_INVALID_VALUE.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        StructuredLog.warn(log)
                .message("MethodArgumentNotValidException")
                .put("detail", message)
                .log();
        return Result.fail(ErrorEnum.PARAM_INVALID_VALUE.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        StructuredLog.warn(log)
                .message("BindException")
                .put("detail", message)
                .log();
        return Result.fail(ErrorEnum.PARAM_INVALID_VALUE.getCode(), message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        StructuredLog.warn(log)
                .message("HttpRequestMethodNotSupportedException")
                .put("detail", e.getMessage())
                .log();
        return Result.fail(ErrorEnum.BAD_REQUEST.getCode(), "不支持当前请求方法");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        StructuredLog.warn(log)
                .message("HttpMediaTypeNotSupportedException")
                .put("detail", e.getMessage())
                .log();
        return Result.fail(ErrorEnum.BAD_REQUEST.getCode(), "不支持当前媒体类型");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        StructuredLog.warn(log)
                .message("HttpMessageNotReadableException")
                .put("detail", e.getMessage())
                .log();
        return Result.fail(ErrorEnum.MISSING_PARAMS);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        StructuredLog.warn(log)
                .message("MissingServletRequestParameterException")
                .put("parameterName", e.getParameterName())
                .put("detail", e.getMessage())
                .log();
        return Result.fail(ErrorEnum.MISSING_PARAMS.getCode(), "缺少请求参数: " + e.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        StructuredLog.error(log)
                .message("Unexpected exception")
                .exception(e)
                .log();
        return Result.fail(ErrorEnum.SYSTEM_ERROR);
    }
}
