package com.github.liyibo1110.secondkill.support.account.dubbo;

import com.github.liyibo1110.secondkill.support.account.converter.FreeCardConverter;
import com.github.liyibo1110.secondkill.support.account.service.FreeCardService;
import com.github.liyibo1110.secondkill.support.api.FreeCardDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.FreeCardDTO;
import com.github.liyibo1110.secondkill.support.api.request.ActivateCardRequest;
import com.github.liyibo1110.secondkill.support.api.request.IssueCardRequest;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:51
 */
@DubboService
@RequiredArgsConstructor
public class FreeCardDubboServiceImpl implements FreeCardDubboService {

    private final FreeCardService freeCardService;
    private final FreeCardConverter freeCardConverter;

    @Override
    public FreeCardDTO getByCardNo(String cardNo) {
        return freeCardConverter.toDTO(freeCardService.getByCardNo(cardNo));
    }

    @Override
    public List<FreeCardDTO> listByUserId(Long userId) {
        return freeCardConverter.toDTOList(freeCardService.listByUserId(userId));
    }

    @Override
    public String issueCard(IssueCardRequest request) {
        return freeCardService.issueCard(request);
    }

    @Override
    public void activateCard(ActivateCardRequest request) {
        freeCardService.activateCard(request);
    }

    @Override
    public void freezeCard(String cardNo) {
        freeCardService.freezeCard(cardNo);
    }

    @Override
    public void unfreezeCard(String cardNo) {
        freeCardService.unfreezeCard(cardNo);
    }
}
