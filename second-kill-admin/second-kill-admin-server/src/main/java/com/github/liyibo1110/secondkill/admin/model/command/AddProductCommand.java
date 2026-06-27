package com.github.liyibo1110.secondkill.admin.model.command;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建产品接口的请求实体。
 * @author liyibo
 * @date 2026-06-26 10:34
 */
@Data
public class AddProductCommand {

    @NotBlank(message = "活动编号不能为空")
    private String activityNo;

    @NotBlank(message = "商品编号不能为空")
    private String skuNo;

    @NotNull(message = "活动库存不能为空")
    private Integer activityStock;

    private Integer discountType;

    private BigDecimal discountPrice;

    private BigDecimal discountPercent;
}
