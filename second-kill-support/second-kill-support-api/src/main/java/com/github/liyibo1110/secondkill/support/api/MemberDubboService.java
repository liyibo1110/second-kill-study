package com.github.liyibo1110.secondkill.support.api;

import com.github.liyibo1110.secondkill.support.api.dto.UserDTO;

/**
 * @author liyibo
 * @date 2026-06-23 17:33
 */
public interface MemberDubboService {

    UserDTO getById(Long userId);

    UserDTO getByPhone(String phone);

    Integer getMemberLevel(Long userId);
}
