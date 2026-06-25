package com.github.liyibo1110.secondkill.service.check;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.service.config.SecondKillProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户级限流服务，基于Redisson的RRateLimiter实现每用户独立的令牌桶限流。
 * 与网关层的全局QPS限流不同，这里是按单个用户维度控制请求频率。
 * 默认配置：每个用户10秒内最多10次请求，限流器5分钟后自动过期。
 *
 * @author liyibo
 * @date 2026-06-24 14:06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRateLimitService {

    private final RedissonClient redissonClient;
    private final SecondKillProperties secondKillProperties;

    /**
     * 检查用户是否超过限流阈值
     *
     * @param userId 用户ID
     * @return true=允许通过，false=超过频率限制
     */
    public boolean tryAcquire(Long userId) {
        SecondKillProperties.UserRateLimit config = secondKillProperties.getUserRateLimit();
        if (!config.isEnabled())
            return true;

        String rateLimiterKey = RedisKeyConstants.userRateLimitKey(userId);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

        if (!rateLimiter.isExists()) {
            // 首次访问，创建限流器
            rateLimiter.trySetRate(
                    RateType.OVERALL,
                    config.getRate(),
                    config.getInterval(),
                    RateIntervalUnit.SECONDS
            );
            rateLimiter.expireAsync(config.getExpireMinutes(), TimeUnit.MINUTES);
        }

        boolean acquired = rateLimiter.tryAcquire(0, TimeUnit.MILLISECONDS);
        if (!acquired) {
            StructuredLog.warn(log)
                    .message("用户级限流触发")
                    .put("userId", userId)
                    .put("rate", config.getRate())
                    .put("interval", config.getInterval())
                    .log();
        }
        return acquired;
    }
}
