package com.github.liyibo1110.secondkill.support.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-06-23 17:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordRiskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String actionType;

    private Integer riskLevel;

    private String requestIp;

    private String requestInfo;
}
