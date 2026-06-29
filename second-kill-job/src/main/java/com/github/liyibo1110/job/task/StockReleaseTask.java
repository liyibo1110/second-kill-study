package com.github.liyibo1110.job.task;

import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存释放任务，活动结束后，清理Redis中的库存Key和限购Key。
 * 已结束的活动不再接受请求，这些Key留在Redis里只是浪费内存。
 * 建议执行频率：每10分钟一次。
 * @author liyibo
 * @date 2026-06-29 10:22
 */
@Slf4j
@Component
public class StockReleaseTask {

    private final RedisService redisService;

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public StockReleaseTask(RedisService redisService) {
        this.redisService = redisService;
    }

    @XxlJob("stockRelease")
    public void execute() {
        // 已结束的活动
        List<SecondKillActivityDTO> ended = activityDubboService.listByStatus(2);
        if (ended == null || ended.isEmpty())
            return;

        for (SecondKillActivityDTO activity : ended) {
            try {
                releaseActivityKeys(activity.getActivityNo());
            } catch (Exception e) {
                StructuredLog.error(log)
                        .message("活动Key释放失败")
                        .put("activityNo", activity.getActivityNo())
                        .exception(e)
                        .log();
            }
        }
    }

    private void releaseActivityKeys(String activityNo) {
        int deletedCount = 0;

        // 删除活动信息缓存
        redisService.delete(RedisKeyConstants.ACTIVITY_INFO + activityNo);
        deletedCount++;

        // 删除商品列表缓存
        redisService.delete(RedisKeyConstants.ACTIVITY_PRODUCT_LIST + activityNo);
        deletedCount++;

        // 删除每个SKU的库存Key
        List<SecondKillActivityProductSkuDTO> skuList = productDubboService.listActivityProductSkus(activityNo);
        if (skuList != null) {
            for (SecondKillActivityProductSkuDTO sku : skuList) {
                String stockKey = RedisKeyConstants.skuStockKey(Long.valueOf(activityNo), sku.getProductId());
                redisService.delete(stockKey);
                deletedCount++;
            }
        }

        // 删除活动状态缓存
        redisService.delete(RedisKeyConstants.ACTIVITY_STATUS + activityNo);
        deletedCount++;

        StructuredLog.info(log)
                .message("活动Redis Key已释放")
                .put("activityNo", activityNo)
                .put("deletedCount", deletedCount)
                .log();
    }
}
