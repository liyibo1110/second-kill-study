package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.support.api.OrderSyncDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.OrderDTO;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 订单同步对账任务。
 * 从Redis的ORDER_SYNC_LIST中取出待对账的订单号，逐条比对秒杀域订单和主域订单的状态。
 * 发现不一致时记录日志并触发补偿同步。
 * 建议执行频率：每5分钟一次。
 * @author liyibo
 * @date 2026-06-30 11:29
 */
@Slf4j
@Component
public class OrderSyncCheckTask {

    private static final int BATCH_SIZE = 100;

    private final RedisService redisService;

    @DubboReference
    private SecondKillOrderDubboService secondKillOrderDubboService;

    @DubboReference
    private OrderSyncDubboService orderSyncDubboService;

    public OrderSyncCheckTask(RedisService redisService) {
        this.redisService = redisService;
    }

    @XxlJob("orderSyncCheck")
    public void execute() {
        int processedCount = 0;
        int mismatchCount = 0;

        // 从Redis List中批量取出待对账的订单号
        for (int i = 0; i < BATCH_SIZE; i++) {
            String orderNo = redisService.lPop(RedisKeyConstants.ORDER_SYNC_LIST);
            if (orderNo == null || orderNo.isBlank())
                break;

            processedCount++;
            boolean matched = checkOrderConsistency(orderNo);
            if (!matched)
                mismatchCount++;
        }

        if (processedCount > 0) {
            StructuredLog.info(log)
                    .message("订单对账完成")
                    .put("processedCount", processedCount)
                    .put("mismatchCount", mismatchCount)
                    .log();
        }
    }

    private boolean checkOrderConsistency(String orderNo) {
        SecondKillOrderDTO secondKillOrder = secondKillOrderDubboService.getByOrderNo(orderNo);
        if (secondKillOrder == null) {
            StructuredLog.error(log)
                    .message("秒杀域订单不存在")
                    .put("orderNo", orderNo)
                    .log();
            return false;
        }

        OrderDTO mainOrder = orderSyncDubboService.getByOrderNo(orderNo);
        if (mainOrder == null) {
            // 主域订单不存在，标记为需要补偿同步
            StructuredLog.warn(log)
                    .message("主域订单不存在，需补偿同步")
                    .put("orderNo", orderNo)
                    .log();
            return false;
        }

        return true;
    }
}
