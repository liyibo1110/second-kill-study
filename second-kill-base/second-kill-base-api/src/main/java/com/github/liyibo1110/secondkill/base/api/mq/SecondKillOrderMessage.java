package com.github.liyibo1110.secondkill.base.api.mq;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * MQ消息体，由second-kill-service服务发送，由second-kill-processor服务来消费。
 * @author liyibo
 * @date 2026-06-23 15:49
 */
@Data
public class SecondKillOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 活动编号 */
    private String activityNo;

    /** SKU编号 */
    private String skuId;

    /** 购买数量 */
    private Integer quantity;

    /** 商品金额 */
    private BigDecimal totalFee;

    /** 排队令牌 */
    private String token;

    /** 链路追踪ID */
    private String traceId;

    /** 请求时间戳 */
    private long requestTime;
}
