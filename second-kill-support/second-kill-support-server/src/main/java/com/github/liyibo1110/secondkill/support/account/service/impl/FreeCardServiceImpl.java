package com.github.liyibo1110.secondkill.support.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.common.id.IdGenerator;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.support.account.mapper.FreeCardMapper;
import com.github.liyibo1110.secondkill.support.account.service.FreeCardService;
import com.github.liyibo1110.secondkill.support.api.constant.CardStatusEnum;
import com.github.liyibo1110.secondkill.support.api.request.ActivateCardRequest;
import com.github.liyibo1110.secondkill.support.api.request.IssueCardRequest;
import com.github.liyibo1110.secondkill.support.model.entity.FreeCardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:46
 */
@Slf4j
@Service
public class FreeCardServiceImpl extends ServiceImpl<FreeCardMapper, FreeCardEntity>
        implements FreeCardService {

    @Override
    public FreeCardEntity getByCardNo(String cardNo) {
        return getOne(new LambdaQueryWrapper<FreeCardEntity>()
                .eq(FreeCardEntity::getCardNo, cardNo));
    }

    @Override
    public List<FreeCardEntity> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<FreeCardEntity>()
                .eq(FreeCardEntity::getUserId, userId)
                .orderByDesc(FreeCardEntity::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String issueCard(IssueCardRequest request) {
        // 幂等：同一个订单号只发一张卡
        FreeCardEntity existing = getOne(new LambdaQueryWrapper<FreeCardEntity>()
                .eq(FreeCardEntity::getOrderNo, request.getOrderNo()));
        if (existing != null) {
            StructuredLog.info(log)
                    .message("自由卡已发放，跳过重复发卡")
                    .put("orderNo", request.getOrderNo())
                    .put("cardNo", existing.getCardNo())
                    .log();
            return existing.getCardNo();
        }

        String cardNo = "FC" + IdGenerator.nextId();

        FreeCardEntity card = new FreeCardEntity();
        card.setCardNo(cardNo);
        card.setCardName(request.getCardName());
        card.setFaceValue(request.getFaceValue());
        card.setUserId(request.getUserId());
        card.setOrderNo(request.getOrderNo());
        card.setStatus(CardStatusEnum.INACTIVE.getCode());
        card.setValidDays(request.getValidDays());
        save(card);

        StructuredLog.info(log)
                .message("自由卡发放成功")
                .put("cardNo", cardNo)
                .put("orderNo", request.getOrderNo())
                .put("userId", request.getUserId())
                .put("faceValue", request.getFaceValue())
                .log();

        return cardNo;
    }

    @Override
    public void activateCard(ActivateCardRequest request) {
        FreeCardEntity card = getByCardNo(request.getCardNo());
        if (card == null)
            throw new IllegalArgumentException("自由卡不存在: " + request.getCardNo());

        // 幂等：已激活直接返回
        if (card.getStatus() == CardStatusEnum.ACTIVATED.getCode())
            return;

        transitStatus(card, CardStatusEnum.ACTIVATED);

        // 设置激活时间和过期时间
        LocalDateTime now = LocalDateTime.now();
        int validDays = card.getValidDays() != null ? card.getValidDays() : 365;
        update(new LambdaUpdateWrapper<FreeCardEntity>()
                .eq(FreeCardEntity::getCardNo, request.getCardNo())
                .set(FreeCardEntity::getActivatedTime, now)
                .set(FreeCardEntity::getExpireTime, now.plusDays(validDays)));

        StructuredLog.info(log)
                .message("自由卡激活成功")
                .put("cardNo", request.getCardNo())
                .put("userId", request.getUserId())
                .put("validDays", validDays)
                .log();
    }

    @Override
    public void freezeCard(String cardNo) {
        FreeCardEntity card = getByCardNo(cardNo);
        if (card == null)
            throw new IllegalArgumentException("自由卡不存在: " + cardNo);

        transitStatus(card, CardStatusEnum.FROZEN);

        StructuredLog.info(log)
                .message("自由卡已冻结")
                .put("cardNo", cardNo)
                .log();
    }

    @Override
    public void unfreezeCard(String cardNo) {
        FreeCardEntity card = getByCardNo(cardNo);
        if (card == null)
            throw new IllegalArgumentException("自由卡不存在: " + cardNo);

        transitStatus(card, CardStatusEnum.ACTIVATED);

        StructuredLog.info(log)
                .message("自由卡已解冻")
                .put("cardNo", cardNo)
                .log();
    }

    /**
     * 通用状态流转方法，使用乐观锁保护
     */
    private void transitStatus(FreeCardEntity card, CardStatusEnum target) {
        CardStatusEnum current = CardStatusEnum.of(card.getStatus());
        if (!current.canTransitTo(target))
            throw new IllegalStateException("自由卡状态不允许从 " + current.getDesc() + " 转换为 " + target.getDesc() + ", cardNo=" + card.getCardNo());

        boolean updated = update(new LambdaUpdateWrapper<FreeCardEntity>()
                .eq(FreeCardEntity::getCardNo, card.getCardNo())
                .eq(FreeCardEntity::getStatus, current.getCode())
                .set(FreeCardEntity::getStatus, target.getCode()));

        if (!updated)
            throw new IllegalStateException("自由卡状态更新失败（乐观锁冲突），cardNo=" + card.getCardNo());
    }
}
