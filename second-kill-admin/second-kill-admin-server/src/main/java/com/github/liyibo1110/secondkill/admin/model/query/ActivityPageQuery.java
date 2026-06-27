package com.github.liyibo1110.secondkill.admin.model.query;

import lombok.Data;

/**
 * 查询活动接口的请求实体。
 * @author liyibo
 * @date 2026-06-26 10:37
 */
@Data
public class ActivityPageQuery {

    private String activityName;

    private Integer activityStatus;

    private Integer page = 1;

    private Integer size = 10;
}
