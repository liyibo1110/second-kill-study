package com.github.liyibo1110.secondkill.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.common.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结构化日志工具类，将日志内容组织成JSON格式的键值对输出，方便日志平台解析和检索。
 * @author liyibo
 * @date 2026-06-22 13:57
 */
@Slf4j
public class StructuredLog {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> fields = new LinkedHashMap<>();

    private final LogLevel level;

    private final Logger logger;

    private String message;

    private Throwable throwable;

    private enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }

    private StructuredLog(LogLevel level, Logger logger) {
        this.level = level;
        this.logger = logger;
    }

    public static StructuredLog info(Logger logger) {
        return new StructuredLog(LogLevel.INFO, logger);
    }

    public static StructuredLog info() {
        return new StructuredLog(LogLevel.INFO, log);
    }

    public static StructuredLog warn(Logger logger) {
        return new StructuredLog(LogLevel.WARN, logger);
    }

    public static StructuredLog warn() {
        return new StructuredLog(LogLevel.WARN, log);
    }

    public static StructuredLog error(Logger logger) {
        return new StructuredLog(LogLevel.ERROR, logger);
    }

    public static StructuredLog error() {
        return new StructuredLog(LogLevel.ERROR, log);
    }

    public static StructuredLog debug(Logger logger) {
        return new StructuredLog(LogLevel.DEBUG, logger);
    }

    public static StructuredLog debug() {
        return new StructuredLog(LogLevel.DEBUG, log);
    }

    public StructuredLog message(String message) {
        this.message = message;
        return this;
    }

    public StructuredLog exception(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public StructuredLog put(String key, Object value) {
        if (key != null && value != null)
            fields.put(key, value);
        return this;
    }

    /**
     * 输出JSON版本的日志信息。
     */
    public void log() {
        String logContent = buildLogContent();

        switch (level) {
            case INFO:
                if (throwable != null)
                    logger.info(logContent, throwable);
                else
                    logger.info(logContent);

                break;
            case WARN:
                if (throwable != null)
                    logger.warn(logContent, throwable);
                else
                    logger.warn(logContent);

                break;
            case ERROR:
                if (throwable != null)
                    logger.error(logContent, throwable);
                else
                    logger.error(logContent);

                break;
            case DEBUG:
                if (throwable != null)
                    logger.debug(logContent, throwable);
                else
                    logger.debug(logContent);

                break;
        }
    }

    private String buildLogContent() {
        // 自动注入traceId，确保每条日志都能关联到请求链路
        String traceId = TraceContext.getTraceId();
        if (traceId != null && !traceId.isEmpty() && !fields.containsKey("traceId"))
            fields.put("traceId", traceId);

        StringBuilder sb = new StringBuilder();
        if (message != null && !message.isEmpty())
            sb.append(message).append(" || ");

        try {
            sb.append(MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            sb.append(fields);
        }

        return sb.toString();
    }
}
