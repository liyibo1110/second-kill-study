package com.github.liyibo1110.secondkill.admin.service;

import com.github.liyibo1110.secondkill.admin.model.command.AddProductCommand;
import com.github.liyibo1110.secondkill.admin.model.command.CreateActivityCommand;
import com.github.liyibo1110.secondkill.admin.model.command.UpdateActivityCommand;
import com.github.liyibo1110.secondkill.admin.model.query.ActivityPageQuery;
import com.github.liyibo1110.secondkill.admin.model.vo.ActivityDetailVO;
import com.github.liyibo1110.secondkill.admin.model.vo.ActivityListVO;
import com.github.liyibo1110.secondkill.admin.model.vo.ActivityProductVO;
import com.github.liyibo1110.secondkill.base.api.SecondKillActivityDubboService;
import com.github.liyibo1110.secondkill.base.api.SecondKillProductDubboService;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductSkuDTO;
import com.github.liyibo1110.secondkill.common.exception.BizException;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台活动服务，提供活动的CRUD操作，通过Dubbo调用second-kill-base读写数据。
 * 注意这个管理后台不会直接操作数据库，所有数据操作都委托给second-kill-base。
 * @author liyibo
 * @date 2026-06-26 10:43
 */
@Slf4j
@Service
public class ActivityAdminService {

    @DubboReference
    private SecondKillActivityDubboService activityDubboService;

    @DubboReference
    private SecondKillProductDubboService productDubboService;

    public List<ActivityListVO> listActivities(ActivityPageQuery query) {
        List<SecondKillActivityDTO> activities;
        if (query.getActivityStatus() != null) {
            activities = activityDubboService.listByStatus(query.getActivityStatus());
        } else {
            // 查询所有非终态的活动
            List<SecondKillActivityDTO> upcoming = activityDubboService.listByStatus(0);
            List<SecondKillActivityDTO> active = activityDubboService.listByStatus(1);
            List<SecondKillActivityDTO> ended = activityDubboService.listByStatus(2);
            activities = new ArrayList<>();
            activities.addAll(upcoming);
            activities.addAll(active);
            activities.addAll(ended);
        }

        return activities.stream().map(dto -> {
            List<SecondKillActivityProductSkuDTO> products = productDubboService.listActivityProductSkus(dto.getActivityNo());
            int productCount = (products != null) ? products.size() : 0;

            return ActivityListVO.builder()
                    .activityNo(dto.getActivityNo())
                    .activityName(dto.getActivityName())
                    .activityStatus(dto.getActivityStatus())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .purchaseLimit(dto.getPurchaseLimit())
                    .productCount(productCount)
                    .createTime(dto.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    public ActivityDetailVO getActivityDetail(String activityNo) {
        SecondKillActivityDTO dto = activityDubboService.getByActivityNo(activityNo);
        if (dto == null)
            throw new BizException("data_not_found", "活动不存在: " + activityNo);

        List<SecondKillActivityProductSkuDTO> skuList = productDubboService.listActivityProductSkus(activityNo);
        List<ActivityProductVO> products = new ArrayList<>();
        if (skuList != null) {
            for (SecondKillActivityProductSkuDTO sku : skuList) {
                products.add(ActivityProductVO.builder()
                        .skuNo(sku.getSkuNo())
                        .activityStock(sku.getActivityStock())
                        .discountType(sku.getDiscountType())
                        .discountPrice(sku.getDiscountPrice())
                        .discountPercent(sku.getDiscountPercent())
                        .build());
            }
        }

        return ActivityDetailVO.builder()
                .activityNo(dto.getActivityNo())
                .activityName(dto.getActivityName())
                .activityStatus(dto.getActivityStatus())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purchaseLimit(dto.getPurchaseLimit())
                .remark(dto.getRemark())
                .products(products)
                .createTime(dto.getCreateTime())
                .build();
    }

    public String createActivity(CreateActivityCommand command) {
        if (!command.getEndTime().isAfter(command.getStartTime()))
            throw new BizException("param_invalid_value", "结束时间必须晚于开始时间");

        if (command.getStartTime().isBefore(LocalDateTime.now()))
            throw new BizException("param_invalid_value", "开始时间不能早于当前时间");

        SecondKillActivityDTO dto = new SecondKillActivityDTO();
        dto.setActivityName(command.getActivityName());
        dto.setStartTime(command.getStartTime());
        dto.setEndTime(command.getEndTime());
        dto.setPurchaseLimit(command.getPurchaseLimit());
        dto.setRemark(command.getRemark());
        dto.setActivityStatus(0);

        String activityNo = activityDubboService.createActivity(dto);

        StructuredLog.info(log)
                .message("活动创建成功")
                .put("activityNo", activityNo)
                .put("activityName", command.getActivityName())
                .log();

        return activityNo;
    }

    public void updateActivity(UpdateActivityCommand command) {
        SecondKillActivityDTO existing = activityDubboService.getByActivityNo(command.getActivityNo());
        if (existing == null)
            throw new BizException("data_not_found", "活动不存在: " + command.getActivityNo());

        if (existing.getActivityStatus() != null && existing.getActivityStatus() == 2)
            throw new BizException("operation_forbidden", "已结束的活动不允许修改");

        SecondKillActivityDTO dto = new SecondKillActivityDTO();
        dto.setActivityNo(command.getActivityNo());
        dto.setActivityName(command.getActivityName());
        dto.setStartTime(command.getStartTime());
        dto.setEndTime(command.getEndTime());
        dto.setPurchaseLimit(command.getPurchaseLimit());
        dto.setRemark(command.getRemark());

        activityDubboService.updateActivity(dto);

        StructuredLog.info(log)
                .message("活动更新成功")
                .put("activityNo", command.getActivityNo())
                .log();
    }

    public void endActivity(String activityNo) {
        SecondKillActivityDTO existing = activityDubboService.getByActivityNo(activityNo);
        if (existing == null)
            throw new BizException("data_not_found", "活动不存在: " + activityNo);

        if (existing.getActivityStatus() != null && existing.getActivityStatus() == 2)
            throw new BizException("operation_forbidden", "活动已经结束");

        activityDubboService.updateActivityStatus(activityNo, 2);

        StructuredLog.info(log)
                .message("活动手动结束")
                .put("activityNo", activityNo)
                .log();
    }

    public void addProduct(AddProductCommand command) {
        SecondKillActivityDTO existing = activityDubboService.getByActivityNo(command.getActivityNo());
        if (existing == null)
            throw new BizException("data_not_found", "活动不存在: " + command.getActivityNo());

        if (existing.getActivityStatus() != null && existing.getActivityStatus() == 2)
            throw new BizException("operation_forbidden", "已结束的活动不允许添加商品");

        productDubboService.addActivityProduct(
                command.getActivityNo(),
                command.getSkuNo(),
                command.getActivityStock(),
                command.getDiscountType(),
                command.getDiscountPrice(),
                command.getDiscountPercent());

        StructuredLog.info(log)
                .message("活动商品添加成功")
                .put("activityNo", command.getActivityNo())
                .put("skuNo", command.getSkuNo())
                .put("activityStock", command.getActivityStock())
                .log();
    }

    public void removeProduct(String activityNo, String skuNo) {
        SecondKillActivityDTO existing = activityDubboService.getByActivityNo(activityNo);
        if (existing == null)
            throw new BizException("data_not_found", "活动不存在: " + activityNo);

        if (existing.getActivityStatus() != null && existing.getActivityStatus() == 1)
            throw new BizException("operation_forbidden", "进行中的活动不允许移除商品");

        productDubboService.removeActivityProduct(activityNo, skuNo);

        StructuredLog.info(log)
                .message("活动商品移除成功")
                .put("activityNo", activityNo)
                .put("skuNo", skuNo)
                .log();
    }
}
