package com.github.liyibo1110.secondkill.support.order.dubbo;

import com.github.liyibo1110.secondkill.support.api.OrderSyncDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.OrderDTO;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;
import com.github.liyibo1110.secondkill.support.order.converter.OrderConverter;
import com.github.liyibo1110.secondkill.support.order.service.OrderSyncService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:52
 */
@DubboService
@RequiredArgsConstructor
public class OrderSyncDubboServiceImpl implements OrderSyncDubboService {

    private final OrderSyncService orderSyncService;
    private final OrderConverter orderConverter;

    @Override
    public OrderDTO getByOrderNo(String orderNo) {
        return orderConverter.toDTO(orderSyncService.getByOrderNo(orderNo));
    }

    @Override
    public List<OrderDTO> listByUserId(Long userId) {
        return orderConverter.toDTOList(orderSyncService.listByUserId(userId));
    }

    @Override
    public void syncOrder(SyncOrderRequest request) {
        orderSyncService.syncOrder(request);
    }

    @Override
    public void updateOrderStatus(String orderNo, Integer status) {
        orderSyncService.updateOrderStatus(orderNo, status);
    }
}
