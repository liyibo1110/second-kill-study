package com.github.liyibo1110.secondkill.base.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 14:41
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sk_order_item")
public class SecondKillOrderItemEntity extends BaseEntity {

    private String orderNo;

    private String activityNo;

    private String skuNo;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;
}
