package com.github.liyibo1110.secondkill.support.api;

import com.github.liyibo1110.secondkill.support.api.dto.FreeCardDTO;
import com.github.liyibo1110.secondkill.support.api.request.ActivateCardRequest;
import com.github.liyibo1110.secondkill.support.api.request.IssueCardRequest;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:33
 */
public interface FreeCardDubboService {

    FreeCardDTO getByCardNo(String cardNo);

    List<FreeCardDTO> listByUserId(Long userId);

    /**
     * 发卡：支付成功后为用户创建自由卡
     *
     * @return 卡号
     */
    String issueCard(IssueCardRequest request);

    void activateCard(ActivateCardRequest request);

    void freezeCard(String cardNo);

    void unfreezeCard(String cardNo);
}
