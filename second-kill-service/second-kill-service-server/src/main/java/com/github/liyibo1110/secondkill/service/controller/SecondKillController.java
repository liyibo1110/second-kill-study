package com.github.liyibo1110.secondkill.service.controller;

import com.github.liyibo1110.secondkill.common.context.UserContext;
import com.github.liyibo1110.secondkill.common.exception.ErrorEnum;
import com.github.liyibo1110.secondkill.common.result.Result;
import com.github.liyibo1110.secondkill.service.check.MachineCheckService;
import com.github.liyibo1110.secondkill.service.entry.SecondKillEntryService;
import com.github.liyibo1110.secondkill.service.model.request.QueueCheckRequest;
import com.github.liyibo1110.secondkill.service.model.request.SecondKillPartInRequest;
import com.github.liyibo1110.secondkill.service.model.vo.PreCheckVO;
import com.github.liyibo1110.secondkill.service.model.vo.QueueCheckVO;
import com.github.liyibo1110.secondkill.service.model.vo.SecondKillPartInVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀操作接口。
 * @author liyibo
 * @date 2026-06-24 15:03
 */
@RestController
@RequestMapping("/api/secondkill")
@RequiredArgsConstructor
public class SecondKillController {

    private final MachineCheckService machineCheckService;
    private final SecondKillEntryService secondKillEntryService;

    /**
     * 预检接口：获取机审令牌
     */
    @GetMapping("/pre-check")
    public Result<PreCheckVO> preCheck() {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.fail(ErrorEnum.NOT_LOGIN);

        PreCheckVO vo = machineCheckService.generateToken(userId);
        return Result.success(vo);
    }

    /**
     * 秒杀下单入口
     */
    @PostMapping("/part-in")
    public Result<SecondKillPartInVO> partIn(@RequestBody SecondKillPartInRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.fail(ErrorEnum.NOT_LOGIN);

        SecondKillPartInVO vo = secondKillEntryService.partIn(request);
        return Result.success(vo);
    }

    /**
     * 排队结果轮询
     */
    @PostMapping("/queue/check")
    public Result<QueueCheckVO> checkQueue(@RequestBody QueueCheckRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.fail(ErrorEnum.NOT_LOGIN);

        QueueCheckVO vo = secondKillEntryService.checkQueue(request.getTraceId(), userId);
        return Result.success(vo);
    }
}
