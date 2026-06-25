package com.github.liyibo1110.secondkill.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存定时刷新服务。
 *
 * 定期从数据库重新加载活动和商品数据写入Redis，以保证Caffeine的refreshAfterWrite能从Redis拿到最新值。
 * @author liyibo
 * @date 2026-06-24 11:44
 */
@Slf4j
@Service
public class CacheRefreshService {

    private static final long ACTIVITY_CACHE_TTL = 3600;
    private static final long PRODUCT_CACHE_TTL = 1800;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public CacheRefreshService(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    /**
     * 每60秒刷新进行中活动的Redis缓存。
     *
     * Caffeine的reload方法只从Redis读取，不回源Dubbo。
     * 如果Redis中的数据过时，Caffeine刷新拿到的也是旧值。
     * 这个定时任务保证Redis中的数据持续更新，
     * 整条链路：DB → (定时任务) → Redis → (refreshAfterWrite) → Caffeine
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void refreshActiveActivities() {
        try {
            List<SecondKillActivityDTO> activities = activityDubboService.listActiveActivities();
            if (activities == null || activities.isEmpty())
                return;

            for (SecondKillActivityDTO activity : activities) {
                refreshActivityInfo(activity);
                refreshProductList(activity.getActivityNo());
            }

            StructuredLog.info(log)
                    .message("活动缓存定时刷新完成")
                    .put("activityCount", activities.size())
                    .log();
        } catch (Exception e) {
            // 刷新失败不影响业务，Caffeine中有旧值兜底
            StructuredLog.warn(log)
                    .message("活动缓存定时刷新异常")
                    .exception(e)
                    .log();
        }
    }

    private void refreshActivityInfo(SecondKillActivityDTO activity) {
        try {
            String redisKey = RedisKeyConstants.ACTIVITY_INFO + activity.getActivityNo();
            String json = objectMapper.writeValueAsString(activity);
            redisService.set(redisKey, json, ACTIVITY_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("活动信息刷新失败")
                    .put("activityNo", activity.getActivityNo())
                    .exception(e)
                    .log();
        }
    }

    private void refreshProductList(String activityNo) {
        try {
            List<SecondKillActivityProductDTO> products = productDubboService.listActivityProducts(activityNo);
            if (products != null && !products.isEmpty()) {
                String redisKey = RedisKeyConstants.ACTIVITY_PRODUCT_LIST + activityNo;
                String json = objectMapper.writeValueAsString(products);
                redisService.set(redisKey, json, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("商品列表刷新失败")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
        }
    }
}
