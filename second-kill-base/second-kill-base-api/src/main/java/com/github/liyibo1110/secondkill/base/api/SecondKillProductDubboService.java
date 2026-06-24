package com.github.liyibo1110.secondkill.base.api;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillProductDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品相关的Dubbo接口。
 * @author liyibo
 * @date 2026-06-23 15:43
 */
public interface SecondKillProductDubboService {

    SecondKillProductDTO getBySkuNo(String skuNo);

    List<SecondKillProductDTO> listByActivityNo(String activityNo);

    List<SecondKillActivityProductDTO> listActivityProducts(String activityNo);

    List<SecondKillActivityProductSkuDTO> listActivityProductSkus(String activityNo);

    boolean deductStock(String skuNo, Integer quantity);

    void addActivityProduct(String activityNo, String skuNo, Integer activityStock,
                            Integer discountType, BigDecimal discountPrice, BigDecimal discountPercent);

    void removeActivityProduct(String activityNo, String skuNo);
}
