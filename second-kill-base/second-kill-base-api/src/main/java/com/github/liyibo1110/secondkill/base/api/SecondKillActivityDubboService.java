package com.github.liyibo1110.secondkill.base.api;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;

import java.util.List;

/**
 * 活动相关的Dubbo接口。
 * @author liyibo
 * @date 2026-06-23 15:42
 */
public interface SecondKillActivityDubboService {

    SecondKillActivityDTO getByActivityNo(String activityNo);

    List<SecondKillActivityDTO> listActiveActivities();

    List<SecondKillActivityDTO> listByActivityNos(List<String> activityNos);

    List<SecondKillActivityDTO> listByStatus(Integer status);

    void updateActivityStatus(String activityNo, Integer status);

    String createActivity(SecondKillActivityDTO dto);

    void updateActivity(SecondKillActivityDTO dto);
}
