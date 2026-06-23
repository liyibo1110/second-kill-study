package com.github.liyibo1110.secondkill.common.id;

import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * 基于Snowflake算法的分布式ID生成器，因为订单表做了分片（userId % 4），自增主键在分片场景下会冲突。
 * 注意这个组件只提供了通用的ID生成能力，不涉及业务前缀的拼接，因为这属于业务层面的约定（这个是在base模块中的BizIdType里面定义）。
 * @author liyibo
 * @date 2026-06-22 16:02
 */
@Slf4j
public class IdGenerator {

    /** 起始时间戳 (2024-01-01 00:00:00) */
    private static final long EPOCH = 1704067200000L;

    /** 机器ID占10位，通常划分为数据中心ID（5位）和机器ID（5位），支持1024台机器 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号占12位，1毫秒内的计数，支持4096个ID/每毫秒/每机器 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器ID左移12位 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移22位 (10 + 12) */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 序列号掩码4095 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器ID掩码1023 */
    private static final long WORKER_MASK = ~(-1L << WORKER_ID_BITS);

    private static long workerId;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    static {
        String workerMark = null;
        try {
            /**
             * workerId的获取策略是：
             * 1、取本机IP地址的hashCode，再对1024取模。
             * 2、在容器化部署环境种，每个Pod的IP不同，workerId自然也不同。
             * 3、这种方式不依赖外部注册中心，实现简单，对于秒杀系统这种实例数不会很多的场景足够用了。
             */
            workerMark = InetAddress.getLocalHost().getHostAddress();
            workerId = workerMark.hashCode() & WORKER_MASK;
        } catch (UnknownHostException e) {
            StructuredLog.error(log)
                    .message("获取本机IP失败")
                    .exception(e)
                    .log();
            workerMark = System.currentTimeMillis() + UUID.randomUUID().toString();
            workerId = workerMark.hashCode() & WORKER_MASK;
        } finally {
            StructuredLog.info(log)
                    .message("工作节点初始化完成")
                    .put("workerMark", workerMark)
                    .put("workerId", workerId)
                    .log();
        }
    }

    private IdGenerator() {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成纯数字雪花 ID
     */
    public static long nextId() {
        return generateId();
    }

    /**
     * 生成 UUID（去除中划线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp)
            throw new RuntimeException(String.format("时钟回拨，拒绝生成ID %d 毫秒", lastTimestamp - timestamp));

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0)
                timestamp = tilNextMillis(lastTimestamp);
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
            timestamp = System.currentTimeMillis();

        return timestamp;
    }
}
