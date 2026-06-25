package com.github.liyibo1110.secondkill.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热服务。
 *
 * 活动开始前主动将活动信息、商品列表和库存数据加载到Redis中，避免活动开始瞬间大量请求穿透到数据库。
 * 由定时任务在活动开始前10分钟调用。
 * @author liyibo
 * @date 2026-06-24 11:31
 */
@Slf4j
@Service
public class CacheWarmupService {

    private static final long ACTIVITY_CACHE_TTL = 3600;
    private static final long PRODUCT_CACHE_TTL = 1800;
    private static final long STOCK_CACHE_TTL = 1800;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public CacheWarmupService(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    /**
     * 预热指定活动的全部缓存。
     * 加载顺序：活动信息 → 商品列表 → SKU库存。
     *
     * @param activityNo 活动编号
     */
    public void warmup(String activityNo) {
        long start = System.currentTimeMillis();

        StructuredLog.info(log)
                .message("开始缓存预热")
                .put("activityNo", activityNo)
                .log();

        try {
            warmupActivityInfo(activityNo);
            warmupProductList(activityNo);
            warmupSkuStock(activityNo);

            long cost = System.currentTimeMillis() - start;
            StructuredLog.info(log)
                    .message("缓存预热完成")
                    .put("activityNo", activityNo)
                    .put("costMs", cost)
                    .log();
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("缓存预热异常")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
        }
    }

    /**
     * 预热活动信息。
     * 从数据库查询并写入Redis。
     */
    private void warmupActivityInfo(String activityNo) {
        SecondKillActivityDTO activity = activityDubboService.getByActivityNo(activityNo);
        if (activity == null) {
            StructuredLog.warn(log)
                    .message("预热跳过：活动不存在")
                    .put("activityNo", activityNo)
                    .log();
            return;
        }

        try {
            String redisKey = RedisKeyConstants.ACTIVITY_INFO + activityNo;
            String json = objectMapper.writeValueAsString(activity);
            redisService.set(redisKey, json, ACTIVITY_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("活动信息预热写入Redis失败")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
            return;
        }

        StructuredLog.info(log)
                .message("活动信息预热完成")
                .put("activityNo", activityNo)
                .put("activityName", activity.getActivityName())
                .log();
    }

    /**
     * 预热商品列表。
     * 将活动下所有商品数据加载到Redis。
     */
    private void warmupProductList(String activityNo) {
        List<SecondKillActivityProductDTO> products = productDubboService.listActivityProducts(activityNo);
        if (products == null || products.isEmpty()) {
            StructuredLog.warn(log)
                    .message("预热跳过：活动无商品")
                    .put("activityNo", activityNo)
                    .log();
            return;
        }

        try {
            String redisKey = RedisKeyConstants.ACTIVITY_PRODUCT_LIST + activityNo;
            String json = objectMapper.writeValueAsString(products);
            redisService.set(redisKey, json, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("商品列表预热写入Redis失败")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
            return;
        }

        StructuredLog.info(log)
                .message("商品列表预热完成")
                .put("activityNo", activityNo)
                .put("productCount", products.size())
                .log();
    }

    /**
     * 预热SKU库存。
     * 将每个SKU的库存值单独写入Redis，供Lua脚本扣减使用。
     */
    private void warmupSkuStock(String activityNo) {
        List<SecondKillActivityProductSkuDTO> skuList = productDubboService.listActivityProductSkus(activityNo);
        if (skuList == null || skuList.isEmpty()) {
            return;
        }

        int count = 0;
        for (SecondKillActivityProductSkuDTO sku : skuList) {
            String stockKey = RedisKeyConstants.skuStockKey(
                    Long.valueOf(activityNo), sku.getProductId());
            // 库存值用String类型存储，供Lua脚本做INCRBY/DECRBY操作
            redisService.set(stockKey, String.valueOf(sku.getActivityStock()),
                    STOCK_CACHE_TTL, TimeUnit.SECONDS);
            count++;
        }

        StructuredLog.info(log)
                .message("SKU库存预热完成")
                .put("activityNo", activityNo)
                .put("skuCount", count)
                .log();
    }
}
