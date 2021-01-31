package com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author: tk
 * @since: 2021/1/31
 */
public final class ContextUtils {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private static TransmittableThreadLocal<String> ttl = new TransmittableThreadLocal<>();

    public static void set(String s) {
        threadLocal.set(s);
        ttl.set(s);
    }

    public static String getThreadLocal() {
        return threadLocal.get();
    }

    public static String getTtlThreadLocal() {
        return ttl.get();
    }
}
