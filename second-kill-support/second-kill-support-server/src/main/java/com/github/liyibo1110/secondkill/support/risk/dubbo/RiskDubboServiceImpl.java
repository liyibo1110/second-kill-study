package com.github.liyibo1110.secondkill.support.risk.dubbo;

import com.github.liyibo1110.secondkill.support.api.RiskDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.RiskRecordDTO;
import com.github.liyibo1110.secondkill.support.api.request.RecordRiskRequest;
import com.github.liyibo1110.secondkill.support.risk.converter.RiskRecordConverter;
import com.github.liyibo1110.secondkill.support.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:53
 */
@DubboService
@RequiredArgsConstructor
public class RiskDubboServiceImpl implements RiskDubboService {

    private final RiskService riskService;
    private final RiskRecordConverter riskRecordConverter;

    @Override
    public List<RiskRecordDTO> listByUserId(Long userId) {
        return riskRecordConverter.toDTOList(riskService.listByUserId(userId));
    }

    @Override
    public Integer countRecentActions(Long userId, String actionType) {
        return riskService.countRecentActions(userId, actionType);
    }

    @Override
    public void recordRiskAction(RecordRiskRequest request) {
        riskService.recordRiskAction(request);
    }

    @Override
    public boolean hasHighRiskRecord(Long userId) {
        return riskService.hasHighRiskRecord(userId);
    }
}
