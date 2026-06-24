package com.github.liyibo1110.secondkill.support.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-06-23 17:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateCardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cardNo;

    private Long userId;

    private String orderNo;
}
