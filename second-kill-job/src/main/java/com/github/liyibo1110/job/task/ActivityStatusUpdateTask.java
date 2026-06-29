package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动状态自动变更任务，定时扫描活动表，根据startTime和endTime自动切换活动状态。
 * 状态值：0=未开始，1=进行中，2=已结束。
 * 建议执行频率：每分钟一次。
 * @author liyibo
 * @date 2026-06-29 10:26
 */
@Slf4j
@Component
public class ActivityStatusUpdateTask {

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @XxlJob("activityStatusUpdate")
    public void execute() {
        LocalDateTime now = LocalDateTime.now();
        // 未开始 → 进行中：startTime <= now 且 endTime > now
        activateUpcomingActivities(now);
        // 进行中 → 已结束：endTime <= now
        closeExpiredActivities(now);
    }

    private void activateUpcomingActivities(LocalDateTime now) {
        List<SecondKillActivityDTO> upcoming = activityDubboService.listByStatus(0);
        for (SecondKillActivityDTO activity : upcoming) {
            if (activity.getStartTime() != null && !activity.getStartTime().isAfter(now)) {
                if (activity.getEndTime() != null && activity.getEndTime().isAfter(now)) {
                    // 修改成已开始
                    activityDubboService.updateActivityStatus(activity.getActivityNo(), 1);
                    StructuredLog.info(log)
                            .message("活动状态变更为进行中")
                            .put("activityNo", activity.getActivityNo())
                            .put("activityName", activity.getActivityName())
                            .log();
                } else {
                    // startTime已过且endTime也已过，直接标记为已结束
                    activityDubboService.updateActivityStatus(activity.getActivityNo(), 2);
                    StructuredLog.info(log)
                            .message("活动未开始即已过期，直接标记为已结束")
                            .put("activityNo", activity.getActivityNo())
                            .log();
                }
            }
        }
    }

    private void closeExpiredActivities(LocalDateTime now) {
        List<SecondKillActivityDTO> active = activityDubboService.listByStatus(1);
        for (SecondKillActivityDTO activity : active) {
            if (activity.getEndTime() != null && !activity.getEndTime().isAfter(now)) {
                activityDubboService.updateActivityStatus(activity.getActivityNo(), 2);
                StructuredLog.info(log)
                        .message("活动状态变更为已结束")
                        .put("activityNo", activity.getActivityNo())
                        .put("activityName", activity.getActivityName())
                        .log();
            }
        }
    }
}
