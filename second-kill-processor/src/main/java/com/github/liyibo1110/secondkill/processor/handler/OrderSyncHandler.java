package com.github.liyibo1110.secondkill.processor.handler;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.support.api.OrderSyncDubboService;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 订单同步处理器，消费端接收到同步消息后，执行以下逻辑：
 * 1、幂等检查：Redis Set判断是否已同步过。
 * 2、调用second-kill-support的Dubbo接口写入主域订单表。
 * 3、同步成功后标记到Redis Set。
 * @author liyibo
 * @date 2026-06-25 12:05
 */
@Slf4j
@Component
public class OrderSyncHandler {

    private final RedisService redisService;

    @DubboReference
    private OrderSyncDubboService orderSyncDubboService;

    public OrderSyncHandler(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * 处理订单同步
     */
    public void handle(SyncOrderRequest request) {
        String orderNo = request.getOrderNo();

        // 幂等检查：已同步过的订单直接跳过
        if (redisService.sIsMember(RedisKeyConstants.ORDER_SYNC_DONE, orderNo)) {
            StructuredLog.info(log)
                    .message("订单已同步，跳过")
                    .put("orderNo", orderNo)
                    .log();
            return;
        }

        try {
            orderSyncDubboService.syncOrder(request);

            // 同步成功，写入已完成Set
            redisService.sAdd(RedisKeyConstants.ORDER_SYNC_DONE, orderNo);

            StructuredLog.info(log)
                    .message("订单同步到主域成功")
                    .put("orderNo", orderNo)
                    .put("userId", request.getUserId())
                    .log();
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("订单同步到主域失败")
                    .put("orderNo", orderNo)
                    .put("userId", request.getUserId())
                    .exception(e)
                    .log();
            // 抛出异常让RocketMQ重试
            throw new RuntimeException("订单同步失败: " + orderNo, e);
        }
    }
}
