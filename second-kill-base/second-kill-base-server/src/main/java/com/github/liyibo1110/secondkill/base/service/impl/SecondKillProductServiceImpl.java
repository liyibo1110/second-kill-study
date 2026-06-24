package com.github.liyibo1110.secondkill.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillActivityProductMapper;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillActivityProductSkuMapper;
import com.github.liyibo1110.secondkill.base.mapper.SecondKillProductMapper;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillActivityProductSkuEntity;
import com.github.liyibo1110.secondkill.base.model.entity.SecondKillProductEntity;
import com.github.liyibo1110.secondkill.base.service.SecondKillProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-06-23 16:10
 */
@Service
@RequiredArgsConstructor
public class SecondKillProductServiceImpl extends ServiceImpl<SecondKillProductMapper, SecondKillProductEntity>
        implements SecondKillProductService {

    private final SecondKillActivityProductMapper activityProductMapper;
    private final SecondKillActivityProductSkuMapper activityProductSkuMapper;

    @Override
    public SecondKillProductEntity getBySkuNo(String skuNo) {
        return getOne(new LambdaQueryWrapper<SecondKillProductEntity>()
                .eq(SecondKillProductEntity::getSkuNo, skuNo));
    }

    @Override
    public List<SecondKillProductEntity> listByActivityNo(String activityNo) {
        return list(new LambdaQueryWrapper<SecondKillProductEntity>()
                .eq(SecondKillProductEntity::getActivityNo, activityNo));
    }

    @Override
    public List<SecondKillActivityProductEntity> listActivityProducts(String activityNo) {
        return activityProductMapper.selectList(
                new LambdaQueryWrapper<SecondKillActivityProductEntity>()
                        .eq(SecondKillActivityProductEntity::getActivityNo, activityNo)
                        .orderByAsc(SecondKillActivityProductEntity::getSortOrder));
    }

    @Override
    public List<SecondKillActivityProductSkuEntity> listActivityProductSkus(String activityNo) {
        return activityProductSkuMapper.selectList(
                new LambdaQueryWrapper<SecondKillActivityProductSkuEntity>()
                        .eq(SecondKillActivityProductSkuEntity::getActivityNo, activityNo));
    }

    @Override
    public void addActivityProduct(String activityNo, String skuNo, Integer activityStock,
                                   Integer discountType, BigDecimal discountPrice, BigDecimal discountPercent) {
        SecondKillActivityProductSkuEntity skuEntity = new SecondKillActivityProductSkuEntity();
        skuEntity.setActivityNo(activityNo);
        skuEntity.setSkuNo(skuNo);
        skuEntity.setActivityStock(activityStock);
        skuEntity.setDiscountType(discountType);
        skuEntity.setDiscountPrice(discountPrice);
        skuEntity.setDiscountPercent(discountPercent);
        activityProductSkuMapper.insert(skuEntity);
    }

    @Override
    public void removeActivityProduct(String activityNo, String skuNo) {
        activityProductSkuMapper.delete(
                new LambdaQueryWrapper<SecondKillActivityProductSkuEntity>()
                        .eq(SecondKillActivityProductSkuEntity::getActivityNo, activityNo)
                        .eq(SecondKillActivityProductSkuEntity::getSkuNo, skuNo));
    }
}
