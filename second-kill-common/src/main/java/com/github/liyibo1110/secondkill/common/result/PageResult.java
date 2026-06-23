package com.github.liyibo1110.secondkill.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询的通用结构，业务服务查询分页数据时，MP返回的是IPage对象，这个类负责将IPage转换为统一的分页响应结构。
 * @author liyibo
 * @date 2026-06-22 14:38
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;

    public PageResult() {}

    public PageResult(List<T> records, long total, long pageNum, long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
    }

    /**
     * 从MyBatis Plus的IPage构建分页结果，记录类型一致。
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 从MyBatis Plus的IPage构建分页结果，支持记录类型转换。
     * 适用于DO转VO的场景：先用IPage查出DO列表，转换为VO列表后传入。
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public long getTotalPages() {
        if (this.pageSize == 0)
            return 0;

        return (this.total + this.pageSize - 1) / this.pageSize;
    }
}
