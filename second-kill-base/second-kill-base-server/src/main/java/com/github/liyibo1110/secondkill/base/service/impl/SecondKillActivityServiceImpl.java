package com.github.liyibo1110.secondkill.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.base.constant.BizIdType;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillActivityMapper;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillActivityService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:08
 */
@Service
public class SecondKillActivityServiceImpl extends ServiceImpl<SecondKillActivityMapper, SecondKillActivityEntity>
        implements SecondKillActivityService {

    @Override
    public SecondKillActivityEntity getByActivityNo(String activityNo) {
        return getOne(new LambdaQueryWrapper<SecondKillActivityEntity>()
                .eq(SecondKillActivityEntity::getActivityNo, activityNo));
    }

    @Override
    public List<SecondKillActivityEntity> listActiveActivities() {
        // activityStatus = 1 表示进行中
        return list(new LambdaQueryWrapper<SecondKillActivityEntity>()
                .eq(SecondKillActivityEntity::getActivityStatus, 1)
                .orderByDesc(SecondKillActivityEntity::getCreateTime));
    }

    @Override
    public List<SecondKillActivityEntity> listByActivityNos(List<String> activityNos) {
        if (activityNos == null || activityNos.isEmpty()) {
            return Collections.emptyList();
        }
        return list(new LambdaQueryWrapper<SecondKillActivityEntity>()
                .in(SecondKillActivityEntity::getActivityNo, activityNos));
    }

    @Override
    public List<SecondKillActivityEntity> listByStatus(Integer status) {
        return list(new LambdaQueryWrapper<SecondKillActivityEntity>()
                .eq(SecondKillActivityEntity::getActivityStatus, status)
                .orderByAsc(SecondKillActivityEntity::getStartTime));
    }

    @Override
    public String createActivity(SecondKillActivityEntity entity) {
        String activityNo = BizIdType.ACTIVITY.generateId();
        entity.setActivityNo(activityNo);
        if (entity.getActivityStatus() == null) {
            entity.setActivityStatus(0);
        }
        save(entity);
        return activityNo;
    }
}
