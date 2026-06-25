package com.github.liyibo1110.secondkill.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 秒杀业务配置，通过Nacos配置中心动态管理，支持运行时热更新。
 * @author liyibo
 * @date 2026-06-24 11:50
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "secondkill")
public class SecondKillProperties {

    private MachineCheck machineCheck = new MachineCheck();

    private UserRateLimit userRateLimit = new UserRateLimit();

    private BlackList blackList = new BlackList();

    private DegradeSwitch degrade = new DegradeSwitch();

    private RiskConfig risk = new RiskConfig();

    /**
     * 机审校验配置
     */
    @Data
    public static class MachineCheck {

        /** 机审校验开关，关闭后所有请求直接通过机审 */
        private boolean enabled = true;

        /** 随机串长度，同时也是时间戳取模的除数 */
        private int randomLength = 16;

        /** 机审令牌在Redis中的过期时间（秒） */
        private int ttlSeconds = 30;
    }

    /**
     * 用户级限流配置
     */
    @Data
    public static class UserRateLimit {

        /** 用户级限流开关 */
        private boolean enabled = true;

        /** 时间窗口内允许的请求次数 */
        private int rate = 10;

        /** 时间窗口大小（秒） */
        private int interval = 10;

        /** 限流器过期时间（分钟） */
        private int expireMinutes = 5;
    }

    /**
     * 黑名单配置
     */
    @Data
    public static class BlackList {

        /** 小黑屋开关 */
        private boolean enabled = true;

        /** 活动开始前多少秒开始标记（默认300秒，即5分钟） */
        private int markStartSeconds = 300;

        /** 活动开始前多少秒停止标记（默认10秒，防止前后端时间差） */
        private int markEndSeconds = 10;

        /** 黑名单记录过期时间（秒） */
        private int expireSeconds = 300;
    }

    /**
     * 降级开关配置
     * 所有开关默认关闭（false），打开后对应功能走降级逻辑
     */
    @Data
    public static class DegradeSwitch {

        /** 秒杀入口总开关，打开后所有秒杀请求直接返回活动已结束 */
        private boolean seckillClosed = false;

        /** 跳过机审校验，打开后所有请求直接通过机审环节 */
        private boolean skipMachineCheck = false;

        /** 跳过风控评估，打开后不调用RiskEvaluateService */
        private boolean skipRiskCheck = false;

        /** 跳过订单同步，打开后支付成功不发送同步MQ消息 */
        private boolean skipOrderSync = false;

        /** 跳过发卡，打开后支付成功不触发自由卡发放 */
        private boolean skipCardIssue = false;
    }

    /**
     * 风控评估配置
     */
    @Data
    public static class RiskConfig {

        /** 1小时内秒杀次数达到此阈值判定为高风险 */
        private int highRiskThreshold = 10;

        /** 风险用户Redis黑名单过期时间（秒） */
        private long riskUserExpireSeconds = 86400;
    }
}
