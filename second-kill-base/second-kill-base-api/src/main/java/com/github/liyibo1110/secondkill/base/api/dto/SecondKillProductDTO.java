package com.github.liyibo1110.secondkill.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 15:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityNo;

    private String productName;

    private String productImage;

    private String skuNo;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer totalStock;

    private Integer availableStock;

    private Integer limitQuantity;
}
