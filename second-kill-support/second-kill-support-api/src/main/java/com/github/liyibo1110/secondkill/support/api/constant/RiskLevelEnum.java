package com.github.liyibo1110.secondkill.support.api.constant;

/**
 * 风控等级枚举，简化为三级的风控等级划分，正常用户不记录风控记录，只有可疑和高风险的请求才会写入t_risk_record。
 * @author liyibo
 * @date 2026-06-23 17:26
 */
public enum RiskLevelEnum {

    NORMAL(0, "正常"),
    SUSPICIOUS(1, "可疑"),
    HIGH_RISK(2, "高风险");

    private final int code;
    private final String desc;

    RiskLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RiskLevelEnum of(int code) {
        for (RiskLevelEnum level : values()) {
            if (level.code == code)
                return level;
        }
        throw new IllegalArgumentException("未知的风控等级: " + code);
    }
}
