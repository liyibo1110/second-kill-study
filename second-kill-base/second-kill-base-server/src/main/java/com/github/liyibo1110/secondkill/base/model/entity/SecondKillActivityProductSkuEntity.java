package com.github.liyibo1110.secondkill.base.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 14:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sk_activity_product_sku")
public class SecondKillActivityProductSkuEntity extends BaseEntity {

    private String activityNo;

    private Long productId;

    private String skuNo;

    private Integer activityStock;

    private Integer discountType;

    private BigDecimal discountPercent;

    private BigDecimal discountPrice;
}
