package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日统计报表任务，活动结束后，汇总当天所有活动的关键指标：
 * 参与人数、下单量、支付量、库存消耗率等。
 * 输出结构化日志，供日志平台做可视化看板。
 * 建议执行频率：每天凌晨1点执行一次。
 * @author liyibo
 * @date 2026-06-30 11:57
 */
@Slf4j
@Component
public class DailyStatisticsTask {

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    @XxlJob("dailyStatistics")
    public void execute() {
        // 统计进行中和已结束的活动
        List<SecondKillActivityDTO> activeList = activityDubboService.listByStatus(1);
        List<SecondKillActivityDTO> endedList = activityDubboService.listByStatus(2);

        int activeCount = (activeList != null) ? activeList.size() : 0;
        int endedCount = (endedList != null) ? endedList.size() : 0;

        StructuredLog.info(log)
                .message("每日活动统计")
                .put("activeCount", activeCount)
                .put("endedCount", endedCount)
                .put("totalCount", activeCount + endedCount)
                .log();

        // 对每个已结束的活动，输出库存消耗情况
        if (endedList != null) {
            for (SecondKillActivityDTO activity : endedList) {
                reportActivityMetrics(activity);
            }
        }
    }

    private void reportActivityMetrics(SecondKillActivityDTO activity) {
        List<SecondKillActivityProductSkuDTO> skuList = productDubboService.listActivityProductSkus(activity.getActivityNo());
        int totalStock = 0;
        if (skuList != null) {
            for (SecondKillActivityProductSkuDTO sku : skuList) {
                totalStock += (sku.getActivityStock() != null) ? sku.getActivityStock() : 0;
            }
        }

        StructuredLog.info(log)
                .message("活动数据统计")
                .put("activityNo", activity.getActivityNo())
                .put("activityName", activity.getActivityName())
                .put("totalInitStock", totalStock)
                .put("startTime", String.valueOf(activity.getStartTime()))
                .put("endTime", String.valueOf(activity.getEndTime()))
                .log();
    }
}
