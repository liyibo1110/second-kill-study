package com.github.liyibo1110.secondkill.processor.service;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * 库存扣减服务，基于Redis Lua脚本实现原子性的库存操作。
 * 有两个核心Lua脚本：限购检查脚本和库存扣减脚本，按顺序执行。
 * @author liyibo
 * @date 2026-06-25 10:52
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductService {

    private final RedisService redisService;

    /**
     * 库存扣减Lua脚本
     * KEYS[1] = 库存key
     * ARGV[1] = 扣减数量
     *
     * 逻辑：key存在且库存 >= 扣减数量时执行DECRBY，返回1；否则返回0
     */
    private static final String DEDUCT_STOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 1) then\n" +
                    "  local current = tonumber(redis.call('GET', KEYS[1]))\n" +
                    "  local deductNum = tonumber(ARGV[1])\n" +
                    "  if current >= deductNum then\n" +
                    "    redis.call('DECRBY', KEYS[1], deductNum)\n" +
                    "    return 1\n" +
                    "  end\n" +
                    "  return 0\n" +
                    "end\n" +
                    "return 0";

    /**
     * 限购检查Lua脚本
     * KEYS[1] = 用户限购key
     * ARGV[1] = 本次购买数量
     * ARGV[2] = 限购上限
     *
     * 逻辑：key不存在则初始化为qty并返回1；
     *       key存在但累加后不超限则INCRBY并返回1；
     *       超限返回0
     */
    private static final String CHECK_USER_LIMIT_SCRIPT =
            "local qty = tonumber(ARGV[1])\n" +
                    "local limit = tonumber(ARGV[2])\n" +
                    "if (redis.call('exists', KEYS[1]) == 0) then\n" +
                    "  redis.call('SET', KEYS[1], qty)\n" +
                    "  return 1\n" +
                    "elseif (tonumber(redis.call('GET', KEYS[1])) + qty) > limit then\n" +
                    "  return 0\n" +
                    "else\n" +
                    "  redis.call('INCRBY', KEYS[1], qty)\n" +
                    "  return 1\n" +
                    "end";

    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT;
    private static final DefaultRedisScript<Long> LIMIT_SCRIPT;

    static {
        DEDUCT_SCRIPT = new DefaultRedisScript<>();
        DEDUCT_SCRIPT.setScriptText(DEDUCT_STOCK_SCRIPT);
        DEDUCT_SCRIPT.setResultType(Long.class);

        LIMIT_SCRIPT = new DefaultRedisScript<>();
        LIMIT_SCRIPT.setScriptText(CHECK_USER_LIMIT_SCRIPT);
        LIMIT_SCRIPT.setResultType(Long.class);
    }

    /**
     * 执行限购检查 + 库存扣减的完整流程
     *
     * @param activityNo    活动编号
     * @param skuId         SKU编号
     * @param userId        用户ID
     * @param quantity      购买数量
     * @param purchaseLimit 限购上限，null表示不限购
     * @return true=扣减成功
     */
    public boolean deductWithLimit(String activityNo, String skuId, Long userId, int quantity, Integer purchaseLimit) {
        // 有限购配置时，先做限购检查
        if (purchaseLimit != null && purchaseLimit > 0) {
            boolean limitOk = checkAndIncrUserLimit(userId, activityNo, skuId, quantity, purchaseLimit);
            if (!limitOk) {
                StructuredLog.info(log)
                        .message("限购检查未通过")
                        .put("userId", userId)
                        .put("activityNo", activityNo)
                        .put("skuId", skuId)
                        .put("quantity", quantity)
                        .put("purchaseLimit", purchaseLimit)
                        .log();
                return false;
            }

            // 限购通过，执行库存扣减
            boolean stockOk = deductStock(activityNo, skuId, quantity);
            if (!stockOk) {
                // 库存不足，回滚限购计数
                rollbackUserLimit(userId, activityNo, skuId, quantity);
                StructuredLog.info(log)
                        .message("库存不足，已回滚限购计数")
                        .put("userId", userId)
                        .put("activityNo", activityNo)
                        .put("skuId", skuId)
                        .log();
                return false;
            }
            return true;
        }

        // 无限购配置，直接扣减库存
        return deductStock(activityNo, skuId, quantity);
    }

    /**
     * Lua脚本扣减库存
     */
    public boolean deductStock(String activityNo, String skuId, int quantity) {
        String stockKey = RedisKeyConstants.skuStockKey(Long.valueOf(activityNo), Long.valueOf(skuId));
        Long result = redisService.executeScript(DEDUCT_SCRIPT, stockKey, String.valueOf(quantity));
        return result != null && result == 1L;
    }

    /**
     * Lua脚本检查并递增限购计数
     */
    private boolean checkAndIncrUserLimit(Long userId, String activityNo, String skuId,
                                          int quantity, int limit) {
        String limitKey = RedisKeyConstants.userPurchaseLimitKey(userId, Long.valueOf(activityNo), Long.valueOf(skuId));
        Long result = redisService.executeScript(LIMIT_SCRIPT, limitKey, String.valueOf(quantity), String.valueOf(limit));
        return result != null && result == 1L;
    }

    /**
     * 回滚限购计数（限购通过但库存扣减失败时调用）
     */
    private void rollbackUserLimit(Long userId, String activityNo, String skuId, int quantity) {
        String limitKey = RedisKeyConstants.userPurchaseLimitKey(userId, Long.valueOf(activityNo), Long.valueOf(skuId));
        redisService.increment(limitKey, -quantity);
    }

    /**
     * 释放库存（订单取消或支付超时时调用）
     */
    public void releaseStock(String activityNo, String skuId, Long userId, int quantity) {
        // 归还库存
        String stockKey = RedisKeyConstants.skuStockKey(Long.valueOf(activityNo), Long.valueOf(skuId));
        redisService.increment(stockKey, quantity);

        // 归还限购计数
        String limitKey = RedisKeyConstants.userPurchaseLimitKey(userId, Long.valueOf(activityNo), Long.valueOf(skuId));
        redisService.increment(limitKey, -quantity);

        StructuredLog.info(log)
                .message("库存和限购已释放")
                .put("activityNo", activityNo)
                .put("skuId", skuId)
                .put("userId", userId)
                .put("quantity", quantity)
                .log();
    }
}
