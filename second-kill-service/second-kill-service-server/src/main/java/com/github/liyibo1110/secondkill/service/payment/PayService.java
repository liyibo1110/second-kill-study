package com.github.liyibo1110.secondkill.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.OrderStatusEnum;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.mq.MqConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.config.SecondKillProperties;
import com.github.liyibo1110.secondkill.support.api.FreeCardDubboService;
import com.github.liyibo1110.secondkill.support.api.PaymentDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.PayResult;
import com.github.liyibo1110.secondkill.support.api.request.CreatePayRequest;
import com.github.liyibo1110.secondkill.support.api.request.IssueCardRequest;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务，处理预支付创建和支付回调两个核心流程。
 *
 * 预支付结果缓存到Redis，避免重复调用支付网关。
 * 回调处理用分布式锁保证并发安全，更新订单后触发同步。
 * @author liyibo
 * @date 2026-06-24 13:04
 */
@Slf4j
@Service
public class PayService {

    private static final String PREPAY_CACHE_PREFIX = "secondkill:pay:prepay:";
    private static final long PREPAY_CACHE_EXPIRE_SECONDS = 300;

    private final RedisService redisService;
    private final RedissonClient redissonClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;
    private final SecondKillProperties secondKillProperties;

    @DubboReference
    private SecondKillOrderDubboService orderDubboService;

    @DubboReference
    private PaymentDubboService paymentDubboService;

    @DubboReference
    private FreeCardDubboService freeCardDubboService;

    public PayService(RedisService redisService,
                      RedissonClient redissonClient,
                      RocketMQTemplate rocketMQTemplate,
                      ObjectMapper objectMapper,
                      SecondKillProperties secondKillProperties) {
        this.redisService = redisService;
        this.redissonClient = redissonClient;
        this.rocketMQTemplate = rocketMQTemplate;
        this.objectMapper = objectMapper;
        this.secondKillProperties = secondKillProperties;
    }

    /**
     * 获取预支付信息。
     * 先查缓存，缓存没有则调用支付网关创建预支付订单。
     */
    public PayResult prepay(String orderNo, String payChannel) {
        // 查缓存
        String cacheKey = PREPAY_CACHE_PREFIX + orderNo;
        String cached = redisService.get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, PayResult.class);
            } catch (Exception e) {
                StructuredLog.warn(log)
                        .message("预支付缓存反序列化失败，重新创建")
                        .put("orderNo", orderNo)
                        .log();
            }
        }

        // 校验订单状态
        SecondKillOrderDTO order = orderDubboService.getByOrderNo(orderNo);
        if (order == null)
            throw new IllegalArgumentException("订单不存在: " + orderNo);

        if (order.getOrderStatus() != OrderStatusEnum.WAIT_PAY.getCode())
            throw new IllegalStateException("订单状态不是待支付: " + orderNo);

        // 调用支付网关创建预支付
        CreatePayRequest payRequest = CreatePayRequest.builder()
                .orderNo(orderNo)
                .userId(order.getUserId())
                .payAmount(order.getPayAmount())
                .payChannel(payChannel)
                .subject("秒杀订单-" + orderNo)
                .expireTime(LocalDateTime.now().plusSeconds(310))
                .build();

        PayResult result = paymentDubboService.createPayment(payRequest);

        // 缓存预支付结果，有效期和支付窗口一致
        try {
            String json = objectMapper.writeValueAsString(result);
            redisService.set(cacheKey, json, PREPAY_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("预支付结果缓存写入失败")
                    .put("orderNo", orderNo)
                    .exception(e)
                    .log();
        }

        return result;
    }

    /**
     * 处理支付回调。
     * 分布式锁保证同一订单的回调不会并发处理。
     */
    public void handleCallback(String channel, Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String transactionNo = params.get("transactionNo");

        if (orderNo == null || transactionNo == null) {
            StructuredLog.warn(log)
                    .message("支付回调参数缺失")
                    .put("channel", channel)
                    .log();
            return;
        }

        // 分布式锁：防止同一笔订单的回调并发处理
        String lockKey = RedisKeyConstants.PAYMENT_UPDATE_LOCK + orderNo;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                StructuredLog.warn(log)
                        .message("支付回调获取锁超时")
                        .put("orderNo", orderNo)
                        .log();
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            doHandleCallback(orderNo, transactionNo);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void doHandleCallback(String orderNo, String transactionNo) {
        // 查询订单，做幂等检查
        SecondKillOrderDTO order = orderDubboService.getByOrderNo(orderNo);
        if (order == null) {
            StructuredLog.warn(log)
                    .message("支付回调找不到订单")
                    .put("orderNo", orderNo)
                    .log();
            return;
        }

        // 幂等：已支付的订单直接返回
        if (order.getOrderStatus() != OrderStatusEnum.WAIT_PAY.getCode()) {
            StructuredLog.info(log)
                    .message("订单非待支付状态，跳过回调")
                    .put("orderNo", orderNo)
                    .put("status", order.getOrderStatus())
                    .log();
            return;
        }

        // 更新秒杀域订单状态
        orderDubboService.updatePaidInfo(orderNo, transactionNo);

        // 订单号追加到Redis对账列表
        redisService.lRightPush(RedisKeyConstants.ORDER_SYNC_LIST, orderNo);

        // 发送订单同步MQ消息（降级开关控制）
        if (!secondKillProperties.getDegrade().isSkipOrderSync())
            sendOrderSyncMessage(order, transactionNo);

        // 发放自由卡（降级开关控制，非关键路径）
        if (!secondKillProperties.getDegrade().isSkipCardIssue())
            issueCardAfterPay(order);

        // 清理预支付缓存
        redisService.delete(PREPAY_CACHE_PREFIX + orderNo);

        StructuredLog.info(log)
                .message("支付回调处理完成")
                .put("orderNo", orderNo)
                .put("transactionNo", transactionNo)
                .put("userId", order.getUserId())
                .log();
    }

    /**
     * 发送订单同步消息到MQ
     */
    private void sendOrderSyncMessage(SecondKillOrderDTO order, String transactionNo) {
        try {
            SyncOrderRequest syncRequest = SyncOrderRequest.builder()
                    .orderNo(order.getOrderNo())
                    .userId(order.getUserId())
                    .orderSource("SECKILL")
                    .totalAmount(order.getTotalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .payAmount(order.getPayAmount())
                    .paidTime(LocalDateTime.now())
                    .transactionNo(transactionNo)
                    .build();

            String json = objectMapper.writeValueAsString(syncRequest);
            rocketMQTemplate.syncSend(
                    MqConstants.TOPIC_ORDER_SYNC,
                    MessageBuilder.withPayload(json).build(),
                    3000
            );
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("订单同步消息发送失败")
                    .put("orderNo", order.getOrderNo())
                    .exception(e)
                    .log();
        }
    }

    /**
     * 支付成功后发放自由卡
     * 非关键路径操作，失败只记日志不影响支付流程
     */
    private void issueCardAfterPay(SecondKillOrderDTO order) {
        try {
            IssueCardRequest cardRequest = IssueCardRequest.builder()
                    .userId(order.getUserId())
                    .orderNo(order.getOrderNo())
                    .cardName("秒杀自由卡")
                    .faceValue(order.getPayAmount())
                    .validDays(365)
                    .build();

            String cardNo = freeCardDubboService.issueCard(cardRequest);

            StructuredLog.info(log)
                    .message("支付后发卡成功")
                    .put("orderNo", order.getOrderNo())
                    .put("cardNo", cardNo)
                    .put("userId", order.getUserId())
                    .log();
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("支付后发卡失败")
                    .put("orderNo", order.getOrderNo())
                    .put("userId", order.getUserId())
                    .exception(e)
                    .log();
        }
    }
}
