package com.github.liyibo1110.secondkill.support.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeCardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cardNo;

    private String cardName;

    private BigDecimal faceValue;

    private Long userId;

    private String orderNo;

    private Integer status;

    private Integer validDays;

    private LocalDateTime activatedTime;

    private LocalDateTime expireTime;
}
