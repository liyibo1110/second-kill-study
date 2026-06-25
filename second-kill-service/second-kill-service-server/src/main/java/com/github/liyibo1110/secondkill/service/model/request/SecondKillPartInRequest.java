package com.github.liyibo1110.secondkill.service.model.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀下单请求参数。
 * @author liyibo
 * @date 2026-06-24 10:58
 */
@Data
public class SecondKillPartInRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 活动编号 */
    private String activityNo;

    /** SKU编号 */
    private String skuId;

    /** 购买数量 */
    private Integer quantity;

    /** 商品金额 */
    private BigDecimal totalFee;

    /** 机审校验值（前端计算后带回） */
    private String random;
}
