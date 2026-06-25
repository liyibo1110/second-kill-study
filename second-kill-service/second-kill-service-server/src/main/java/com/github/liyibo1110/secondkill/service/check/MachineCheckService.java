package com.github.liyibo1110.secondkill.service.check;

import com.github.liyibo1110.secondkill.base.api.constant.RedisKeyConstants;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import com.github.liyibo1110.secondkill.service.config.SecondKillProperties;
import com.github.liyibo1110.secondkill.service.model.vo.PreCheckVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 机审校验服务，通过随机串+时间戳算法检测请求是否来自真实用户交互，以下是工作原理：
 * 1、前端进入活动页时调用预检接口，服务端生成随机字母串和当前时间戳。
 * 2、服务端在随机串的特定位置（timestamp % length）插入标记字符'z'，得到校验值存入Redis。
 * 3、前端拿到原始随机串和时间戳后，在本地做同样的计算，得到校验值。
 * 4、用户点击抢购时，前端将计算出的校验值带到请求参数中。
 * 5、服务端比对请求中的校验值和Redis中的存储值，一致则通过。
 * 6、校验通过后立即删除Redis中的令牌，保证一次性使用。
 * @author liyibo
 * @date 2026-06-24 13:32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCheckService {

    private final RedisService redisService;
    private final SecondKillProperties secondKillProperties;

    public PreCheckVO generateToken(Long userId) {
        SecondKillProperties.MachineCheck config = secondKillProperties.getMachineCheck();
        int randomLength = config.getRandomLength();
        long timestamp = System.currentTimeMillis();

        // 生成随机字母串
        String random = randomAlphabetic(randomLength);

        // 计算标记字符插入位置
        int pos = (int) (timestamp % randomLength);
        StringBuilder sb = new StringBuilder(random);
        sb.replace(pos, pos + 1, "z");
        String checkValue = sb.toString();

        // 存入Redis
        String redisKey = RedisKeyConstants.machineCheckKey(userId);
        redisService.set(redisKey, checkValue, config.getTtlSeconds(), TimeUnit.SECONDS);

        StructuredLog.info(log)
                .message("机审令牌已生成")
                .put("userId", userId)
                .put("ttl", config.getTtlSeconds())
                .log();

        return new PreCheckVO()
                .setResult(random)
                .setKey(timestamp);
    }

    /**
     * 校验机审令牌，将前端传回的校验值与Redis中存储的值比对，通过后立即删除。
     * @param userId 用户ID
     * @param random 前端计算出的校验值
     */
    public boolean verify(Long userId, String random) {
        if (!secondKillProperties.getMachineCheck().isEnabled())
            return true;

        String redisKey = RedisKeyConstants.machineCheckKey(userId);

        Boolean exists = redisService.hasKey(redisKey);
        if (exists == null || !exists) {
            StructuredLog.warn(log)
                    .message("机审校验失败：令牌不存在或已过期")
                    .put("userId", userId)
                    .log();
            return false;
        }

        String stored = redisService.get(redisKey);
        if (random == null || !random.equals(stored)) {
            StructuredLog.warn(log)
                    .message("机审校验失败：令牌不匹配")
                    .put("userId", userId)
                    .log();
            return false;
        }

        // 用完即删
        redisService.delete(redisKey);

        StructuredLog.info(log)
                .message("机审校验通过")
                .put("userId", userId)
                .log();
        return true;
    }

    /**
     * 清除用户的机审令牌
     */
    public void clearToken(Long userId) {
        redisService.delete(RedisKeyConstants.machineCheckKey(userId));
    }

    /**
     * 生成指定长度的随机大写字母串
     */
    private String randomAlphabetic(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++)
            sb.append((char) ('A' + rnd.nextInt(26)));

        return sb.toString();
    }
}
