package com.github.liyibo1110.secondkill.admin.model.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 修改活动接口的请求实体。
 * @author liyibo
 * @date 2026-06-26 10:37
 */
@Data
public class UpdateActivityCommand {

    @NotBlank(message = "活动编号不能为空")
    private String activityNo;

    private String activityName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer purchaseLimit;

    private String remark;
}
