package com.github.liyibo1110.secondkill.admin.model.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 创建活动接口的请求实体。
 * @author liyibo
 * @date 2026-06-26 10:36
 */
@Data
public class CreateActivityCommand {

    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    private Integer purchaseLimit;

    private String remark;
}
