package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 风险用户清理任务，风险用户标记存储在Redis中，带有过期时间。
 * 正常情况下Redis的TTL机制会自动淘汰，但存量大时惰性删除可能不够及时，占用额外内存。
 * 本任务通过scan扫描已过期但未被惰性删除的Key，主动清理。
 * 建议执行频率：每天凌晨3点执行一次。
 * @author liyibo
 * @date 2026-06-29 18:13
 */
@Slf4j
@Component
public class RiskUserCleanupTask {

    private final RedisService redisService;

    public RiskUserCleanupTask(RedisService redisService) {
        this.redisService = redisService;
    }

    @XxlJob("riskUserCleanup")
    public void execute() {
        String pattern = RedisKeyConstants.RISK_USER + "*";
        long deletedCount = redisService.deleteExpiredByPattern(pattern);
        StructuredLog.info(log)
                .message("风险用户过期Key清理完成")
                .put("deletedCount", deletedCount)
                .log();
    }
}
