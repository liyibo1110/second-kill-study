package com.github.liyibo1110.secondkill.base.dubbo;

import com.github.liyibo1110.secondkill.base.api.SecondKillOrderDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderItemDTO;
import com.github.liyibo1110.secondkill.base.api.request.CreateOrderRequest;
import com.github.liyibo1110.secondkill.base.converter.SecondKillOrderConverter;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillOrderItemService;
import com.github.liyibo1110.secondkill.base.service.SecondKillOrderService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:21
 */
@DubboService
@RequiredArgsConstructor
public class SecondKillOrderDubboServiceImpl implements SecondKillOrderDubboService {

    private final SecondKillOrderService secondKillOrderService;
    private final SecondKillOrderItemService secondKillOrderItemService;
    private final SecondKillOrderConverter secondKillOrderConverter;

    @Override
    public SecondKillOrderDTO getByOrderNo(String orderNo) {
        return secondKillOrderConverter.toDTO(secondKillOrderService.getByOrderNo(orderNo));
    }

    @Override
    public List<SecondKillOrderDTO> listByUserId(Long userId) {
        return secondKillOrderConverter.toDTOList(secondKillOrderService.listByUserId(userId));
    }

    @Override
    public List<SecondKillOrderDTO> listByActivityNo(String activityNo) {
        return secondKillOrderConverter.toDTOList(secondKillOrderService.listByActivityNo(activityNo));
    }

    @Override
    public List<SecondKillOrderItemDTO> listOrderItems(String orderNo) {
        return secondKillOrderConverter.toItemDTOList(secondKillOrderItemService.listByOrderNo(orderNo));
    }

    @Override
    public String generateOrderNo(Long userId) {
        return secondKillOrderService.generateOrderNo(userId);
    }

    @Override
    public SecondKillOrderDTO createOrder(CreateOrderRequest request) {
        SecondKillOrderEntity entity = secondKillOrderService.createOrder(request);
        return secondKillOrderConverter.toDTO(entity);
    }

    @Override
    public void updatePaidInfo(String orderNo, String transactionNo) {
        secondKillOrderService.updatePaidInfo(orderNo, transactionNo);
    }

    @Override
    public void cancelOrder(String orderNo) {
        secondKillOrderService.cancelOrder(orderNo);
    }

    @Override
    public void closeOrder(String orderNo) {
        secondKillOrderService.closeOrder(orderNo);
    }
}
