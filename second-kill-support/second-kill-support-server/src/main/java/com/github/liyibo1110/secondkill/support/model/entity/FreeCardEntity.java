package com.github.liyibo1110.secondkill.support.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_free_card")
public class FreeCardEntity extends BaseEntity {

    private String cardNo;

    private String cardName;

    private BigDecimal faceValue;

    private Long userId;

    private String orderNo;

    private Integer status;

    /**
     * 有效天数，从激活之日起算
     */
    private Integer validDays;

    private LocalDateTime activatedTime;

    private LocalDateTime expireTime;
}
