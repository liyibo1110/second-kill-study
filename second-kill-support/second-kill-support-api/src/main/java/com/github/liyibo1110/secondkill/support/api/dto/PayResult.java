package com.github.liyibo1110.secondkill.support.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-06-23 17:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private String payChannel;

    private String prepayId;

    private String nonceStr;

    private String timeStamp;

    private String sign;
}
