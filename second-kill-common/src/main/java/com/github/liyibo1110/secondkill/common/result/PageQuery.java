package com.github.liyibo1110.secondkill.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询的通用参数。
 * @author liyibo
 * @date 2026-06-22 14:37
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private long pageNum = 1;

    private long pageSize = 10;
}
