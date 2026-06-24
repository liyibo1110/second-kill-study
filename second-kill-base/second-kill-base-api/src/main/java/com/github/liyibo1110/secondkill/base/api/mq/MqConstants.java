package com.github.liyibo1110.secondkill.base.api.mq;

/**
 * RocketMQ的常量定义。
 * 集中管理所有Topic、消费者组、Tag命名，生产端和消费端都会共用这些。
 * @author liyibo
 * @date 2026-06-23 15:45
 */
public final class MqConstants {

    private MqConstants() {}

    // ==================== Topic ====================

    /**
     * 秒杀下单消息。
     * 生产端：second-kill-service发送有序消息。
     * 消费端：second-kill-processor顺序消费，执行库存扣减和订单创建。
     */
    public static final String TOPIC_SECOND_KILL_ORDER = "second-kill-order-topic";

    /**
     * 支付超时关单消息。
     * 生产端：second-kill-processor在创建订单时发送延迟消息。
     * 消费端：second-kill-processor消费，检查订单支付状态并关闭超时订单。
     */
    public static final String TOPIC_PAYMENT_TIMEOUT = "second-kill-payment-timeout-topic";

    /**
     * 订单同步消息。
     * 生产端：支付成功后发送，携带秒杀订单的完整支付信息。
     * 消费端：second-kill-processor消费，通过Dubbo调用second-kill-support写入主域订单表。
     */
    public static final String TOPIC_ORDER_SYNC = "second-kill-order-sync-topic";

    // ==================== 消费者组 ====================

    /** 秒杀下单消费者组 */
    public static final String GROUP_SECOND_KILL_ORDER = "second-kill-order-consumer-group";

    /** 支付超时消费者组 */
    public static final String GROUP_PAYMENT_TIMEOUT = "second-kill-payment-timeout-consumer-group";

    /** 订单同步消费者组 */
    public static final String GROUP_ORDER_SYNC = "second-kill-order-sync-consumer-group";

    // ==================== Tag ====================

    /** 秒杀下单Tag */
    public static final String TAG_SECOND_KILL_ORDER = "second-kill-order";

    // ==================== 延迟级别 ====================

    /**
     * RocketMQ延迟级别对照：
     * 1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m, 9=5m,
     * 10=6m, 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     *
     * 支付超时使用10分钟（级别14），实际超时时间由业务层二次判断
     */
    public static final int DELAY_LEVEL_10_MIN = 14;
}
