package com.github.liyibo1110.secondkill.base.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author liyibo
 * @date 2026-06-23 14:38
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sk_activity")
public class SecondKillActivityEntity extends BaseEntity {

    private String activityNo;

    private String activityName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer effectiveType;

    private String effectiveDays;

    private LocalTime effectiveStart;

    private LocalTime effectiveEnd;

    private Integer activityStatus;

    private Integer purchaseLimit;

    private String remark;
}
