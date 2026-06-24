package com.github.liyibo1110.secondkill.support.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.support.api.request.RecordRiskRequest;
import com.github.liyibo1110.secondkill.support.model.entity.RiskRecordEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:45
 */
public interface RiskService extends IService<RiskRecordEntity> {

    List<RiskRecordEntity> listByUserId(Long userId);

    Integer countRecentActions(Long userId, String actionType);

    void recordRiskAction(RecordRiskRequest request);

    boolean hasHighRiskRecord(Long userId);
}
