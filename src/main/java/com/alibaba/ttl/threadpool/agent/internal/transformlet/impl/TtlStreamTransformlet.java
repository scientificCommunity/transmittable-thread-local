package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * @author: tk
 * @since: 2021/1/27
 */
public class TtlStreamTransformlet extends BaseTtlTransformlet {
    private static final Logger LOGGER = Logger.getLogger(TtlVertxFutureTransformlet.class);

    private static final String GRPC_CALLBACK_INVOKE_CLASS_NAME = "io.grpc.internal.AbstractClientStream";
    private static final String STREAM_LISTENER_CLASS_NAME = "io.grpc.internal.ClientStreamListener";
    private static final String TTL_STREAM_LISTENER_CLASS_NAME = "com.alibaba.ttl.ext.TtlGrpcStreamListener";

    private static final String DECORATE_SET_HANDLER_METHOD = "setHandler";

    static {
        CALL_CLASS_NAMES.add(GRPC_CALLBACK_INVOKE_CLASS_NAME);

        PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.put(STREAM_LISTENER_CLASS_NAME, TTL_STREAM_LISTENER_CLASS_NAME);

        DECORATE_METHODS_NAME.add(DECORATE_SET_HANDLER_METHOD);
    }

    @Override
    protected void loadClass() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Method findClass = URLClassLoader.class.getDeclaredMethod("findClass", String.class);
            findClass.setAccessible(true);
            findClass.invoke(contextClassLoader, TTL_STREAM_LISTENER_CLASS_NAME);
        } catch (Throwable t) {
            LOGGER.info("load class failed in TtlFutureTransformlet. 【className:" + TTL_STREAM_LISTENER_CLASS_NAME + "】" +
                "cause:" + t.getMessage());
        }
    }

    @Override
    protected boolean needDecorateToTtlWrapper(String methodName) {
        return DECORATE_METHODS_NAME.contains(methodName);
    }
}
