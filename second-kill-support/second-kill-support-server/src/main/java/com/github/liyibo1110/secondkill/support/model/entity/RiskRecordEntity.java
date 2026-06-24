package com.github.liyibo1110.secondkill.support.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liyibo
 * @date 2026-06-23 17:38
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_risk_record")
public class RiskRecordEntity extends BaseEntity {

    private Long userId;

    private String actionType;

    private Integer riskLevel;

    private String requestIp;

    private String requestInfo;
}
