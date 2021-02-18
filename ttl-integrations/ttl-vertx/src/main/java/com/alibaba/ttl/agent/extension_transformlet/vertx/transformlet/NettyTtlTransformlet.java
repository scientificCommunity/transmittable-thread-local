package com.alibaba.ttl.agent.extension_transformlet.vertx.transformlet;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CannotCompileException;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtClass;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtMethod;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.NotFoundException;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;

/**
 * {@link TtlTransformlet} for {@link io.vertx.core.Future}.
 *
 * @author tk (305809299 at qq dot com)
 * @see com.alibaba.ttl.integration.vertx4.TtlVertxHandler
 * @see io.vertx.core.Future
 * @see io.vertx.core.Handler
 * @see sun.misc.Launcher.AppClassLoader
 * @see URLClassLoader#findClass(String)
 */
public class NettyTtlTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(NettyTtlTransformlet.class);

    /**
     * netty
     */
    private static final String NETTY_INVOKE_CLASS_NAME = "io.netty.channel.DefaultChannelPipeline";
    private static final String NETTY_LISTENER_INVOKE_CLASS_NAME = "io.netty.channel.DefaultChannelPromise";

    private static final String NETTY_CHANNEL_HANDLER_CLASS_NAME = "io.netty.channel.ChannelHandler";
    private static final String TTL_NETTY_CHANNEL_HANDLER_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.vertx.TtlNetty1Handler";

    private static final String NETTY_GENERIC_FUTURE_LISTENER_CLASS_NAME = "io.netty.util.concurrent.GenericFutureListener";
    private static final String TTL_NETTY_GENERIC_FUTURE_LISTENER_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.vertx.TtlGenericFutureListener";

    private static final String TTL_NETTY_HANDLER_UTIL_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.vertx.TtlWrapHandlerUtils";

    private static final Set<String> TO_BE_TRANSFORMED_CLASS_NAMES = new HashSet<>();
    private static final Map<String, String> TO_BE_WRAPPED_CLASS_NAMES = new HashMap<>();

    static {
        TO_BE_TRANSFORMED_CLASS_NAMES.add(NETTY_INVOKE_CLASS_NAME);
        TO_BE_TRANSFORMED_CLASS_NAMES.add(NETTY_LISTENER_INVOKE_CLASS_NAME);

        TO_BE_WRAPPED_CLASS_NAMES.put(NETTY_CHANNEL_HANDLER_CLASS_NAME, TTL_NETTY_HANDLER_UTIL_CLASS_NAME);
        TO_BE_WRAPPED_CLASS_NAMES.put(NETTY_GENERIC_FUTURE_LISTENER_CLASS_NAME, TTL_NETTY_GENERIC_FUTURE_LISTENER_CLASS_NAME);
    }

    @Override
    public void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException {
        final CtClass clazz = classInfo.getCtClass();
        if (TO_BE_TRANSFORMED_CLASS_NAMES.contains(classInfo.getClassName())) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }
            classInfo.setModified();
        }
    }

    private void updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!checkMethodNeedToBeDecorated(modifiers)) {
            return;
        }

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (TO_BE_WRAPPED_CLASS_NAMES.containsKey(paramTypeName)) {
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "$%d = %s.get($%1$d, false, true);"
                        + "\n    com.alibaba.ttl.spi.TtlAttachmentsDelegate.setAutoWrapperAttachment($%1$d);",
                    i + 1, TO_BE_WRAPPED_CLASS_NAMES.get(paramTypeName));
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ":\n" + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) method.insertBefore(insertCode.toString());
    }

    private boolean checkMethodNeedToBeDecorated(int modifiers) {
        return Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isAbstract(modifiers);
    }
}
