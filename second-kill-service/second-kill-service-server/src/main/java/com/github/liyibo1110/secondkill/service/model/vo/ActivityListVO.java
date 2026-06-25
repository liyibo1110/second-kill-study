package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动列表项响应。
 * @author liyibo
 * @date 2026-06-24 11:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityNo;

    private String activityName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer activityStatus;
}
