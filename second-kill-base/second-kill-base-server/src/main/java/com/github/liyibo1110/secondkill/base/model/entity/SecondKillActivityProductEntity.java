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
@TableName("sk_activity_product")
public class SecondKillActivityProductEntity extends BaseEntity {

    private String activityNo;

    private String productName;

    private String productImage;

    private BigDecimal originalPrice;

    private Integer discountType;

    private BigDecimal discountPrice;

    private Integer sortOrder;
}
