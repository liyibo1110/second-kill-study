package com.github.liyibo1110.secondkill.support.payment.service.impl;

import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.support.api.dto.PayQueryResult;
import com.github.liyibo1110.secondkill.support.api.dto.PayResult;
import com.github.liyibo1110.secondkill.support.api.request.CreatePayRequest;
import com.github.liyibo1110.secondkill.support.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author liyibo
 * @date 2026-06-23 17:49
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PayResult createPayment(CreatePayRequest request) {
        // 模拟调用支付网关的预下单接口
        String prepayId = "prepay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);

        StructuredLog.info(log)
                .message("预支付订单创建成功")
                .put("orderNo", request.getOrderNo())
                .put("payChannel", request.getPayChannel())
                .put("payAmount", request.getPayAmount())
                .put("prepayId", prepayId)
                .log();

        return PayResult.builder()
                .orderNo(request.getOrderNo())
                .payChannel(request.getPayChannel())
                .prepayId(prepayId)
                .nonceStr(nonceStr)
                .timeStamp(timeStamp)
                .sign("mock_sign_" + nonceStr)
                .build();
    }

    @Override
    public PayQueryResult queryPayment(String orderNo) {
        // 模拟查询支付网关的订单状态
        // 生产环境中调用微信/支付宝的查单接口
        StructuredLog.info(log)
                .message("查询支付状态")
                .put("orderNo", orderNo)
                .log();

        return PayQueryResult.builder()
                .orderNo(orderNo)
                .payStatus(0)
                .build();
    }

    @Override
    public void closePayment(String orderNo) {
        // 模拟关闭支付网关的订单
        // 生产环境中调用微信/支付宝的关单接口
        StructuredLog.info(log)
                .message("支付订单已关闭")
                .put("orderNo", orderNo)
                .log();
    }
}
