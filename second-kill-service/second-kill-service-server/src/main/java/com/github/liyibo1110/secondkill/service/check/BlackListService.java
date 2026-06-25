package com.github.liyibo1110.secondkill.service.check;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.config.SecondKillProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 黑名单服务。
 *
 * 检测活动开始前就发起抢购请求的用户，自动标记为风险用户。
 * 正常用户在活动倒计时结束后才会点击抢购，而脚本通常会在活动开始前就持续发送请求。
 * 利用这个行为差异，在活动开始前300秒到10秒的时间窗口内，将发起请求的用户标记到Redis黑名单中。
 * @author liyibo
 * @date 2026-06-24 13:19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlackListService {

    private final RedisService redisService;
    private final SecondKillProperties secondKillProperties;

    /**
     * 检测并标记黑名单用户。
     * 在活动未开始时调用，如果当前时间处于标记窗口内，将用户加入黑名单。
     */
    public void markIfSuspicious(SecondKillActivityDTO activity, Long userId) {
        SecondKillProperties.BlackList config = secondKillProperties.getBlackList();
        if (!config.isEnabled())
            return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime activityStart = activity.getStartTime();

        // 标记窗口：活动开始前markStartSeconds秒 到 活动开始前markEndSeconds秒
        LocalDateTime windowStart = activityStart.minusSeconds(config.getMarkStartSeconds());
        LocalDateTime windowEnd = activityStart.minusSeconds(config.getMarkEndSeconds());

        if (now.isAfter(windowStart) && now.isBefore(windowEnd)) {
            String redisKey = RedisKeyConstants.riskUserKey(userId);
            redisService.set(redisKey, "blacklist", config.getExpireSeconds(), TimeUnit.SECONDS);

            StructuredLog.info(log)
                    .message("用户加入小黑屋")
                    .put("userId", userId)
                    .put("activityNo", activity.getActivityNo())
                    .put("activityStart", activityStart.toString())
                    .put("expireSeconds", config.getExpireSeconds())
                    .log();
        }
    }

    /**
     * 检查用户是否在黑名单中。
     */
    public boolean isRiskUser(Long userId) {
        String redisKey = RedisKeyConstants.riskUserKey(userId);
        Boolean exists = redisService.hasKey(redisKey);
        return exists != null && exists;
    }
}
