package com.github.liyibo1110.secondkill.base.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 14:40
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sk_order")
public class SecondKillOrderEntity extends BaseEntity {

    private String orderNo;

    private Long userId;

    private String activityNo;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer orderStatus;

    private LocalDateTime paidTime;

    private LocalDateTime closedTime;

    private String transactionNo;

    private String traceId;

    private String remark;
}
