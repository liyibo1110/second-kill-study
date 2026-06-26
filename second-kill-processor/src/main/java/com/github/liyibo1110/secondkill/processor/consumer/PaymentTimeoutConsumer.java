package com.github.liyibo1110.secondkill.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.processor.handler.PaymentTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付超时消息消费者。
 * 订单创建后发送延迟消息，到期后检查订单支付状态。
 * 未支付的订单执行关闭操作，释放库存。
 * @author liyibo
 * @date 2026-06-25 12:09
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstants.TOPIC_PAYMENT_TIMEOUT,
        consumerGroup = MqConstants.GROUP_PAYMENT_TIMEOUT,
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class PaymentTimeoutConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final PaymentTimeoutHandler paymentTimeoutHandler;

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(String messageBody) {
        try {
            Map<String, String> payload = objectMapper.readValue(messageBody, Map.class);
            String orderNo = payload.get("orderNo");

            StructuredLog.info(log)
                    .message("收到支付超时消息")
                    .put("orderNo", orderNo)
                    .log();

            paymentTimeoutHandler.handle(orderNo);

        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("支付超时消息消费异常")
                    .exception(e)
                    .log();
            throw new RuntimeException("支付超时消息消费失败", e);
        }
    }
}
