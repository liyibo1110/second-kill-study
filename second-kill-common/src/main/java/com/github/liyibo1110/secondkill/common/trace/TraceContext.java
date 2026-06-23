package com.github.liyibo1110.secondkill.common.trace;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 链路追踪上下文，内部使用了阿里的TransmittableThreadLocal（支持线程池场景下的上下文透传）。
 *
 * 在这里介绍一下TransmittableThreadLocal：
 * 1、普通ThreadLocal在父线程里面set的值，子线程拿不到。
 * 2、业务代码里经常用的@Async、CompletableFuture、线程池等，去做异步操作，如果用普通的ThreadLocal，异步线程里拿不到traceId和userId，日志和审计数据就会断掉。
 * 3、TransmittableThreadLocal能自动把父线程的上下文，透传到子线程，前提是线程池需要使用TTL提供的TtlExecutors再包装一下。
 * @author liyibo
 * @date 2026-06-22 14:12
 */
public class TraceContext {

    private static final TransmittableThreadLocal<String> TRACE_ID_TL = new TransmittableThreadLocal<>();

    public static void setTraceId(String traceId) {
        TRACE_ID_TL.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID_TL.get();
    }

    public static void clear() {
        TRACE_ID_TL.remove();
    }
}
