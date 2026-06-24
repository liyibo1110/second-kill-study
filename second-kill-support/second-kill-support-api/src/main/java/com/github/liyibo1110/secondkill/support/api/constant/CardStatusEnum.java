package com.github.liyibo1110.secondkill.support.api.constant;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 自由卡状态枚举。
 * 自由卡的生命周期：支付成功后发卡（未激活）→ 用户激活 → 正常使用 → 过期。
 * 管理员可以在任意非终态下冻结卡片，冻结后可解冻恢复。
 *
 * 状态转换规则：
 * INACTIVE → ACTIVATED（用户激活）
 * INACTIVE → FROZEN（管理员冻结）
 * INACTIVE → EXPIRED（超过激活期限）
 * ACTIVATED → FROZEN（管理员冻结）
 * ACTIVATED → EXPIRED（超过有效期）
 * FROZEN → ACTIVATED（管理员解冻）
 * EXPIRED是终态，不可再流转。
 *
 * @author liyibo
 * @date 2026-06-23 17:09
 */
public enum CardStatusEnum {

    INACTIVE(0, "未激活"),
    ACTIVATED(1, "已激活"),
    FROZEN(2, "已冻结"),
    EXPIRED(3, "已过期");

    private final int code;
    private final String desc;

    private static final Map<CardStatusEnum, Set<CardStatusEnum>> TRANSITIONS;

    CardStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        Map<CardStatusEnum, Set<CardStatusEnum>> map = new EnumMap<>(CardStatusEnum.class);
        map.put(INACTIVE, EnumSet.of(ACTIVATED, FROZEN, EXPIRED));
        map.put(ACTIVATED, EnumSet.of(FROZEN, EXPIRED));
        map.put(FROZEN, EnumSet.of(ACTIVATED));
        map.put(EXPIRED, EnumSet.noneOf(CardStatusEnum.class));
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean canTransitTo(CardStatusEnum target) {
        Set<CardStatusEnum> allowed = TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    public static CardStatusEnum of(int code) {
        for (CardStatusEnum status : values()) {
            if (status.code == code)
                return status;
        }
        throw new IllegalArgumentException("未知的自由卡状态码: " + code);
    }

    public static boolean isTerminal(int code) {
        CardStatusEnum status = of(code);
        Set<CardStatusEnum> allowed = TRANSITIONS.get(status);
        return allowed == null || allowed.isEmpty();
    }
}
