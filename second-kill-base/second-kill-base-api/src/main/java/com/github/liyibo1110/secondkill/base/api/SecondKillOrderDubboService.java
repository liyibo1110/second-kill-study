package com.github.liyibo1110.secondkill.base.api;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderItemDTO;
import com.github.liyibo1110.secondkill.base.api.request.CreateOrderRequest;

import java.util.List;

/**
 * 订单相关的Dubbo接口。
 * @author liyibo
 * @date 2026-06-23 15:44
 */
public interface SecondKillOrderDubboService {

    SecondKillOrderDTO getByOrderNo(String orderNo);

    List<SecondKillOrderDTO> listByUserId(Long userId);

    List<SecondKillOrderDTO> listByActivityNo(String activityNo);

    List<SecondKillOrderItemDTO> listOrderItems(String orderNo);

    String generateOrderNo(Long userId);

    SecondKillOrderDTO createOrder(CreateOrderRequest request);

    void updatePaidInfo(String orderNo, String transactionNo);

    void cancelOrder(String orderNo);

    void closeOrder(String orderNo);
}
