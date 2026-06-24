package com.github.liyibo1110.secondkill.support.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-06-23 17:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayQueryResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private Integer payStatus;

    private String transactionNo;

    private LocalDateTime paidTime;
}
