package com.github.liyibo1110.secondkill.base.api.constant;

/**
 * Redis key的前缀常量。
 * 命名规则：业务域:实体:用途，末尾还是冒号的，表示需要再拼接业务ID。
 * @author liyibo
 * @date 2026-06-23 15:34
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    public static final String SEPARATOR = ":";

    // ==================== 活动相关 ====================

    /** 秒杀活动详情，拼接活动ID */
    public static final String ACTIVITY_INFO = "secondkill:activity:info:";

    /** 秒杀活动状态，拼接活动ID */
    public static final String ACTIVITY_STATUS = "secondkill:activity:status:";

    /** 秒杀活动商品列表，拼接活动ID */
    public static final String ACTIVITY_PRODUCT_LIST = "secondkill:activity:product:list:";

    // ==================== 商品与库存相关 ====================

    /** 商品SKU库存，拼接活动ID:skuId */
    public static final String PRODUCT_SKU_STOCK = "secondkill:product:sku:stock:";

    /** 商品SKU详情，拼接skuNo */
    public static final String PRODUCT_SKU_INFO = "secondkill:product:sku:info:";

    /** 商品SKU关联关系，拼接活动ID */
    public static final String PRODUCT_SKU_RELATION = "secondkill:product:sku:relation:";

    /** 库存扣减锁，拼接活动ID:skuId */
    public static final String STOCK_DEDUCT_LOCK = "secondkill:stock:deduct:lock:";

    // ==================== 订单相关 ====================

    /** 秒杀订单队列，拼接活动ID */
    public static final String ORDER_QUEUE = "secondkill:order:queue:";

    /** 秒杀订单详情，拼接订单ID */
    public static final String ORDER_DETAIL = "secondkill:order:detail:";

    /** 排队状态，拼接用户ID:活动ID */
    public static final String ORDER_QUEUING = "secondkill:order:queuing:";

    // ==================== 用户相关 ====================

    /** 用户限购计数，拼接用户ID:活动ID:商品ID */
    public static final String USER_PURCHASE_LIMIT = "secondkill:user:purchase:limit:";

    /** 用户信息缓存，拼接用户ID */
    public static final String USER_INFO = "secondkill:user:info:";

    /** 用户收货地址，Hash类型，拼接用户ID */
    public static final String USER_ADDRESS = "secondkill:user:address:";

    // ==================== 风控与限流 ====================

    /** 接口限流配置，拼接活动ID */
    public static final String RATE_LIMIT = "secondkill:rate:limit:";

    /** 机审校验随机串，拼接用户ID */
    public static final String MACHINE_CHECK = "secondkill:machine:check:";

    /** 用户级限流器，拼接用户ID */
    public static final String USER_RATE_LIMIT = "secondkill:user:rate:limit:";

    /** 风险用户黑名单，拼接用户ID */
    public static final String RISK_USER = "secondkill:risk:user:";

    // ==================== 消费结果 ====================

    /** 秒杀订单处理结果，拼接traceId，消费端写入，前端轮询读取 */
    public static final String ORDER_RESULT = "secondkill:order:result:";

    // ==================== 分布式锁 ====================

    /** 支付状态更新锁，拼接订单ID */
    public static final String PAYMENT_UPDATE_LOCK = "secondkill:payment:update:lock:";

    // ==================== 订单同步 ====================

    /** 订单同步对账列表（Redis List），支付成功后追加订单号，对账任务消费 */
    public static final String ORDER_SYNC_LIST = "secondkill:order:sync:list";

    /** 订单同步幂等Set，已同步成功的订单号缓存，防止重复写入主域 */
    public static final String ORDER_SYNC_DONE = "secondkill:order:sync:done";

    // ==================== Key 拼接工具方法 ====================

    public static String skuStockKey(Long activityId, Long skuId) {
        return PRODUCT_SKU_STOCK + activityId + SEPARATOR + skuId;
    }

    public static String userPurchaseLimitKey(Long userId, Long activityId, Long productId) {
        return USER_PURCHASE_LIMIT + userId + SEPARATOR + activityId + SEPARATOR + productId;
    }

    public static String orderQueuingKey(Long userId, Long activityId) {
        return ORDER_QUEUING + userId + SEPARATOR + activityId;
    }

    public static String stockDeductLockKey(Long activityId, Long skuId) {
        return STOCK_DEDUCT_LOCK + activityId + SEPARATOR + skuId;
    }

    public static String machineCheckKey(Long userId) {
        return MACHINE_CHECK + userId;
    }

    public static String userRateLimitKey(Long userId) {
        return USER_RATE_LIMIT + userId;
    }

    public static String riskUserKey(Long userId) {
        return RISK_USER + userId;
    }

    public static String orderResultKey(String traceId) {
        return ORDER_RESULT + traceId;
    }
}
