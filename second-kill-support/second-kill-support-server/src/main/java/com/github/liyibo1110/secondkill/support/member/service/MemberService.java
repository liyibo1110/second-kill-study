package com.github.liyibo1110.secondkill.support.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.support.model.entity.UserEntity;

/**
 * @author liyibo
 * @date 2026-06-23 17:44
 */
public interface MemberService extends IService<UserEntity> {

    UserEntity getByPhone(String phone);
}
