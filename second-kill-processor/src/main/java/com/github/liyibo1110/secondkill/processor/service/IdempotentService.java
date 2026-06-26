package com.github.liyibo1110.secondkill.processor.service;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 幂等校验服务，提供基于Redis SETNX的幂等校验能力。
 * 使用SETNX原子操作抢占处理权，避免hasKey+set两步操作之间的竞态条件。
 * @author liyibo
 * @date 2026-06-25 10:52
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotentService {

    private final RedisService redisService;

    /** 占位值，表示消息正在处理中 */
    private static final String PROCESSING = "PROCESSING";

    /** 处理中状态的过期时间（秒），防止处理异常后永远无法重试 */
    private static final long PROCESSING_EXPIRE_SECONDS = 60;

    /** 最终结果的过期时间（分钟） */
    private static final long RESULT_EXPIRE_MINUTES = 5;

    /**
     * 尝试获取处理权
     * 使用SETNX原子操作，同一个traceId只有一个消费线程能拿到处理权。
     *
     * @param traceId 链路追踪ID
     * @return true=获取成功，可以处理；false=已被其他线程处理
     */
    public boolean tryAcquire(String traceId) {
        String resultKey = RedisKeyConstants.orderResultKey(traceId);

        // SETNX: 只有key不存在时才能设置成功
        Boolean acquired = redisService.setIfAbsent(resultKey, PROCESSING, PROCESSING_EXPIRE_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired))
            return true;

        // key已存在，检查是否是PROCESSING状态（上一次处理可能卡住了）
        String currentValue = redisService.get(resultKey);
        if (PROCESSING.equals(currentValue)) {
            StructuredLog.info(log)
                    .message("消息正在被其他线程处理")
                    .put("traceId", traceId)
                    .log();
        } else {
            StructuredLog.info(log)
                    .message("消息已处理完成，跳过重复消费")
                    .put("traceId", traceId)
                    .put("result", currentValue)
                    .log();
        }
        return false;
    }

    /**
     * 标记处理成功，写入最终结果
     *
     * @param traceId 链路追踪ID
     * @param orderNo 订单号
     */
    public void markSuccess(String traceId, String orderNo) {
        String resultKey = RedisKeyConstants.orderResultKey(traceId);
        redisService.set(resultKey, orderNo, RESULT_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 标记处理失败，写入失败原因
     *
     * @param traceId    链路追踪ID
     * @param failReason 失败原因
     */
    public void markFail(String traceId, String failReason) {
        String resultKey = RedisKeyConstants.orderResultKey(traceId);
        redisService.set(resultKey, failReason, RESULT_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 释放处理权（处理失败且需要允许重试时调用）
     * 只删除PROCESSING状态的key，已有最终结果的key不删
     *
     * @param traceId 链路追踪ID
     */
    public void release(String traceId) {
        String resultKey = RedisKeyConstants.orderResultKey(traceId);
        String currentValue = redisService.get(resultKey);
        if (PROCESSING.equals(currentValue))
            redisService.delete(resultKey);
    }
}
