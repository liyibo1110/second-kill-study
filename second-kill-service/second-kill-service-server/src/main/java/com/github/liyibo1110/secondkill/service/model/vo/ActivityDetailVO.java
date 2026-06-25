package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动详情响应。
 * @author liyibo
 * @date 2026-06-24 11:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityNo;

    private String activityName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer activityStatus;

    private Integer purchaseLimit;

    /**
     * 活动当前是否可参与（状态为进行中且在时间范围内）
     */
    private Boolean activityOpen;

    private List<ProductVO> products;
}
