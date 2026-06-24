package com.github.liyibo1110.secondkill.support.order.converter;

import com.github.liyibo1110.secondkill.support.api.dto.OrderDTO;
import com.github.liyibo1110.secondkill.support.model.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 17:40
 */
@Mapper(componentModel = "spring")
public interface OrderConverter {

    @Mapping(source = "createTime", target = "createTime")
    OrderDTO toDTO(OrderEntity entity);

    List<OrderDTO> toDTOList(List<OrderEntity> entities);
}
