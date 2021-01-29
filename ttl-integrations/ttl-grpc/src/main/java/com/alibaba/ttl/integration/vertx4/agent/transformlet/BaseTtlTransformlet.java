package com.alibaba.ttl.integration.vertx4.agent.transformlet;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;


/**
 * @author: tk (305809299 at qq dot com)
 * @since: 2021/1/21
 */
public abstract class BaseTtlTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(BaseTtlTransformlet.class);

    @Override
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        final CtClass clazz = classInfo.getCtClass();
        if (getCallClassNames().contains(classInfo.getClassName())) {

            //load ttl wrapper class
            loadClass();
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }

            classInfo.setModified();
        } else {
            if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()) {
                return;
            }

            logger.info("Transforming class " + classInfo.getClassName());
        }
    }

    /**
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     * @see Utils#setAutoWrapperAttachment(Object)
     */
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE") // [ERROR] Format string should use %n rather than \n
    private void updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(@NonNull final CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)) {
            return;
        }

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (getParamTypeNameToDecorateMethodClass().containsKey(paramTypeName)) {

                if (!needDecorateToTtlWrapper(method.getName())) {
                    return;
                }
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "$%d = %s.get($%1$d, false, true);"
                        + "\ncom.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.setAutoWrapperAttachment($%1$d);",
                    i + 1, getParamTypeNameToDecorateMethodClass().get(paramTypeName));
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) {
            method.insertBefore(insertCode.toString());
        }
    }

    /**
     * Template method which can be overridden to load specific class.
     * call on {@link #doTransform(ClassInfo)}
     * <p>
     * current project was added to boot classpath path.
     * so there will be throw {@link NoClassDefFoundError}
     * when {@link BaseTtlTransformlet#updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(CtMethod)}
     * try to load the class that related some class that was loaded By AppClassLoader.
     *
     * @see NoClassDefFoundError
     * @see com.alibaba.ttl.TtlVertxHandler
     * @see io.vertx.core.Handler
     * @see sun.misc.Launcher.AppClassLoader
     */
    protected void loadClass() {
        // For subclasses: do nothing by default.
    }

    ;

    /**
     * if it is true, the method that belong to some class which from CALL_CLASS_NAMES will be modified
     *
     * @param methodName method from {@link #getCallClassNames}
     * @return .
     * @see BaseTtlTransformlet#updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(CtMethod)
     */
    protected boolean needDecorateToTtlWrapper(String methodName) {
        // For subclasses: return true by default.
        return true;
    }

    protected abstract Set<String> getCallClassNames();
    protected abstract Set<String> getDecorateMethodsName();
    protected abstract Map<String, String> getParamTypeNameToDecorateMethodClass();
}
