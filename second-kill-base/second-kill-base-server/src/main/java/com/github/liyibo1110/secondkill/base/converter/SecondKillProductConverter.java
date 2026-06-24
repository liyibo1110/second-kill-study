package com.github.liyibo1110.secondkill.base.converter;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillProductDTO;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductSkuEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillProductEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:00
 */
@Mapper(componentModel = "spring")
public interface SecondKillProductConverter {

    SecondKillProductDTO toDTO(SecondKillProductEntity entity);

    List<SecondKillProductDTO> toDTOList(List<SecondKillProductEntity> entities);

    SecondKillActivityProductDTO toActivityProductDTO(SecondKillActivityProductEntity entity);

    List<SecondKillActivityProductDTO> toActivityProductDTOList(List<SecondKillActivityProductEntity> entities);

    SecondKillActivityProductSkuDTO toActivityProductSkuDTO(SecondKillActivityProductSkuEntity entity);

    List<SecondKillActivityProductSkuDTO> toActivityProductSkuDTOList(List<SecondKillActivityProductSkuEntity> entities);
}
