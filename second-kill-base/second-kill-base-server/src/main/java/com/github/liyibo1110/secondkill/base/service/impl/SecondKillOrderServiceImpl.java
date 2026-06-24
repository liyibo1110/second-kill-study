package com.github.liyibo1110.secondkill.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.base.api.constant.OrderStatusEnum;
import com.github.liyibo1110.secondkill.base.api.request.CreateOrderRequest;
import com.github.liyibo1110.secondkill.base.constant.BizIdType;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillOrderMapper;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderItemEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillOrderItemService;
import com.github.liyibo1110.secondkill.base.service.SecondKillOrderService;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecondKillOrderServiceImpl extends ServiceImpl<SecondKillOrderMapper, SecondKillOrderEntity>
        implements SecondKillOrderService {

    private final SecondKillOrderItemService secondKillOrderItemService;

    @Override
    public SecondKillOrderEntity getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<SecondKillOrderEntity>()
                .eq(SecondKillOrderEntity::getOrderNo, orderNo));
    }

    @Override
    public List<SecondKillOrderEntity> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<SecondKillOrderEntity>()
                .eq(SecondKillOrderEntity::getUserId, userId)
                .orderByDesc(SecondKillOrderEntity::getCreateTime));
    }

    @Override
    public List<SecondKillOrderEntity> listByActivityNo(String activityNo) {
        return list(new LambdaQueryWrapper<SecondKillOrderEntity>()
                .eq(SecondKillOrderEntity::getActivityNo, activityNo)
                .orderByDesc(SecondKillOrderEntity::getCreateTime));
    }

    @Override
    public String generateOrderNo(Long userId) {
        // 订单号 = 业务前缀 + 雪花ID + 分片标识(userId % 4)
        return BizIdType.ORDER.generateId() + (int) (userId % 4);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecondKillOrderEntity createOrder(CreateOrderRequest request) {
        String orderNo = generateOrderNo(request.getUserId());

        // 写入订单主表
        SecondKillOrderEntity order = new SecondKillOrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setActivityNo(request.getActivityNo());
        order.setTotalAmount(request.getPayAmount());
        order.setPayAmount(request.getPayAmount());
        order.setOrderStatus(OrderStatusEnum.WAIT_PAY.getCode());
        order.setTraceId(request.getTraceId());
        save(order);

        // 写入订单项表
        SecondKillOrderItemEntity item = new SecondKillOrderItemEntity();
        item.setOrderNo(orderNo);
        item.setActivityNo(request.getActivityNo());
        item.setSkuNo(request.getSkuNo());
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPayAmount());
        item.setTotalAmount(request.getPayAmount());
        secondKillOrderItemService.save(item);

        StructuredLog.info(log)
                .message("秒杀订单创建成功")
                .put("orderNo", orderNo)
                .put("userId", request.getUserId())
                .put("activityNo", request.getActivityNo())
                .log();

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaidInfo(String orderNo, String transactionNo) {
        SecondKillOrderEntity order = getByOrderNo(orderNo);
        if (order == null) {
            StructuredLog.warn(log)
                    .message("支付回调找不到订单")
                    .put("orderNo", orderNo)
                    .log();
            return;
        }

        // 状态机校验：只有待支付才能流转到已支付
        OrderStatusEnum current = OrderStatusEnum.of(order.getOrderStatus());
        if (!current.canTransitTo(OrderStatusEnum.PAID)) {
            StructuredLog.warn(log)
                    .message("订单状态不允许支付")
                    .put("orderNo", orderNo)
                    .put("currentStatus", current.getDesc())
                    .log();
            return;
        }

        boolean updated = update(new LambdaUpdateWrapper<SecondKillOrderEntity>()
                .eq(SecondKillOrderEntity::getOrderNo, orderNo)
                .eq(SecondKillOrderEntity::getOrderStatus, OrderStatusEnum.WAIT_PAY.getCode())
                .set(SecondKillOrderEntity::getOrderStatus, OrderStatusEnum.PAID.getCode())
                .set(SecondKillOrderEntity::getTransactionNo, transactionNo)
                .set(SecondKillOrderEntity::getPaidTime, LocalDateTime.now()));

        if (updated) {
            StructuredLog.info(log)
                    .message("订单支付成功")
                    .put("orderNo", orderNo)
                    .put("transactionNo", transactionNo)
                    .log();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        transitStatus(orderNo, OrderStatusEnum.CANCELLED, "用户取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(String orderNo) {
        transitStatus(orderNo, OrderStatusEnum.CLOSED, "支付超时关闭");
    }

    /**
     * 通用状态流转方法，带乐观锁校验
     */
    private void transitStatus(String orderNo, OrderStatusEnum target, String reason) {
        SecondKillOrderEntity order = getByOrderNo(orderNo);
        if (order == null) {
            return;
        }

        OrderStatusEnum current = OrderStatusEnum.of(order.getOrderStatus());
        if (!current.canTransitTo(target)) {
            StructuredLog.warn(log)
                    .message("订单状态流转被拒绝")
                    .put("orderNo", orderNo)
                    .put("currentStatus", current.getDesc())
                    .put("targetStatus", target.getDesc())
                    .put("reason", reason)
                    .log();
            return;
        }

        // 用WHERE条件里的状态值做乐观锁，防止并发重复流转
        boolean updated = update(new LambdaUpdateWrapper<SecondKillOrderEntity>()
                .eq(SecondKillOrderEntity::getOrderNo, orderNo)
                .eq(SecondKillOrderEntity::getOrderStatus, current.getCode())
                .set(SecondKillOrderEntity::getOrderStatus, target.getCode())
                .set(SecondKillOrderEntity::getClosedTime, LocalDateTime.now()));

        if (updated) {
            StructuredLog.info(log)
                    .message(reason)
                    .put("orderNo", orderNo)
                    .put("from", current.getDesc())
                    .put("to", target.getDesc())
                    .log();
        }
    }
}
