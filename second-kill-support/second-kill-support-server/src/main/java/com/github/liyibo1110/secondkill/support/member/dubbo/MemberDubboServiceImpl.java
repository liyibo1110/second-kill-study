package com.github.liyibo1110.secondkill.support.member.dubbo;

import com.github.liyibo1110.secondkill.support.api.MemberDubboService;
import com.github.liyibo1110.secondkill.support.api.dto.UserDTO;
import com.github.liyibo1110.secondkill.support.member.converter.UserConverter;
import com.github.liyibo1110.secondkill.support.member.service.MemberService;
import com.github.liyibo1110.secondkill.support.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author liyibo
 * @date 2026-06-23 17:52
 */
@DubboService
@RequiredArgsConstructor
public class MemberDubboServiceImpl implements MemberDubboService {

    private final MemberService memberService;
    private final UserConverter userConverter;

    @Override
    public UserDTO getById(Long userId) {
        UserEntity entity = memberService.getById(userId);
        return userConverter.toDTO(entity);
    }

    @Override
    public UserDTO getByPhone(String phone) {
        UserEntity entity = memberService.getByPhone(phone);
        return userConverter.toDTO(entity);
    }

    @Override
    public Integer getMemberLevel(Long userId) {
        UserEntity entity = memberService.getById(userId);
        if (entity == null)
            return 0;

        return entity.getMemberLevel();
    }
}
