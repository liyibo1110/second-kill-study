package com.github.liyibo1110.secondkill.processor.handler;

import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.OrderStatusEnum;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderItemDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.processor.service.StockDeductService;
import com.github.liyibo1110.secondkill.support.api.PaymentDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付超时处理器，延迟消息到期后，检查订单是否已支付。
 * 未支付则依次执行：关闭秒杀域订单 → 释放库存 → 关闭支付网关订单。
 *
 * @author liyibo
 * @date 2026-06-25 12:04
 */
@Slf4j
@Component
public class PaymentTimeoutHandler {

    private final StockDeductService stockDeductService;

    @DubboReference
    private SecondKillOrderDubboService orderDubboService;

    @DubboReference
    private PaymentDubboService paymentDubboService;

    public PaymentTimeoutHandler(StockDeductService stockDeductService) {
        this.stockDeductService = stockDeductService;
    }

    /**
     * 处理支付超时
     */
    public void handle(String orderNo) {
        if (orderNo == null || orderNo.isEmpty())
            return;

        SecondKillOrderDTO order = orderDubboService.getByOrderNo(orderNo);
        if (order == null) {
            StructuredLog.warn(log)
                    .message("订单不存在，跳过超时处理")
                    .put("orderNo", orderNo)
                    .log();
            return;
        }

        // 只关闭待支付状态的订单
        if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatusEnum.WAIT_PAY.getCode())
            return;

        // 关闭秒杀域订单（状态流转：待支付→已关闭）
        orderDubboService.closeOrder(orderNo);

        // 释放库存和限购计数
        releaseStock(order);

        // 关闭支付网关的预支付订单
        closePaymentOrder(orderNo);

        StructuredLog.info(log)
                .message("支付超时处理完成")
                .put("orderNo", orderNo)
                .put("userId", order.getUserId())
                .put("activityNo", order.getActivityNo())
                .log();
    }

    /**
     * 释放库存：查订单项，逐项释放库存和限购计数
     */
    private void releaseStock(SecondKillOrderDTO order) {
        try {
            List<SecondKillOrderItemDTO> items = orderDubboService.listOrderItems(order.getOrderNo());
            if (items == null || items.isEmpty())
                return;

            for (SecondKillOrderItemDTO item : items) {
                stockDeductService.releaseStock(
                        item.getActivityNo(),
                        item.getSkuNo(),
                        order.getUserId(),
                        item.getQuantity()
                );
            }
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("超时库存释放异常")
                    .put("orderNo", order.getOrderNo())
                    .exception(e)
                    .log();
        }
    }

    /**
     * 关闭支付网关的预支付订单
     */
    private void closePaymentOrder(String orderNo) {
        try {
            paymentDubboService.closePayment(orderNo);
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("关闭支付网关订单异常")
                    .put("orderNo", orderNo)
                    .exception(e)
                    .log();
        }
    }
}
