package com.github.liyibo1110.secondkill.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillOrderItemMapper;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderItemEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:15
 */
@Service
public class SecondKillOrderItemServiceImpl extends ServiceImpl<SecondKillOrderItemMapper, SecondKillOrderItemEntity>
        implements SecondKillOrderItemService {

    @Override
    public List<SecondKillOrderItemEntity> listByOrderNo(String orderNo) {
        return list(new LambdaQueryWrapper<SecondKillOrderItemEntity>()
                .eq(SecondKillOrderItemEntity::getOrderNo, orderNo));
    }
}
