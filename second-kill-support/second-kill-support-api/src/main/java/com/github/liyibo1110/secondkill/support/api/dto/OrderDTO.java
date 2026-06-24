package com.github.liyibo1110.secondkill.support.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;
}
