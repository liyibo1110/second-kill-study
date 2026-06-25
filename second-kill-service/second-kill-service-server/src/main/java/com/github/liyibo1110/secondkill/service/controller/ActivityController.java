package com.github.liyibo1110.secondkill.service.controller;

import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityDTO;
import com.github.liyibo1110.secondkill.base.api.dto.SecondKillActivityProductDTO;
import com.github.liyibo1110.secondkill.common.result.Result;
import com.github.liyibo1110.secondkill.service.activity.ActivityQueryService;
import com.github.liyibo1110.secondkill.service.converter.ActivityConverter;
import com.github.liyibo1110.secondkill.service.model.vo.ActivityDetailVO;
import com.github.liyibo1110.secondkill.service.model.vo.ActivityListVO;
import com.github.liyibo1110.secondkill.service.model.vo.ProductVO;
import com.github.liyibo1110.secondkill.service.product.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动查询接口。
 * @author liyibo
 * @date 2026-06-24 15:00
 */
@RestController
@RequestMapping("/api/secondkill/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityQueryService activityQueryService;
    private final ProductQueryService productQueryService;
    private final ActivityConverter activityConverter;

    /**
     * 查询活动详情（含商品列表）
     */
    @GetMapping("/{activityNo}")
    public Result<ActivityDetailVO> getDetail(@PathVariable String activityNo) {
        SecondKillActivityDTO activity = activityQueryService.getActivity(activityNo);
        if (activity == null)
            return Result.fail("活动不存在");

        ActivityDetailVO vo = activityConverter.toDetailVO(activity);
        vo.setActivityOpen(activityQueryService.isActivityOpen(activity));

        List<SecondKillActivityProductDTO> products = productQueryService.listActivityProducts(activityNo);
        List<ProductVO> productVOList = activityConverter.toProductVOList(products);
        vo.setProducts(productVOList);

        return Result.success(vo);
    }

    /**
     * 查询进行中的活动列表
     */
    @GetMapping("/active")
    public Result<List<ActivityListVO>> listActive() {
        List<SecondKillActivityDTO> activities = activityQueryService.listActiveActivities();
        List<ActivityListVO> voList = activityConverter.toListVOList(activities);
        return Result.success(voList);
    }
}
