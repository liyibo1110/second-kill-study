package com.github.liyibo1110.secondkill.support.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import com.github.liyibo1110.secondkill.support.model.entity.OrderEntity;
import com.github.liyibo1110.secondkill.support.order.mapper.OrderMapper;
import com.github.liyibo1110.secondkill.support.order.service.OrderSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:49
 */
@Slf4j
@Service
public class OrderSyncServiceImpl extends ServiceImpl<OrderMapper, OrderEntity>
        implements OrderSyncService {

    @Override
    public OrderEntity getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo));
    }

    @Override
    public List<OrderEntity> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, userId)
                .orderByDesc(OrderEntity::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncOrder(SyncOrderRequest request) {
        // 数据库层幂等：先查是否已存在
        if (existsByOrderNo(request.getOrderNo())) {
            StructuredLog.info(log)
                    .message("主域订单已存在，跳过同步")
                    .put("orderNo", request.getOrderNo())
                    .log();
            return;
        }

        OrderEntity order = new OrderEntity();
        order.setOrderNo(request.getOrderNo());
        order.setUserId(request.getUserId());
        order.setOrderSource(request.getOrderSource());
        order.setTotalAmount(request.getTotalAmount());
        order.setDiscountAmount(request.getDiscountAmount());
        order.setPayAmount(request.getPayAmount());
        order.setOrderStatus(1);
        order.setPaidTime(request.getPaidTime());
        order.setTransactionNo(request.getTransactionNo());
        save(order);

        StructuredLog.info(log)
                .message("主域订单写入成功")
                .put("orderNo", request.getOrderNo())
                .put("userId", request.getUserId())
                .log();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderNo, Integer status) {
        update(new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .set(OrderEntity::getOrderStatus, status));
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return count(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)) > 0;
    }
}
