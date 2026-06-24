package com.github.liyibo1110.secondkill.support.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.support.member.mapper.UserMapper;
import com.github.liyibo1110.secondkill.support.member.service.MemberService;
import com.github.liyibo1110.secondkill.support.model.entity.UserEntity;
import org.springframework.stereotype.Service;

/**
 * @author liyibo
 * @date 2026-06-23 17:48
 */
@Service
public class MemberServiceImpl extends ServiceImpl<UserMapper, UserEntity>
        implements MemberService {

    @Override
    public UserEntity getByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getPhone, phone));
    }
}
