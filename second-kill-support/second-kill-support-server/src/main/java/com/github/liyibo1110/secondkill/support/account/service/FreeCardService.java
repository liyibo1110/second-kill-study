package com.github.liyibo1110.secondkill.support.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.support.api.request.ActivateCardRequest;
import com.github.liyibo1110.secondkill.support.api.request.IssueCardRequest;
import com.github.liyibo1110.secondkill.support.model.entity.FreeCardEntity;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:43
 */
public interface FreeCardService extends IService<FreeCardEntity> {

    FreeCardEntity getByCardNo(String cardNo);

    List<FreeCardEntity> listByUserId(Long userId);

    /** 发卡：支付成功后为用户创建自由卡，返回卡号 */
    String issueCard(IssueCardRequest request);

    /** 激活自由卡 */
    void activateCard(ActivateCardRequest request);

    /** 冻结自由卡 */
    void freezeCard(String cardNo);

    /** 解冻自由卡 */
    void unfreezeCard(String cardNo);
}
