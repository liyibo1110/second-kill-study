package com.github.liyibo1110.secondkill.support.api;

import com.github.liyibo1110.secondkill.support.api.dto.OrderDTO;
import com.github.liyibo1110.secondkill.support.api.request.SyncOrderRequest;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:33
 */
public interface OrderSyncDubboService {

    OrderDTO getByOrderNo(String orderNo);

    List<OrderDTO> listByUserId(Long userId);

    void syncOrder(SyncOrderRequest request);

    void updateOrderStatus(String orderNo, Integer status);
}
