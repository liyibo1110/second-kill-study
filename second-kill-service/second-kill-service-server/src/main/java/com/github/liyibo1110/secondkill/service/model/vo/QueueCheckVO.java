package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 排队结果轮询响应。
 * @author liyibo
 * @date 2026-06-24 11:04
 */
@Data
@Accessors(chain = true)
public class QueueCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 轮询状态：0=停止轮询（异常），1=继续轮询，2=成功（可跳转支付） */
    private String pollStatus;

    /** 秒杀订单号（成功时返回） */
    private String orderNo;
}
