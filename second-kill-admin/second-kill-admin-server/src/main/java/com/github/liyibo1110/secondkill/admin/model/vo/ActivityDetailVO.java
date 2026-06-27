package com.github.liyibo1110.secondkill.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动详情的响应实体。
 * @author liyibo
 * @date 2026-06-26 10:38
 */
@Data
@Builder
public class ActivityDetailVO {

    private String activityNo;

    private String activityName;

    private Integer activityStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer purchaseLimit;

    private String remark;

    private List<ActivityProductVO> products;

    private LocalDateTime createTime;
}
