package com.github.liyibo1110.secondkill.admin.controller;

import com.github.liyibo1110.secondkill.admin.model.command.AddProductCommand;
import com.github.liyibo1110.secondkill.admin.model.command.CreateActivityCommand;
import com.github.liyibo1110.secondkill.admin.model.command.UpdateActivityCommand;
import com.github.liyibo1110.secondkill.admin.model.query.ActivityPageQuery;
import com.github.liyibo1110.secondkill.admin.model.vo.ActivityDetailVO;
import com.github.liyibo1110.secondkill.admin.model.vo.ActivityListVO;
import com.github.liyibo1110.secondkill.admin.service.ActivityAdminService;
import com.github.liyibo1110.secondkill.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 活动管理入口。
 * @author liyibo
 * @date 2026-06-26 10:57
 */
@RestController
@RequestMapping("/admin/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityAdminService activityAdminService;

    @GetMapping("/list")
    public Result<List<ActivityListVO>> list(ActivityPageQuery query) {
        return Result.success(activityAdminService.listActivities(query));
    }

    @GetMapping("/{activityNo}")
    public Result<ActivityDetailVO> detail(@PathVariable String activityNo) {
        return Result.success(activityAdminService.getActivityDetail(activityNo));
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody CreateActivityCommand command) {
        String activityNo = activityAdminService.createActivity(command);
        return Result.success(activityNo);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody UpdateActivityCommand command) {
        activityAdminService.updateActivity(command);
        return Result.success(null);
    }

    @PostMapping("/{activityNo}/end")
    public Result<Void> end(@PathVariable String activityNo) {
        activityAdminService.endActivity(activityNo);
        return Result.success(null);
    }

    @PostMapping("/product")
    public Result<Void> addProduct(@Valid @RequestBody AddProductCommand command) {
        activityAdminService.addProduct(command);
        return Result.success(null);
    }

    @DeleteMapping("/product")
    public Result<Void> removeProduct(@RequestParam String activityNo,
                                      @RequestParam String skuNo) {
        activityAdminService.removeProduct(activityNo, skuNo);
        return Result.success(null);
    }
}
