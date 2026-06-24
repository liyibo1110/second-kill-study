package com.github.liyibo1110.secondkill.support.account.converter;

import com.github.liyibo1110.secondkill.support.api.dto.FreeCardDTO;
import com.github.liyibo1110.secondkill.support.model.entity.FreeCardEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:39
 */
@Mapper(componentModel = "spring")
public interface FreeCardConverter {

    FreeCardDTO toDTO(FreeCardEntity entity);

    List<FreeCardDTO> toDTOList(List<FreeCardEntity> entities);
}
