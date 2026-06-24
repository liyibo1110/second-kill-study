package com.github.liyibo1110.secondkill.support.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class OrderEntity extends BaseEntity {

    private String orderNo;

    private Long userId;

    private String orderSource;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer orderStatus;

    private LocalDateTime paidTime;

    private LocalDateTime completedTime;

    private String transactionNo;

    private String remark;
}
