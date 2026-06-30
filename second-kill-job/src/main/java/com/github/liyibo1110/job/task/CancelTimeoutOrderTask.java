package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 超时未支付订单补偿任务，RocketMQ延迟消息是支付超时的主路径，但消息有可能丢失或消费失败。
 * 这个任务作为补偿机制，扫描已结束活动中仍然处于待支付状态的订单，执行关闭操作。
 * 建议执行频率：每10分钟一次。
 * @author liyibo
 * @date 2026-06-29 18:15
 */
@Slf4j
@Component
public class CancelTimeoutOrderTask {

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillOrderDubboService secondKillOrderDubboService;

    @XxlJob("cancelTimeoutOrder")
    public void execute() {
        // 已结束的活动
        List<SecondKillActivityDTO> ended = activityDubboService.listByStatus(2);
        if (ended == null || ended.isEmpty())
            return;

        int closedCount = 0;
        for (SecondKillActivityDTO activity : ended)
            closedCount += closeUnpaidOrders(activity.getActivityNo());

        if (closedCount > 0) {
            StructuredLog.info(log)
                    .message("超时订单补偿关闭完成")
                    .put("closedCount", closedCount)
                    .log();
        }
    }

    private int closeUnpaidOrders(String activityNo) {
        // 通过活动编号查询待支付订单
        // 实际生产中这里应该有一个按活动+状态查询的Dubbo接口
        // 当前通过closeOrder逐个处理，closeOrder内部会判断订单状态
        int count = 0;
        try {
            List<SecondKillOrderDTO> orders = secondKillOrderDubboService.listByActivityNo(activityNo);
            if (orders == null)
                return 0;

            for (SecondKillOrderDTO order : orders) {
                // status=0 表示待支付
                if (order.getOrderStatus() != null && order.getOrderStatus() == 0) {
                    secondKillOrderDubboService.closeOrder(order.getOrderNo());
                    count++;
                    StructuredLog.info(log)
                            .message("补偿关闭超时订单")
                            .put("orderNo", order.getOrderNo())
                            .put("activityNo", activityNo)
                            .log();
                }
            }
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("超时订单补偿处理异常")
                    .put("activityNo", activityNo)
                    .exception(e)
                    .log();
        }
        return count;
    }
}
