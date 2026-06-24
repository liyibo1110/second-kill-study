package com.github.liyibo1110.secondkill.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 15:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;
}
