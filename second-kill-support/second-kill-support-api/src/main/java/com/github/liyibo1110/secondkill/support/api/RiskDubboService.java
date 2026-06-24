package com.github.liyibo1110.secondkill.support.api;

import com.github.liyibo1110.secondkill.support.api.dto.RiskRecordDTO;
import com.github.liyibo1110.secondkill.support.api.request.RecordRiskRequest;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:33
 */
public interface RiskDubboService {

    List<RiskRecordDTO> listByUserId(Long userId);

    Integer countRecentActions(Long userId, String actionType);

    void recordRiskAction(RecordRiskRequest request);

    /**
     * 查询用户最近是否存在高风险记录
     */
    boolean hasHighRiskRecord(Long userId);
}
