package com.github.liyibo1110.job.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务。
 * 扫描即将开始的活动（10分钟内），将活动信息、商品列表、SKU库存，批量加载到Redis，避免活动开始瞬间大量请求穿透到数据库。
 * 建议执行频率：每5分钟一次。
 * @author liyibo
 * @date 2026-06-27 12:56
 */
@Slf4j
@Component
public class CacheWarmupTask {

    private static final long ACTIVITY_CACHE_TTL = 3600;
    private static final long PRODUCT_CACHE_TTL = 1800;
    private static final long STOCK_CACHE_TTL = 1800;
    private static final int WARMUP_AHEAD_MINUTES = 10;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public CacheWarmupTask(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @XxlJob("cacheWarmup")
    public void execute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warmupDeadline = now.plusMinutes(WARMUP_AHEAD_MINUTES);

        // 查询所有未开始的活动
        List<SecondKillActivityDTO> upcoming = activityDubboService.listByStatus(0);
        for (SecondKillActivityDTO activity : upcoming) {
            if (activity.getStartTime() == null)
                continue;
            // 开始时间在10分钟内的活动，执行预热
            if (activity.getStartTime().isAfter(now) && !activity.getStartTime().isAfter(warmupDeadline))
                warmup(activity);
        }

        // 进行中的活动也做一次补热，防止缓存过期
        List<SecondKillActivityDTO> active = activityDubboService.listByStatus(1);
        for (SecondKillActivityDTO activity : active)
            warmup(activity);
    }

    private void warmup(SecondKillActivityDTO activity) {
        String activityNo = activity.getActivityNo();
        try {
            warmupActivityInfo(activity);
            warmupProductList(activityNo);
            warmupSkuStock(activityNo);
            StructuredLog.info(log)
                    .message("缓存预热完成")
                    .put("activityNo", activityNo)
                    .put("activityName", activity.getActivityName())
                    .log();
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("缓存预热失败")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
        }
    }

    private void warmupActivityInfo(SecondKillActivityDTO activity) throws Exception {
        String key = RedisKeyConstants.ACTIVITY_INFO + activity.getActivityNo();
        String json = objectMapper.writeValueAsString(activity);
        redisService.set(key, json, ACTIVITY_CACHE_TTL, TimeUnit.SECONDS);
    }

    private void warmupProductList(String activityNo) throws Exception {
        List<SecondKillActivityProductSkuDTO> products = productDubboService.listActivityProductSkus(activityNo);
        if (products == null || products.isEmpty())
            return;

        String key = RedisKeyConstants.ACTIVITY_PRODUCT_LIST + activityNo;
        String json = objectMapper.writeValueAsString(products);
        redisService.set(key, json, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
    }

    private void warmupSkuStock(String activityNo) {
        List<SecondKillActivityProductSkuDTO> skuList = productDubboService.listActivityProductSkus(activityNo);
        if (skuList == null)
            return;

        for (SecondKillActivityProductSkuDTO sku : skuList) {
            String stockKey = RedisKeyConstants.skuStockKey(
                    Long.valueOf(activityNo), sku.getProductId());
            // 只在key不存在时写入，避免覆盖运行中的库存值
            if (!redisService.hasKey(stockKey))
                redisService.set(stockKey, String.valueOf(sku.getActivityStock()), STOCK_CACHE_TTL, TimeUnit.SECONDS);
        }
    }
}
