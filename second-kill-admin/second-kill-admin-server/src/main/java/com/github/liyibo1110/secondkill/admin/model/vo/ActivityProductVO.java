package com.github.liyibo1110.secondkill.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 活动相关产品接口的响应实体。
 * @author liyibo
 * @date 2026-06-26 10:38
 */
@Data
@Builder
public class ActivityProductVO {

    private String skuNo;

    private String productName;

    private Integer activityStock;

    private Integer discountType;

    private BigDecimal discountPrice;

    private BigDecimal discountPercent;
}
