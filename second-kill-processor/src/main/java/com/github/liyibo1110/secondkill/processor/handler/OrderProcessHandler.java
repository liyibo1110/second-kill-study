package com.github.liyibo1110.secondkill.processor.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.base.api.mq.SecondKillOrderMessage;
import com.github.liyibo1110.secondkill.base.api.request.CreateOrderRequest;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.processor.service.IdempotentService;
import com.github.liyibo1110.secondkill.processor.service.StockDeductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 秒杀订单处理器，消费端核心业务逻辑入口，完整流程：
 * 幂等校验(SETNX) → 消费端二次过滤 → 库存扣减 → 创建订单 → 发延迟消息 → 写入结果
 *
 * @author liyibo
 * @date 2026-06-25 10:51
 */
@Slf4j
@Component
public class OrderProcessHandler {

    private final RedisService redisService;
    private final StockDeductService stockDeductService;
    private final IdempotentService idempotentService;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @DubboReference
    private SecondKillOrderDubboService orderDubboService;

    public OrderProcessHandler(RedisService redisService,
                               StockDeductService stockDeductService,
                               IdempotentService idempotentService,
                               RocketMQTemplate rocketMQTemplate,
                               ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.stockDeductService = stockDeductService;
        this.idempotentService = idempotentService;
        this.rocketMQTemplate = rocketMQTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理秒杀下单消息
     */
    public void process(SecondKillOrderMessage message) {
        String traceId = message.getTraceId();

        // 第一层幂等：SETNX原子抢占处理权
        if (!idempotentService.tryAcquire(traceId))
            return;

        try {
            doProcess(message);
        } catch (Exception e) {
            // 处理异常，释放处理权，允许RocketMQ重试
            idempotentService.release(traceId);
            throw e;
        }
    }

    private void doProcess(SecondKillOrderMessage message) {
        String traceId = message.getTraceId();

        // 消费端二次过滤：检查活动是否仍在进行
        String activityInfoKey = RedisKeyConstants.ACTIVITY_INFO + message.getActivityNo();
        if (!redisService.hasKey(activityInfoKey)) {
            StructuredLog.info(log)
                    .message("消费端过滤：活动不存在")
                    .put("traceId", traceId)
                    .put("activityNo", message.getActivityNo())
                    .log();
            idempotentService.markFail(traceId, "ACTIVITY_CLOSED");
            return;
        }

        // 消费端二次过滤：检查用户是否为风险用户
        String riskKey = RedisKeyConstants.riskUserKey(message.getUserId());
        if (redisService.hasKey(riskKey)) {
            StructuredLog.info(log)
                    .message("消费端过滤：风险用户")
                    .put("traceId", traceId)
                    .put("userId", message.getUserId())
                    .log();
            idempotentService.markFail(traceId, "RISK_USER");
            return;
        }

        // 库存扣减（含限购检查）
        boolean deductOk = stockDeductService.deductWithLimit(
                message.getActivityNo(),
                message.getSkuId(),
                message.getUserId(),
                message.getQuantity(),
                null
        );
        if (!deductOk) {
            StructuredLog.info(log)
                    .message("库存扣减失败")
                    .put("traceId", traceId)
                    .put("userId", message.getUserId())
                    .put("activityNo", message.getActivityNo())
                    .put("skuId", message.getSkuId())
                    .log();
            idempotentService.markFail(traceId, "STOCK_EMPTY");
            return;
        }

        // 通过Dubbo调用seckill-base创建订单
        String orderNo = null;
        try {
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .userId(message.getUserId())
                    .activityNo(message.getActivityNo())
                    .skuNo(message.getSkuId())
                    .quantity(message.getQuantity())
                    .payAmount(message.getTotalFee())
                    .traceId(traceId)
                    .build();

            SecondKillOrderDTO orderDTO = orderDubboService.createOrder(request);
            if (orderDTO != null)
                orderNo = orderDTO.getOrderNo();

        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("订单创建异常")
                    .put("traceId", traceId)
                    .exception(e)
                    .log();
        }

        // 生单失败，释放库存
        if (orderNo == null) {
            stockDeductService.releaseStock(
                    message.getActivityNo(),
                    message.getSkuId(),
                    message.getUserId(),
                    message.getQuantity()
            );
            idempotentService.markFail(traceId, "ORDER_FAIL");
            StructuredLog.warn(log)
                    .message("生单失败，库存已释放")
                    .put("traceId", traceId)
                    .put("userId", message.getUserId())
                    .log();
            return;
        }

        // 发送支付超时延迟消息
        sendPaymentTimeoutMessage(orderNo);

        // 标记处理成功，写入订单号
        idempotentService.markSuccess(traceId, orderNo);

        StructuredLog.info(log)
                .message("秒杀订单处理完成")
                .put("traceId", traceId)
                .put("userId", message.getUserId())
                .put("activityNo", message.getActivityNo())
                .put("orderNo", orderNo)
                .log();
    }

    /**
     * 发送支付超时延迟消息
     */
    private void sendPaymentTimeoutMessage(String orderNo) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("orderNo", orderNo));
            rocketMQTemplate.syncSend(
                    MqConstants.TOPIC_PAYMENT_TIMEOUT,
                    MessageBuilder.withPayload(json).build(),
                    3000,
                    MqConstants.DELAY_LEVEL_10_MIN
            );
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("支付超时延迟消息发送失败")
                    .put("orderNo", orderNo)
                    .exception(e)
                    .log();
        }
    }
}
