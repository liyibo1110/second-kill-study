package com.github.liyibo1110.secondkill.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.processor.handler.OrderSyncHandler;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单同步消费者，支付成功后，秒杀域发送同步消息到MQ。
 * 本消费者接收消息后，通过Dubbo调用second-kill-support将订单写入主域订单表。
 * 使用并发消费模式，吞吐优先。
 * @author liyibo
 * @date 2026-06-25 12:10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstants.TOPIC_ORDER_SYNC,
        consumerGroup = MqConstants.GROUP_ORDER_SYNC,
        maxReconsumeTimes = 5
)
public class OrderSyncConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final OrderSyncHandler orderSyncHandler;

    @Override
    public void onMessage(String message) {
        SyncOrderRequest request;
        try {
            request = objectMapper.readValue(message, SyncOrderRequest.class);
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("订单同步消息反序列化失败")
                    .put("raw", message)
                    .exception(e)
                    .log();
            return;
        }

        orderSyncHandler.handle(request);
    }
}
