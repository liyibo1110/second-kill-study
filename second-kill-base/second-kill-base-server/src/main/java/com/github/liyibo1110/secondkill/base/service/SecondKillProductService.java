package com.github.liyibo1110.secondkill.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductSkuEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillProductEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:05
 */
public interface SecondKillProductService extends IService<SecondKillProductEntity> {

    SecondKillProductEntity getBySkuNo(String skuNo);

    List<SecondKillProductEntity> listByActivityNo(String activityNo);

    List<SecondKillActivityProductEntity> listActivityProducts(String activityNo);

    List<SecondKillActivityProductSkuEntity> listActivityProductSkus(String activityNo);

    void addActivityProduct(String activityNo, String skuNo, Integer activityStock,
                            Integer discountType, BigDecimal discountPrice, BigDecimal discountPercent);

    void removeActivityProduct(String activityNo, String skuNo);
}
