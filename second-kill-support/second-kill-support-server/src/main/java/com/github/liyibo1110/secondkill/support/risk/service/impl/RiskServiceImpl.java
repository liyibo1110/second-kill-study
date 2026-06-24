package com.github.liyibo1110.secondkill.support.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.support.api.constant.RiskLevelEnum;
import com.github.liyibo1110.secondkill.support.api.request.RecordRiskRequest;
import com.github.liyibo1110.secondkill.support.model.entity.RiskRecordEntity;
import com.github.liyibo1110.secondkill.support.risk.mapper.RiskRecordMapper;
import com.github.liyibo1110.secondkill.support.risk.service.RiskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:50
 */
@Slf4j
@Service
public class RiskServiceImpl extends ServiceImpl<RiskRecordMapper, RiskRecordEntity>
        implements RiskService {

    @Override
    public List<RiskRecordEntity> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<RiskRecordEntity>()
                .eq(RiskRecordEntity::getUserId, userId)
                .orderByDesc(RiskRecordEntity::getCreateTime));
    }

    @Override
    public Integer countRecentActions(Long userId, String actionType) {
        // 统计最近1小时内的操作次数
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return (int) count(new LambdaQueryWrapper<RiskRecordEntity>()
                .eq(RiskRecordEntity::getUserId, userId)
                .eq(RiskRecordEntity::getActionType, actionType)
                .ge(RiskRecordEntity::getCreateTime, oneHourAgo));
    }

    @Override
    public void recordRiskAction(RecordRiskRequest request) {
        RiskRecordEntity entity = new RiskRecordEntity();
        entity.setUserId(request.getUserId());
        entity.setActionType(request.getActionType());
        entity.setRiskLevel(request.getRiskLevel());
        entity.setRequestIp(request.getRequestIp());
        entity.setRequestInfo(request.getRequestInfo());
        save(entity);

        StructuredLog.info(log)
                .message("风控行为已记录")
                .put("userId", request.getUserId())
                .put("actionType", request.getActionType())
                .put("riskLevel", request.getRiskLevel())
                .log();
    }

    @Override
    public boolean hasHighRiskRecord(Long userId) {
        // 查询最近24小时内是否有高风险记录
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        return count(new LambdaQueryWrapper<RiskRecordEntity>()
                .eq(RiskRecordEntity::getUserId, userId)
                .eq(RiskRecordEntity::getRiskLevel, RiskLevelEnum.HIGH_RISK.getCode())
                .ge(RiskRecordEntity::getCreateTime, oneDayAgo)) > 0;
    }
}
