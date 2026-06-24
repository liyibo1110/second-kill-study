package com.github.liyibo1110.secondkill.support.risk.converter;

import com.github.liyibo1110.secondkill.support.api.dto.RiskRecordDTO;
import com.github.liyibo1110.secondkill.support.model.entity.RiskRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:40
 */
@Mapper(componentModel = "spring")
public interface RiskRecordConverter {

    @Mapping(source = "createTime", target = "createTime")
    RiskRecordDTO toDTO(RiskRecordEntity entity);

    List<RiskRecordDTO> toDTOList(List<RiskRecordEntity> entities);
}
