package com.github.liyibo1110.secondkill.base.dubbo;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.converter.SecondKillActivityConverter;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillActivityService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 活动相关的Dubbo服务实现。
 * @author liyibo
 * @date 2026-06-23 16:02
 */
@DubboService
@RequiredArgsConstructor
public class SecondKillActivityDubboServiceImpl implements SecondKillActivityDubboService {

    private final SecondKillActivityService secondKillActivityService;
    private final SecondKillActivityConverter secondKillActivityConverter;

    @Override
    public SecondKillActivityDTO getByActivityNo(String activityNo) {
        SecondKillActivityEntity entity = secondKillActivityService.getByActivityNo(activityNo);
        return secondKillActivityConverter.toDTO(entity);
    }

    @Override
    public List<SecondKillActivityDTO> listActiveActivities() {
        return secondKillActivityConverter.toDTOList(secondKillActivityService.listActiveActivities());
    }

    @Override
    public List<SecondKillActivityDTO> listByActivityNos(List<String> activityNos) {
        return secondKillActivityConverter.toDTOList(secondKillActivityService.listByActivityNos(activityNos));
    }

    @Override
    public List<SecondKillActivityDTO> listByStatus(Integer status) {
        return secondKillActivityConverter.toDTOList(secondKillActivityService.listByStatus(status));
    }

    @Override
    public void updateActivityStatus(String activityNo, Integer status) {
        SecondKillActivityEntity entity = secondKillActivityService.getByActivityNo(activityNo);
        if (entity != null) {
            entity.setActivityStatus(status);
            secondKillActivityService.updateById(entity);
        }
    }

    @Override
    public String createActivity(SecondKillActivityDTO dto) {
        SecondKillActivityEntity entity = new SecondKillActivityEntity();
        entity.setActivityName(dto.getActivityName());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setPurchaseLimit(dto.getPurchaseLimit());
        entity.setRemark(dto.getRemark());
        entity.setActivityStatus(dto.getActivityStatus() != null ? dto.getActivityStatus() : 0);
        String activityNo = secondKillActivityService.createActivity(entity);
        return activityNo;
    }

    @Override
    public void updateActivity(SecondKillActivityDTO dto) {
        SecondKillActivityEntity entity = secondKillActivityService.getByActivityNo(dto.getActivityNo());
        if (entity == null)
            return;

        if (dto.getActivityName() != null)
            entity.setActivityName(dto.getActivityName());

        if (dto.getStartTime() != null)
            entity.setStartTime(dto.getStartTime());

        if (dto.getEndTime() != null)
            entity.setEndTime(dto.getEndTime());

        if (dto.getPurchaseLimit() != null)
            entity.setPurchaseLimit(dto.getPurchaseLimit());

        if (dto.getRemark() != null)
            entity.setRemark(dto.getRemark());

        secondKillActivityService.updateById(entity);
    }
}
