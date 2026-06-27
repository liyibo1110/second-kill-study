package com.github.liyibo1110.secondkill.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动列表的响应实体。
 * @author liyibo
 * @date 2026-06-26 10:38
 */
@Data
@Builder
public class ActivityListVO {

    private String activityNo;

    private String activityName;

    private Integer activityStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer purchaseLimit;

    private Integer productCount;

    private LocalDateTime createTime;
}
