package com.github.liyibo1110.secondkill.support.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import com.github.liyibo1110.secondkill.support.model.entity.OrderEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:45
 */
public interface OrderSyncService extends IService<OrderEntity> {

    OrderEntity getByOrderNo(String orderNo);

    List<OrderEntity> listByUserId(Long userId);

    void syncOrder(SyncOrderRequest request);

    void updateOrderStatus(String orderNo, Integer status);

    boolean existsByOrderNo(String orderNo);
}
