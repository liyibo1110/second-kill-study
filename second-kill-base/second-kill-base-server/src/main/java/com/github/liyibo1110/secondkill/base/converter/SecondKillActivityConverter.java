package com.github.liyibo1110.secondkill.base.converter;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 15:58
 */
@Mapper(componentModel = "spring")
public interface SecondKillActivityConverter {

    @Mapping(source = "createTime", target = "createTime")
    SecondKillActivityDTO toDTO(SecondKillActivityEntity entity);

    List<SecondKillActivityDTO> toDTOList(List<SecondKillActivityEntity> entities);
}
