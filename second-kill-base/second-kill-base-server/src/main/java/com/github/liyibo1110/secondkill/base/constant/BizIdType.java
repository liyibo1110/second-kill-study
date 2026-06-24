package com.github.liyibo1110.secondkill.base.constant;

import com.github.liyibo1110.secondkill.common.id.IdGenerator;
import lombok.Getter;

/**
 * 业务ID类型。
 * 编码规则：系统标识（3位） + 业务编号（3位）
 * @author liyibo
 * @date 2026-06-23 15:18
 */
@Getter
public enum BizIdType {

    ACTIVITY("100001", "秒杀活动编号"),
    ORDER("100002", "秒杀订单编号"),
    REFUND("100004", "退款流水编号");

    private final String prefix;
    private final String description;

    BizIdType(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }

    /**
     * 生成带业务前缀的ID：prefix + 雪花算法。
     */
    public String generateId() {
        return prefix + IdGenerator.nextId();
    }
}
