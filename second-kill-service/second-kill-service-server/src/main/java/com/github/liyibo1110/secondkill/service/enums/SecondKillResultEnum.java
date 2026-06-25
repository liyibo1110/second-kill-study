package com.github.liyibo1110.secondkill.service.enums;

import lombok.Getter;

/**
 * 秒杀参与结果。
 * @author liyibo
 * @date 2026-06-24 14:10
 */
@Getter
public enum SecondKillResultEnum {

    SUCCESS("0", "抢购成功，请等待排队结果"),
    FAIL("1", "当前请求人数较多，请稍后重试"),
    PURCHASE_LIMIT("2", "当前商品限购"),
    WAIT_PAY("3", "已抢购成功，快去支付吧"),
    QUEUING("4", "已在排队中，请稍等"),
    STOCK_EMPTY("6", "手慢了，商品已售罄"),
    ACTIVITY_NOT_OPEN("7", "活动尚未开始或已结束"),
    RISK_USER("8", "请求异常，请稍后重试"),

    POLL_CONTINUE("1", "继续轮询"),
    POLL_STOP("0", "停止轮询"),
    POLL_SUCCESS("2", "抢购成功，可以跳转支付");

    private final String code;
    private final String message;

    SecondKillResultEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
