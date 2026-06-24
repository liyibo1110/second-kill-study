package com.github.liyibo1110.secondkill.base.api.constant;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 秒杀订单状态，只有四个状态，生命周期短于主域订单，以下是状态转换规则：
 * 1、WAIT_PAY → PAID（支付成功）。
 * 2、WAIT_PAY → CLOSED（支付超时自动关闭）。
 * 3、WAIT_PAY → CANCELLED（用户主动取消）。
 * 4、PAID / CLOSED / CANCELLED 都是终态，不可再流转。
 * @author liyibo
 * @date 2026-06-23 15:31
 */
public enum OrderStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),
    CLOSED(2, "已关闭"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 状态转换表：key是当前状态，value是该状态允许流转的目标状态集合
     */
    private static final Map<OrderStatusEnum, Set<OrderStatusEnum>> TRANSITIONS;

    static {
        Map<OrderStatusEnum, Set<OrderStatusEnum>> map = new EnumMap<>(OrderStatusEnum.class);
        map.put(WAIT_PAY, EnumSet.of(PAID, CLOSED, CANCELLED));
        map.put(PAID, EnumSet.noneOf(OrderStatusEnum.class));
        map.put(CLOSED, EnumSet.noneOf(OrderStatusEnum.class));
        map.put(CANCELLED, EnumSet.noneOf(OrderStatusEnum.class));
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 判断从当前状态到目标状态的转换是否合法
     */
    public boolean canTransitTo(OrderStatusEnum target) {
        Set<OrderStatusEnum> allowed = TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /**
     * 根据code值查找枚举
     */
    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code)
                return status;
        }
        throw new IllegalArgumentException("未知的订单状态码: " + code);
    }

    /**
     * 判断给定状态码是否为终态
     */
    public static boolean isTerminal(int code) {
        OrderStatusEnum status = of(code);
        Set<OrderStatusEnum> allowed = TRANSITIONS.get(status);
        return allowed == null || allowed.isEmpty();
    }
}
