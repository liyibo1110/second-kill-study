package com.github.liyibo1110.secondkill.service.converter;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.service.model.vo.ActivityDetailVO;
import com.github.liyibo1110.secondkill.service.model.vo.ActivityListVO;
import com.github.liyibo1110.secondkill.service.model.vo.ProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-24 14:09
 */
@Mapper(componentModel = "spring")
public interface ActivityConverter {

    @Mapping(target = "activityOpen", ignore = true)
    @Mapping(target = "products", ignore = true)
    ActivityDetailVO toDetailVO(SecondKillActivityDTO dto);

    ActivityListVO toListVO(SecondKillActivityDTO dto);

    List<ActivityListVO> toListVOList(List<SecondKillActivityDTO> dtoList);

    ProductVO toProductVO(SecondKillActivityProductDTO dto);

    List<ProductVO> toProductVOList(List<SecondKillActivityProductDTO> dtoList);
}
