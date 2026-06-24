package com.github.liyibo1110.secondkill.support.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-06-23 17:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String orderNo;

    private String cardName;

    private BigDecimal faceValue;

    /**
     * 有效天数，从激活之日起算
     */
    private Integer validDays;
}
