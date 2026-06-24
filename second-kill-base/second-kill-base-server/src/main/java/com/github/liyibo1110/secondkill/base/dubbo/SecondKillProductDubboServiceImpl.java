package com.github.liyibo1110.secondkill.base.dubbo;

import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillProductDTO;
import com.github.liyibo1110.secondkill.base.converter.SecondKillProductConverter;
import com.github.liyibo1110.secondkill.base.service.SecondKillProductService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:20
 */
@DubboService
@RequiredArgsConstructor
public class SecondKillProductDubboServiceImpl implements SecondKillProductDubboService {

    private final SecondKillProductService secondKillProductService;
    private final SecondKillProductConverter secondKillProductConverter;

    @Override
    public SecondKillProductDTO getBySkuNo(String skuNo) {
        return secondKillProductConverter.toDTO(secondKillProductService.getBySkuNo(skuNo));
    }

    @Override
    public List<SecondKillProductDTO> listByActivityNo(String activityNo) {
        return secondKillProductConverter.toDTOList(secondKillProductService.listByActivityNo(activityNo));
    }

    @Override
    public List<SecondKillActivityProductDTO> listActivityProducts(String activityNo) {
        return secondKillProductConverter.toActivityProductDTOList(
                secondKillProductService.listActivityProducts(activityNo));
    }

    @Override
    public List<SecondKillActivityProductSkuDTO> listActivityProductSkus(String activityNo) {
        return secondKillProductConverter.toActivityProductSkuDTOList(
                secondKillProductService.listActivityProductSkus(activityNo));
    }

    @Override
    public boolean deductStock(String skuNo, Integer quantity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void addActivityProduct(String activityNo, String skuNo, Integer activityStock,
                                   Integer discountType, BigDecimal discountPrice, BigDecimal discountPercent) {
        secondKillProductService.addActivityProduct(activityNo, skuNo, activityStock,
                discountType, discountPrice, discountPercent);
    }

    @Override
    public void removeActivityProduct(String activityNo, String skuNo) {
        secondKillProductService.removeActivityProduct(activityNo, skuNo);
    }
}
