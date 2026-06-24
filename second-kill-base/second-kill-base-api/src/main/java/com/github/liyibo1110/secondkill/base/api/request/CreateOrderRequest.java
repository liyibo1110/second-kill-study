package com.github.liyibo1110.secondkill.base.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 15:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String activityNo;

    private String skuNo;

    private Integer quantity;

    private BigDecimal payAmount;

    private String traceId;
}
