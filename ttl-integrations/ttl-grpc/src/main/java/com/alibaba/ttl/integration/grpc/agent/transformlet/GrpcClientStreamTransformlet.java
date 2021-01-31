package com.alibaba.ttl.integration.grpc.agent.transformlet;

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
import java.util.HashSet;
import java.util.Set;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;

/**
 * {@link TtlTransformlet} for {@link io.grpc.internal.AbstractClientStream} and {@link io.grpc.internal.DelayedStream}.
 *
 * @author tk (305809299 at qq dot com)
 * @see io.grpc.stub.ClientCalls
 * @see io.grpc.internal.DelayedStream
 * @see io.grpc.internal.AbstractClientStream
 * @see io.grpc.internal.ClientStreamListener
 */
public class GrpcClientStreamTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(GrpcClientStreamTransformlet.class);

    private static final String GRPC_CALLBACK_INVOKE_CLASS_NAME = "io.grpc.internal.AbstractClientStream";
    private static final String STREAM_LISTENER_CLASS_NAME = "io.grpc.internal.ClientStreamListener";
    private static final String TTL_STREAM_LISTENER_CLASS_NAME = "com.alibaba.ttl.TtlGrpcClientStreamListener";

    private static final String GRPC_DELAY_STREAM_INVOKE_CLASS_NAME = "io.grpc.internal.DelayedStream";

    private static final Set<String> TO_BE_TRANSFORM_CLASS_NAMES = new HashSet<String>();

    static {
        TO_BE_TRANSFORM_CLASS_NAMES.add(GRPC_CALLBACK_INVOKE_CLASS_NAME);
        TO_BE_TRANSFORM_CLASS_NAMES.add(GRPC_DELAY_STREAM_INVOKE_CLASS_NAME);
    }

    @Override
    public void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException {
        final CtClass clazz = classInfo.getCtClass();
        if (TO_BE_TRANSFORM_CLASS_NAMES.contains(classInfo.getClassName())) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }
            classInfo.setModified();
        }
    }

    private void updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) return;

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (STREAM_LISTENER_CLASS_NAME.equals(paramTypeName)) {
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "    $%d = %s.get($%1$d, false, true);"
                        + "\n    com.alibaba.ttl.spi.TtlAttachmentsDelegate.setAutoWrapperAttachment($%1$d);",
                    i + 1, TTL_STREAM_LISTENER_CLASS_NAME);
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ":\n" + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) method.insertBefore(insertCode.toString());
    }
}
