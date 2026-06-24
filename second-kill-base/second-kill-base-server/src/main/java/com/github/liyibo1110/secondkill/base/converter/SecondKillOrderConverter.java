package com.github.liyibo1110.secondkill.base.converter;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillOrderItemDTO;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillOrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 15:59
 */
@Mapper(componentModel = "spring")
public interface SecondKillOrderConverter {

    @Mapping(source = "createTime", target = "createTime")
    SecondKillOrderDTO toDTO(SecondKillOrderEntity entity);

    List<SecondKillOrderDTO> toDTOList(List<SecondKillOrderEntity> entities);

    SecondKillOrderItemDTO toItemDTO(SecondKillOrderItemEntity entity);

    List<SecondKillOrderItemDTO> toItemDTOList(List<SecondKillOrderItemEntity> entities);
}
