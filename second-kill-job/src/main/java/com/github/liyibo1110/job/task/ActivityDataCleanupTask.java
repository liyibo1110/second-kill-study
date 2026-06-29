package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动数据清理任务。
 * 活动结束超过24小时后，清理Redis中的排队状态、限购计数、机审Token等临时数据，释放Redis内存。
 * StockReleaseTask的区别：StockReleaseTask清理库存Key，本任务清理用户维度的临时Key。
 * 建议执行频率：每天凌晨2点执行一次。
 * @author liyibo
 * @date 2026-06-29 10:18
 */
@Slf4j
@Component
public class ActivityDataCleanupTask {

    private final RedisService redisService;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    public ActivityDataCleanupTask(RedisService redisService) {
        this.redisService = redisService;
    }

    @XxlJob("activityDataCleanup")
    public void execute() {
        // 已结束的活动
        List<SecondKillActivityDTO> ended = activityDubboService.listByStatus(2);
        if (ended == null || ended.isEmpty())
            return;

        LocalDateTime cleanupThreshold = LocalDateTime.now().minusHours(24);
        int cleanedCount = 0;

        for (SecondKillActivityDTO activity : ended) {
            // 只清理结束超过24小时的活动
            if (activity.getEndTime() != null && activity.getEndTime().isBefore(cleanupThreshold)) {
                cleanActivityData(activity.getActivityNo());
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            StructuredLog.info(log)
                    .message("活动临时数据清理完成")
                    .put("cleanedCount", cleanedCount)
                    .log();
        }
    }

    private void cleanActivityData(String activityNo) {
        // 删除排队状态的前缀Key（通过scan模式匹配）
        String queuingPattern = RedisKeyConstants.ORDER_QUEUING + "*:" + activityNo;
        redisService.deleteByPattern(queuingPattern);

        // 删除订单结果缓存
        String orderQueueKey = RedisKeyConstants.ORDER_QUEUE + activityNo;
        redisService.delete(orderQueueKey);

        StructuredLog.info(log)
                .message("活动临时数据已清理")
                .put("activityNo", activityNo)
                .log();
    }
}
