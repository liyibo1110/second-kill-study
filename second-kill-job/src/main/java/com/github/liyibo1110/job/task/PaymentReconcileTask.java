package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.support.api.PaymentDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.PayQueryResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付对账任务，比对秒杀域订单的支付状态和支付网关的实际状态，处理两类异常情况：
 * 1. 订单显示已支付但支付网关无记录（虚假支付）
 * 2. 支付网关已扣款但订单仍为待支付（回调丢失）
 * 回调丢失时自动触发补偿，更新订单状态。
 * 建议执行频率：每30分钟一次。
 * @author liyibo
 * @date 2026-06-30 11:36
 */
@Slf4j
@Component
public class PaymentReconcileTask {

    @DubboReference
    private SecondKillOrderDubboService secondKillOrderDubboService;

    @DubboReference
    private PaymentDubboService paymentDubboService;

    @XxlJob("paymentReconcile")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        if (param != null && !param.isBlank()) {
            reconcileActivity(param.trim());
        } else {
            StructuredLog.info(log)
                    .message("支付对账任务未指定活动编号，跳过执行")
                    .log();
        }
    }

    private void reconcileActivity(String activityNo) {
        List<SecondKillOrderDTO> orders = secondKillOrderDubboService.listByActivityNo(activityNo);
        if (orders == null || orders.isEmpty())
            return;

        int totalChecked = 0;
        int mismatchCount = 0;

        for (SecondKillOrderDTO order : orders) {
            if (order.getOrderStatus() == null)
                continue;

            int status = order.getOrderStatus();
            // 只检查待支付(0)和已支付(1)状态的订单
            if (status != 0 && status != 1)
                continue;

            totalChecked++;

            try {
                PayQueryResult payResult = paymentDubboService.queryPayment(order.getOrderNo());
                if (payResult != null && payResult.getPayStatus() != null) {
                    // payStatus=1 表示支付网关已收款
                    boolean gatewayPaid = payResult.getPayStatus() == 1;
                    if (gatewayPaid && status == 0) {
                        // 支付网关已扣款但订单仍待支付，补偿更新
                        secondKillOrderDubboService.updatePaidInfo(order.getOrderNo(), payResult.getTransactionNo());
                        StructuredLog.warn(log)
                                .message("支付回调丢失，已补偿更新")
                                .put("orderNo", order.getOrderNo())
                                .put("transactionNo", payResult.getTransactionNo())
                                .log();
                        mismatchCount++;
                    }
                }
            } catch (Exception e) {
                StructuredLog.error(log)
                        .message("支付状态查询异常")
                        .put("orderNo", order.getOrderNo())
                        .exception(e)
                        .log();
            }
        }

        StructuredLog.info(log)
                .message("支付对账完成")
                .put("activityNo", activityNo)
                .put("totalChecked", totalChecked)
                .put("mismatchCount", mismatchCount)
                .log();
    }
}
