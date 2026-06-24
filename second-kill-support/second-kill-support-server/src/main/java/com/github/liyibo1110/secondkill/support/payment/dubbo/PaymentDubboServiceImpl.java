package com.github.liyibo1110.secondkill.support.payment.dubbo;

import com.github.liyibo1110.secondkill.support.api.PaymentDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.PayQueryResult;
import com.github.liyibo1110.secondkill.support.api.dto.PayResult;
import com.github.liyibo1110.secondkill.support.api.request.CreatePayRequest;
import com.github.liyibo1110.secondkill.support.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author liyibo
 * @date 2026-06-23 17:53
 */
@DubboService
@RequiredArgsConstructor
public class PaymentDubboServiceImpl implements PaymentDubboService {

    private final PaymentService paymentService;

    @Override
    public PayResult createPayment(CreatePayRequest request) {
        return paymentService.createPayment(request);
    }

    @Override
    public PayQueryResult queryPayment(String orderNo) {
        return paymentService.queryPayment(orderNo);
    }

    @Override
    public void closePayment(String orderNo) {
        paymentService.closePayment(orderNo);
    }
}
