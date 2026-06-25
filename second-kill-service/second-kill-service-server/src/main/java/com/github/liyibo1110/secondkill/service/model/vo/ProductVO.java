package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品信息响应。
 * @author liyibo
 * @date 2026-06-24 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productName;

    private String productImage;

    private BigDecimal originalPrice;

    private Integer discountType;

    private BigDecimal discountPrice;

    private Integer sortOrder;
}
