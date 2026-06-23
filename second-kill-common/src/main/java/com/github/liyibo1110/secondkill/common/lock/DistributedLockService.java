package com.github.liyibo1110.secondkill.common.lock;

import com.github.liyibo1110.secondkill.common.exception.BizException;
import com.github.liyibo1110.secondkill.common.exception.ErrorEnum;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务，基于Redisson封装，提供了编程式的API。
 * @author liyibo
 * @date 2026-06-22 16:11
 */
@Slf4j
public class DistributedLockService {

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取锁，并执行业务逻辑
     * @param lockKey   锁的key
     * @param waitTime  等待获取锁的最大时间
     * @param leaseTime 锁的持有时间（自动续期请传 -1）
     * @param unit      时间单位
     * @param supplier  获取锁成功后执行的业务逻辑
     * @return 业务逻辑执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            // 尝试抢锁
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                StructuredLog.warn(log)
                        .message("获取分布式锁失败")
                        .put("lockKey", lockKey)
                        .log();
                throw BizException.of(ErrorEnum.FETCH_LOCK_FAILED);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BizException.of(ErrorEnum.FETCH_LOCK_FAILED);
        } finally {
            // 在这里会释放锁，不需要调用方自己来释放，注意这个isHeldByCurrentThread判断不能漏，如果当前线程等待超时没有拿到锁，直接调unlock会抛IllegalMonitorStateException
            if (acquired && lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * 尝试获取锁并执行（无返回值）
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, unit, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 使用默认超时时间获取锁并执行（等待 3 秒，持有 30 秒）
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, supplier);
    }

    /**
     * 使用默认超时时间获取锁并执行（无返回值）
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, runnable);
    }
}
