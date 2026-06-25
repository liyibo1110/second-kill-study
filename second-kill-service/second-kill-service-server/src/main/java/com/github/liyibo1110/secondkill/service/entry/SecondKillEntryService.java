package com.github.liyibo1110.secondkill.service.entry;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.mq.SecondKillOrderMessage;
import com.github.liyibo1110.secondkill.common.context.UserContext;
import com.github.liyibo1110.secondkill.common.id.IdGenerator;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.enums.SecondKillResultEnum;
import com.github.liyibo1110.secondkill.service.activity.ActivityQueryService;
import com.github.liyibo1110.secondkill.service.check.BlackListService;
import com.github.liyibo1110.secondkill.service.check.MachineCheckService;
import com.github.liyibo1110.secondkill.service.check.UserRateLimitService;
import com.github.liyibo1110.secondkill.service.model.request.SecondKillPartInRequest;
import com.github.liyibo1110.secondkill.service.model.vo.QueueCheckVO;
import com.github.liyibo1110.secondkill.service.model.vo.SecondKillPartInVO;
import com.github.liyibo1110.secondkill.service.mq.SecondKillMessageProducer;
import com.github.liyibo1110.secondkill.service.product.ProductQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀下单入口服务。
 *
 * 串联起机审校验、用户限流、活动校验、库存校验等全部前置检查，通过后发送MQ消息进入排队流程。
 * @author liyibo
 * @date 2026-06-24 14:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecondKillEntryService {

    private final MachineCheckService machineCheckService;
    private final UserRateLimitService userRateLimitService;
    private final BlackListService blackListService;
    private final ActivityQueryService activityQueryService;
    private final ProductQueryService productQueryService;
    private final SecondKillMessageProducer messageProducer;
    private final RedisService redisService;
    private final IdGenerator idGenerator;

    /**
     * 秒杀下单入口。
     * 依次执行：机审校验 → 用户限流 → 活动校验 → 小黑屋检查 → 库存校验 → 发送MQ
     */
    public SecondKillPartInVO partIn(SecondKillPartInRequest request) {
        Long userId = UserContext.getUserId();
        SecondKillPartInVO vo = new SecondKillPartInVO().setSkuId(request.getSkuId());

        // 1. 机审校验
        if (!machineCheckService.verify(userId, request.getRandom()))
            return vo.setCode(SecondKillResultEnum.FAIL.getCode()).setMessage(SecondKillResultEnum.FAIL.getMessage());

        // 2. 用户级限流
        if (!userRateLimitService.tryAcquire(userId))
            return vo.setCode(SecondKillResultEnum.FAIL.getCode()).setMessage(SecondKillResultEnum.FAIL.getMessage());

        // 3. 活动校验
        SecondKillActivityDTO activity = activityQueryService.getActivity(request.getActivityNo());
        if (activity == null)
            return vo.setCode(SecondKillResultEnum.ACTIVITY_NOT_OPEN.getCode()).setMessage("活动不存在");

        if (!activityQueryService.isActivityOpen(activity)) {
            // 活动未开始，触发小黑屋标记
            blackListService.markIfSuspicious(activity, userId);
            return vo.setCode(SecondKillResultEnum.ACTIVITY_NOT_OPEN.getCode()).setMessage(SecondKillResultEnum.ACTIVITY_NOT_OPEN.getMessage());
        }

        // 4. 小黑屋检查
        if (blackListService.isRiskUser(userId)) {
            StructuredLog.warn(log)
                    .message("风险用户被拦截")
                    .put("userId", userId)
                    .put("activityNo", request.getActivityNo())
                    .log();
            return vo.setCode(SecondKillResultEnum.RISK_USER.getCode()).setMessage(SecondKillResultEnum.RISK_USER.getMessage());
        }

        // 5. 库存预检（Redis中的库存快照，非精确值）
        Integer stock = productQueryService.getSkuStock(Long.valueOf(activity.getActivityNo()), Long.valueOf(request.getSkuId()));
        if (stock == null || stock <= 0)
            return vo.setCode(SecondKillResultEnum.STOCK_EMPTY.getCode()).setMessage(SecondKillResultEnum.STOCK_EMPTY.getMessage());

        // 6. 生成令牌和追踪ID，发送MQ消息
        String token = String.valueOf(idGenerator.nextId());
        String traceId = String.valueOf(idGenerator.nextId());

        SecondKillOrderMessage message = new SecondKillOrderMessage();
        message.setUserId(userId);
        message.setActivityNo(request.getActivityNo());
        message.setSkuId(request.getSkuId());
        message.setQuantity(request.getQuantity());
        message.setTotalFee(request.getTotalFee());
        message.setToken(token);
        message.setTraceId(traceId);
        message.setRequestTime(System.currentTimeMillis());

        boolean sent = messageProducer.send(message);
        if (!sent)
            return vo.setCode(SecondKillResultEnum.FAIL.getCode()).setMessage("系统繁忙，请重试");

        // 7. 缓存排队状态，供前端轮询
        String queuingKey = RedisKeyConstants.orderQueuingKey(userId, Long.valueOf(request.getActivityNo()));
        redisService.set(queuingKey, traceId, 60, TimeUnit.SECONDS);

        StructuredLog.info(log)
                .message("秒杀请求已入队")
                .put("userId", userId)
                .put("activityNo", request.getActivityNo())
                .put("skuId", request.getSkuId())
                .put("traceId", traceId)
                .log();

        return vo.setCode(SecondKillResultEnum.SUCCESS.getCode())
                .setMessage(SecondKillResultEnum.SUCCESS.getMessage())
                .setToken(token)
                .setTraceId(traceId);
    }

    /**
     * 排队结果轮询，前端在下单成功后定时调用，检查消费端是否已生成订单。
     */
    public QueueCheckVO checkQueue(String traceId, Long userId) {
        QueueCheckVO vo = new QueueCheckVO();

        if (traceId == null || traceId.isEmpty())
            return vo.setPollStatus(SecondKillResultEnum.POLL_STOP.getCode());

        // 检查是否有对应的订单号生成
        String orderKey = RedisKeyConstants.orderResultKey(traceId);
        String orderNo = redisService.get(orderKey);

        if (orderNo != null && !orderNo.isEmpty())
            // 订单已生成，返回订单号
            return vo.setPollStatus(SecondKillResultEnum.POLL_SUCCESS.getCode()).setOrderNo(orderNo);

        // 订单尚未生成，继续轮询
        return vo.setPollStatus(SecondKillResultEnum.POLL_CONTINUE.getCode());
    }
}
