package com.github.liyibo1110.secondkill.support.api.constant;

/**
 * 风控行为类型枚举
 * @author liyibo
 * @date 2026-06-23 17:27
 */
public enum RiskActionTypeEnum {

    SECOND_KILL("SECOND_KILL", "秒杀下单"),
    PRE_CHECK("PRE_CHECK", "活动前探测"),
    MACHINE_CHECK_FAIL("MACHINE_CHECK_FAIL", "机审校验失败"),
    RATE_LIMIT_HIT("RATE_LIMIT_HIT", "触发用户级限流"),
    REPEAT_SUBMIT("REPEAT_SUBMIT", "重复提交");

    private final String code;
    private final String desc;

    RiskActionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
