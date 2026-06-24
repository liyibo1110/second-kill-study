package com.github.liyibo1110.secondkill.support.member.converter;

import com.github.liyibo1110.secondkill.support.api.dto.UserDTO;
import com.github.liyibo1110.secondkill.support.model.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * @author liyibo
 * @date 2026-06-23 17:39
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserDTO toDTO(UserEntity entity);
}
