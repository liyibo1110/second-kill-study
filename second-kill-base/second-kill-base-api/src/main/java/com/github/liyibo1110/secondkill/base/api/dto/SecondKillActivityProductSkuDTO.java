package com.github.liyibo1110.secondkill.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 15:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillActivityProductSkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityNo;

    private Long productId;

    private String skuNo;

    private Integer activityStock;

    private Integer discountType;

    private BigDecimal discountPercent;

    private BigDecimal discountPrice;
}
