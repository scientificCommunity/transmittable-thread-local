package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: tk
 * @since: 2021/1/27
 */
public class TtlStreamTransformlet extends BaseTtlTransformlet {
    private static final Logger LOGGER = Logger.getLogger(TtlStreamTransformlet.class);
    private static final Set<String> CALL_CLASS_NAMES = new HashSet<String>();
    private static final Map<String, String> PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS = new HashMap<String, String>();
    private static final Set<String> DECORATE_METHODS_NAME = new HashSet<String>();

    private static final String GRPC_CALLBACK_INVOKE_CLASS_NAME = "io.grpc.internal.AbstractClientStream";
    private static final String STREAM_LISTENER_CLASS_NAME = "io.grpc.internal.ClientStreamListener";
    private static final String TTL_STREAM_LISTENER_CLASS_NAME = "com.alibaba.ttl.TtlGrpcClientStreamListener";

    private static final String GRPC_DELAY_STREAM_INVOKE_CLASS_NAME = "io.grpc.internal.DelayedStream";

    static {
        CALL_CLASS_NAMES.add(GRPC_CALLBACK_INVOKE_CLASS_NAME);
        CALL_CLASS_NAMES.add(GRPC_DELAY_STREAM_INVOKE_CLASS_NAME);

        PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.put(STREAM_LISTENER_CLASS_NAME, TTL_STREAM_LISTENER_CLASS_NAME);
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
    protected Set<String> getCallClassNames() {
        return CALL_CLASS_NAMES;
    }

    @Override
    protected Set<String> getDecorateMethodsName() {
        return DECORATE_METHODS_NAME;
    }

    @Override
    protected Map<String, String> getParamTypeNameToDecorateMethodClass() {
        return PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS;
    }
}
