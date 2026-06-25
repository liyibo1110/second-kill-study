package com.github.liyibo1110.secondkill.service.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.base.api.mq.SecondKillOrderMessage;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 秒杀消息发送器
 * @author liyibo
 * @date 2026-06-24 14:11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecondKillMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送秒杀下单消息到RocketMQ。
     * 使用活动编号作为hashKey，保证同一活动的消息发到同一个队列，实现局部有序。
     */
    public boolean send(SecondKillOrderMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            String destination = MqConstants.TOPIC_SECOND_KILL_ORDER + ":" + MqConstants.TAG_SECOND_KILL_ORDER;

            rocketMQTemplate.syncSendOrderly(
                    destination,
                    MessageBuilder.withPayload(json).build(),
                    message.getActivityNo()
            );

            StructuredLog.info(log)
                    .message("秒杀消息发送成功")
                    .put("userId", message.getUserId())
                    .put("activityNo", message.getActivityNo())
                    .put("skuId", message.getSkuId())
                    .put("traceId", message.getTraceId())
                    .log();
            return true;
        } catch (JsonProcessingException e) {
            StructuredLog.error(log)
                    .message("秒杀消息序列化失败")
                    .put("traceId", message.getTraceId())
                    .exception(e)
                    .log();
            return false;
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("秒杀消息发送失败")
                    .put("traceId", message.getTraceId())
                    .exception(e)
                    .log();
            return false;
        }
    }

    /**
     * 发送延迟消息，用于支付超时关单等需要延迟执行的场景。
     * @param topic      目标Topic
     * @param payload    消息内容
     * @param delayLevel 延迟级别（RocketMQ预定义的18个级别）
     */
    public boolean sendDelay(String topic, Object payload, int delayLevel) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            rocketMQTemplate.syncSend(
                    topic,
                    MessageBuilder.withPayload(json).build(),
                    3000,
                    delayLevel
            );

            StructuredLog.info(log)
                    .message("延迟消息发送成功")
                    .put("topic", topic)
                    .put("delayLevel", delayLevel)
                    .log();
            return true;
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("延迟消息发送失败")
                    .put("topic", topic)
                    .exception(e)
                    .log();
            return false;
        }
    }
}
