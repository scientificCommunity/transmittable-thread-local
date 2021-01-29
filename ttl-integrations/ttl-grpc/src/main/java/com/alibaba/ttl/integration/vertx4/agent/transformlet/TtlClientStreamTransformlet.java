package com.alibaba.ttl.integration.vertx4.agent.transformlet;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**{@link TtlTransformlet} for {@link io.grpc.internal.AbstractClientStream} and {@link io.grpc.internal.DelayedStream}.
 *
 * @author: tk (305809299 at qq dot com)
 * @since: 2021/1/27
 */
public class TtlClientStreamTransformlet extends BaseTtlTransformlet {
    private static final Logger LOGGER = Logger.getLogger(TtlClientStreamTransformlet.class);
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
            LOGGER.warn("load class failed in TtlClientStreamTransformlet. 【className:" + TTL_STREAM_LISTENER_CLASS_NAME + "】" +
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
