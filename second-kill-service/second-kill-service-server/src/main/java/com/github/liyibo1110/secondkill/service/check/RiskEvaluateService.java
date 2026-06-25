package com.github.liyibo1110.secondkill.service.check;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.config.SecondKillProperties;
import com.github.liyibo1110.secondkill.support.api.RiskDubboService;
import com.github.liyibo1110.secondkill.support.api.constant.RiskActionTypeEnum;
import com.github.liyibo1110.secondkill.support.api.constant.RiskLevelEnum;
import com.github.liyibo1110.secondkill.support.api.request.RecordRiskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 风控评估服务。
 *
 * 整合Redis黑名单检查、数据库高风险记录查询和行为频次分析，提供统一的风险评估入口。
 * 判定结果写入风控记录表，高风险用户同时写入Redis黑名单做实时拦截。
 *
 * @author liyibo
 * @date 2026-06-24 13:49
 */
@Slf4j
@Service
public class RiskEvaluateService {

    private static final long RISK_USER_EXPIRE_SECONDS = 86400;
    private static final int HIGH_RISK_THRESHOLD = 10;

    private final RedisService redisService;
    private final SecondKillProperties secondKillProperties;

    @DubboReference
    private RiskDubboService riskDubboService;

    public RiskEvaluateService(RedisService redisService,
                               SecondKillProperties secondKillProperties) {
        this.redisService = redisService;
        this.secondKillProperties = secondKillProperties;
    }

    /**
     * 综合评估用户风险等级。
     * 检查顺序：Redis黑名单 → 数据库高风险记录 → 行为频次分析
     *
     * @return true=风险用户需拦截，false=正常放行
     */
    public boolean evaluate(Long userId, String requestIp) {
        // 第一层：Redis黑名单（自动+手动标记）
        if (isInBlackList(userId))
            return true;

        // 第二层：数据库高风险记录（最近24小时）
        try {
            if (riskDubboService.hasHighRiskRecord(userId)) {
                // 高风险记录存在，同步写入Redis黑名单加速后续拦截
                markAsRiskUser(userId);
                return true;
            }
        } catch (Exception e) {
            // 风控服务异常不阻断业务
            StructuredLog.warn(log)
                    .message("风控记录查询异常，默认放行")
                    .put("userId", userId)
                    .exception(e)
                    .log();
        }

        // 第三层：行为频次分析
        try {
            Integer recentCount = riskDubboService.countRecentActions(userId, RiskActionTypeEnum.SECOND_KILL.getCode());
            if (recentCount != null && recentCount >= secondKillProperties.getRisk().getHighRiskThreshold()) {
                recordAndMark(userId, requestIp, RiskActionTypeEnum.RATE_LIMIT_HIT,
                        RiskLevelEnum.HIGH_RISK, "1小时内秒杀次数:" + recentCount);
                return true;
            }
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("行为频次查询异常，默认放行")
                    .put("userId", userId)
                    .exception(e)
                    .log();
        }

        return false;
    }

    /**
     * 记录风控事件（异步，不阻断主流程）
     */
    public void recordRiskAction(Long userId, String requestIp,
                                 RiskActionTypeEnum actionType,
                                 RiskLevelEnum riskLevel, String info) {
        try {
            RecordRiskRequest request = RecordRiskRequest.builder()
                    .userId(userId)
                    .actionType(actionType.getCode())
                    .riskLevel(riskLevel.getCode())
                    .requestIp(requestIp)
                    .requestInfo(info)
                    .build();
            riskDubboService.recordRiskAction(request);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("风控记录写入失败")
                    .put("userId", userId)
                    .put("actionType", actionType.getCode())
                    .exception(e)
                    .log();
        }
    }

    private boolean isInBlackList(Long userId) {
        String redisKey = RedisKeyConstants.riskUserKey(userId);
        Boolean exists = redisService.hasKey(redisKey);
        return exists != null && exists;
    }

    private void markAsRiskUser(Long userId) {
        String redisKey = RedisKeyConstants.riskUserKey(userId);
        redisService.set(redisKey, "risk", secondKillProperties.getRisk().getRiskUserExpireSeconds(), TimeUnit.SECONDS);
    }

    private void recordAndMark(Long userId, String requestIp,
                               RiskActionTypeEnum actionType,
                               RiskLevelEnum riskLevel, String info) {
        markAsRiskUser(userId);
        recordRiskAction(userId, requestIp, actionType, riskLevel, info);
    }
}
