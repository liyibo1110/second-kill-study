package com.github.liyibo1110.secondkill.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:04
 */
public interface SecondKillActivityService extends IService<SecondKillActivityEntity> {

    SecondKillActivityEntity getByActivityNo(String activityNo);

    List<SecondKillActivityEntity> listActiveActivities();

    List<SecondKillActivityEntity> listByActivityNos(List<String> activityNos);

    List<SecondKillActivityEntity> listByStatus(Integer status);

    String createActivity(SecondKillActivityEntity entity);
}
