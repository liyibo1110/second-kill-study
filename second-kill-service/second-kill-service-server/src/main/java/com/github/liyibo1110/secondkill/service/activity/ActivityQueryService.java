package com.github.liyibo1110.secondkill.service.activity;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.cache.TwoLevelCache;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 活动查询服务，通过两级缓存读取活动数据，回源走Dubbo调用second-kill-base。
 * @author liyibo
 * @date 2026-06-24 11:59
 */
@Service
public class ActivityQueryService {

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @Resource
    private RedisService redisService;

    @Resource
    private ObjectMapper objectMapper;

    private TwoLevelCache<SecondKillActivityDTO> activityCache;

    private TwoLevelCache<List<SecondKillActivityDTO>> activeActivitiesCache;

    @PostConstruct
    public void init() {
        // 活动详情缓存：本地1小时硬过期，30秒刷新间隔，Redis 1小时
        JavaType activityType = objectMapper.getTypeFactory().constructType(SecondKillActivityDTO.class);
        this.activityCache = new TwoLevelCache<>(
                200, 3600, 30,
                redisService, objectMapper,
                RedisKeyConstants.ACTIVITY_INFO, 3600, 60,
                activityType,
                activityDubboService::getByActivityNo);

        // 进行中活动列表缓存：本地5分钟硬过期，15秒刷新间隔，Redis 5分钟
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, SecondKillActivityDTO.class);
        this.activeActivitiesCache = new TwoLevelCache<>(
                10, 300, 15,
                redisService, objectMapper,
                "secondkill:activity:active:", 300, 60,
                listType,
                key -> activityDubboService.listActiveActivities());
    }

    /**
     * 查询活动详情（走两级缓存）
     */
    public SecondKillActivityDTO getActivity(String activityNo) {
        return activityCache.get(activityNo);
    }

    /**
     * 查询进行中的活动列表（走两级缓存）
     */
    public List<SecondKillActivityDTO> listActiveActivities() {
        List<SecondKillActivityDTO> list = activeActivitiesCache.get("all");
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 判断活动当前是否可参与
     * 条件：状态为进行中（1）且当前时间在活动时间范围内
     */
    public boolean isActivityOpen(SecondKillActivityDTO activity) {
        if (activity == null || activity.getActivityStatus() == null)
            return false;

        if (activity.getActivityStatus() != 1)
            return false;

        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(activity.getStartTime()) && !now.isAfter(activity.getEndTime());
    }
}
