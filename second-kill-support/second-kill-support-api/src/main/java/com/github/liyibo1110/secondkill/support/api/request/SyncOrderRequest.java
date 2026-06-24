package com.github.liyibo1110.secondkill.support.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private Long userId;

    private String orderSource;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private LocalDateTime paidTime;

    private String transactionNo;
}
