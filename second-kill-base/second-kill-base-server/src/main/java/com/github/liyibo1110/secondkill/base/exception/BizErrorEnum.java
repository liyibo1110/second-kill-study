package com.github.liyibo1110.secondkill.base.exception;

import com.github.liyibo1110.secondkill.common.exception.IErrorCode;
import lombok.Getter;

/**
 * 业务错误码
 * @author liyibo
 * @date 2026-06-23 15:17
 */
@Getter
public enum BizErrorEnum implements IErrorCode {

    STOCK_NOT_ENOUGH("stock_not_enough", "库存不足"),
    ACTIVITY_NOT_STARTED("activity_not_started", "活动未开始"),
    ACTIVITY_ENDED("activity_ended", "活动已结束"),
    PURCHASE_LIMIT_EXCEEDED("purchase_limit_exceeded", "超出限购数量"),
    ORDER_STATUS_ERROR("order_status_error", "订单状态异常");

    private final String code;
    private final String message;

    BizErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
