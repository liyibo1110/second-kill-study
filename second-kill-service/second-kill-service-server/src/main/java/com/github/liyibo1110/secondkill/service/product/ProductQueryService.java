package com.github.liyibo1110.secondkill.service.product;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.cache.TwoLevelCache;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 商品查询服务，商品列表走两级缓存，库存只走Redis。
 * @author liyibo
 * @date 2026-06-24 13:01
 */
@Service
public class ProductQueryService {

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    @Resource
    private RedisService redisService;

    @Resource
    private ObjectMapper objectMapper;

    private TwoLevelCache<List<SecondKillActivityProductDTO>> activityProductCache;

    @PostConstruct
    public void init() {
        // 活动商品列表缓存：本地30分钟硬过期，5秒刷新间隔，Redis 30分钟
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, SecondKillActivityProductDTO.class);
        this.activityProductCache = new TwoLevelCache<>(
                200, 1800, 5,
                redisService, objectMapper,
                RedisKeyConstants.ACTIVITY_PRODUCT_LIST, 1800, 60,
                type,
                productDubboService::listActivityProducts);
    }

    /**
     * 查询活动的商品列表（走两级缓存）
     */
    public List<SecondKillActivityProductDTO> listActivityProducts(String activityNo) {
        List<SecondKillActivityProductDTO> list = activityProductCache.get(activityNo);
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 查询SKU库存（只走Redis，不走本地缓存）
     *
     * 库存是秒杀中变化最快的数据，每笔订单都会扣减Redis中的库存值。
     * 如果走本地缓存，Caffeine中缓存的库存可能滞后数秒，
     * 导致用户看到已售罄的商品仍显示有库存。
     */
    public Integer getSkuStock(Long activityId, Long skuId) {
        String stockKey = RedisKeyConstants.skuStockKey(activityId, skuId);
        String stock = redisService.get(stockKey);
        if (stock == null)
            return 0;

        return Integer.parseInt(stock);
    }
}
