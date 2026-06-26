package com.github.liyibo1110.secondkill.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.base.api.mq.SecondKillOrderMessage;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.processor.handler.OrderProcessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单消息消费者。
 * 使用顺序消费模式，同一个活动的消息在同一个队列中按序处理，避免并发扣减库存带来的超卖风险。
 * @author liyibo
 * @date 2026-06-25 12:07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstants.TOPIC_SECOND_KILL_ORDER,
        consumerGroup = MqConstants.GROUP_SECOND_KILL_ORDER,
        consumeMode = ConsumeMode.ORDERLY,
        maxReconsumeTimes = 3
)
public class SecondKillOrderConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final OrderProcessHandler orderProcessHandler;

    @Override
    public void onMessage(String messageBody) {
        SecondKillOrderMessage message = null;
        try {
            message = objectMapper.readValue(messageBody, SecondKillOrderMessage.class);

            StructuredLog.info(log)
                    .message("收到秒杀下单消息")
                    .put("userId", message.getUserId())
                    .put("activityNo", message.getActivityNo())
                    .put("skuId", message.getSkuId())
                    .put("traceId", message.getTraceId())
                    .log();

            orderProcessHandler.process(message);
        } catch (Exception e) {
            String traceId = message != null ? message.getTraceId() : "unknown";
            StructuredLog.error(log)
                    .message("秒杀消息消费异常")
                    .put("traceId", traceId)
                    .exception(e)
                    .log();
            // 抛出异常触发RocketMQ重试
            throw new RuntimeException("秒杀消息消费失败, traceId=" + traceId, e);
        }
    }
}
