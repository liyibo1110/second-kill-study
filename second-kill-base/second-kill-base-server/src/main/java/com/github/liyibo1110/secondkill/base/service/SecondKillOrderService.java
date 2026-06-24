package com.github.liyibo1110.secondkill.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.base.api.request.CreateOrderRequest;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:06
 */
public interface SecondKillOrderService extends IService<SecondKillOrderEntity> {

    SecondKillOrderEntity getByOrderNo(String orderNo);

    List<SecondKillOrderEntity> listByUserId(Long userId);

    List<SecondKillOrderEntity> listByActivityNo(String activityNo);

    String generateOrderNo(Long userId);

    SecondKillOrderEntity createOrder(CreateOrderRequest request);

    void updatePaidInfo(String orderNo, String transactionNo);

    void cancelOrder(String orderNo);

    void closeOrder(String orderNo);
}
