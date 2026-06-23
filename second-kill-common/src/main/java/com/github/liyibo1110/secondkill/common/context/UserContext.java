package com.github.liyibo1110.secondkill.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 用户上下文，内部使用了阿里的TransmittableThreadLocal（支持线程池场景下的上下文透传）。
 * @author liyibo
 * @date 2026-06-22 14:33
 */
public class UserContext {

    private static final TransmittableThreadLocal<Long> USER_ID_TL = new TransmittableThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_TL.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_TL.get();
    }

    public static void clear() {
        USER_ID_TL.remove();
    }
}
