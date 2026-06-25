package com.github.liyibo1110.secondkill.service.controller;

import com.github.liyibo1110.secondkill.common.result.Result;
import com.github.liyibo1110.secondkill.service.payment.PayService;
import com.github.liyibo1110.secondkill.support.api.dto.PayResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付接口。
 * @author liyibo
 * @date 2026-06-24 15:02
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 获取预支付信息，前端拿到prepayId等参数后调起支付SDK。
     */
    @PostMapping("/prepay")
    public Result<PayResult> prepay(@RequestParam String orderNo, @RequestParam String payChannel) {
        PayResult result = payService.prepay(orderNo, payChannel);
        return Result.success(result);
    }

    /**
     * 支付回调（支付网关调用）。
     * 生产环境中微信和支付宝的回调地址分开，各自有签名验证逻辑。
     * 教学项目合并为一个接口，通过channel参数区分。
     */
    @PostMapping("/notify/{channel}")
    public String payNotify(@PathVariable String channel, @RequestBody Map<String, String> params) {
        payService.handleCallback(channel, params);
        return "SUCCESS";
    }
}
