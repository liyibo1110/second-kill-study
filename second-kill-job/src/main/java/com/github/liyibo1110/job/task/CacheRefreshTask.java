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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存刷新任务。
 * 定时刷新进行中活动的Redis缓存，保证Caffeine本地缓存通过refreshAfterWrite从Redis加载到的数据是新鲜的。
 * 与second-kill-service中的CacheRefreshService互为补充：CacheRefreshService是进程内的@Scheduled，本任务是XXL-Job集中调度。
 * 建议执行频率：每分钟一次。
 * @author liyibo
 * @date 2026-06-29 10:01
 */
@Slf4j
@Component
public class CacheRefreshTask {

    private static final long ACTIVITY_CACHE_TTL = 3600;
    private static final long PRODUCT_CACHE_TTL = 1800;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public CacheRefreshTask(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @XxlJob("cacheRefresh")
    public void execute() {
        // 进行中的活动
        List<SecondKillActivityDTO> activeList = activityDubboService.listByStatus(1);
        if (activeList == null || activeList.isEmpty())
            return;

        for (SecondKillActivityDTO activity : activeList) {
            try {
                refreshActivityInfo(activity);
                refreshProductList(activity.getActivityNo());
            } catch (Exception e) {
                StructuredLog.error(log)
                        .message("缓存刷新失败")
                        .put("activityNo", activity.getActivityNo())
                        .exception(e)
                        .log();
            }
        }
    }

    private void refreshActivityInfo(SecondKillActivityDTO activity) throws Exception {
        String key = RedisKeyConstants.ACTIVITY_INFO + activity.getActivityNo();
        String json = objectMapper.writeValueAsString(activity);
        redisService.set(key, json, ACTIVITY_CACHE_TTL, TimeUnit.SECONDS);
    }

    private void refreshProductList(String activityNo) throws Exception {
        List<SecondKillActivityProductSkuDTO> products = productDubboService.listActivityProductSkus(activityNo);
        if (products == null || products.isEmpty())
            return;

        String key = RedisKeyConstants.ACTIVITY_PRODUCT_LIST + activityNo;
        String json = objectMapper.writeValueAsString(products);
        redisService.set(key, json, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
    }
}
