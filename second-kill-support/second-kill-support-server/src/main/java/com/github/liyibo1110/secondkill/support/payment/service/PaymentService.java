package com.github.liyibo1110.secondkill.support.payment.service;

import com.github.liyibo1110.secondkill.support.api.dto.PayQueryResult;
import com.github.liyibo1110.secondkill.support.api.dto.PayResult;
import com.github.liyibo1110.secondkill.support.api.request.CreatePayRequest;

/**
 * @author liyibo
 * @date 2026-06-23 17:45
 */
public interface PaymentService {

    PayResult createPayment(CreatePayRequest request);

    PayQueryResult queryPayment(String orderNo);

    void closePayment(String orderNo);
}
