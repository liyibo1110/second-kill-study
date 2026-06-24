package com.github.liyibo1110.secondkill.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderItemEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:07
 */
public interface SecondKillOrderItemService extends IService<SecondKillOrderItemEntity> {

    List<SecondKillOrderItemEntity> listByOrderNo(String orderNo);
}
