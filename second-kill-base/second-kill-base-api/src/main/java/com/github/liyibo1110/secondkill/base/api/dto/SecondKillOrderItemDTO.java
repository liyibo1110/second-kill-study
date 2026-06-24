package com.github.liyibo1110.secondkill.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 15:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillOrderItemDTO {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private String activityNo;

    private String skuNo;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;
}
