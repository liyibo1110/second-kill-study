package com.github.liyibo1110.secondkill.base.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity类是数据库表的映射，字段是跟着表结构走的，而且类本身还附带了@TableName、@TableLogic这些MP的注解。
 * 如果把这些Entity直接作为Dubbo接口的返回值，调用方就要被迫依赖MP相关的包，而且Entity里面的内部字段（例如isDeleted、updateBy）也会暴露。
 *
 * DTO是面向调用者的数据视图，只包含了调用方需要的字段，不带有任何ORM注解，Entity和DTO之间通过MapStruct做转换，成本很低。
 * @author liyibo
 * @date 2026-06-23 15:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillActivityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;
}
