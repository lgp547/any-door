package io.github.lgp547.anydoor.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassUtil {

    private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("forName ClassNotFoundException className {}", className, e);
            throw new IllegalArgumentException("找不到类 className:" + className);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, @Nullable List<String> parameterTypes) {
        Method method;
        List<Method> methods = new ArrayList<>();
        for (Method clazzMethod : clazz.getDeclaredMethods()) {
            if (clazzMethod.getName().equals(methodName)) {
                methods.add(clazzMethod);
            }
        }
        if (methods.isEmpty()) {
            throw new IllegalArgumentException("找不到方法 clazz " + clazz.getName() + " methodName:" + methodName);
        } else if (methods.size() == 1) {
            method = methods.get(0);
        } else {
            Optional<Class<?>[]> classes = Optional.ofNullable(parameterTypes).map(item -> item.stream().map(ClassUtil::forName).toArray(Class[]::new));
            try {
                method = clazz.getMethod(methodName, classes.orElse(null));
            } catch (NoSuchMethodException e) {
                String s = "找不到方法 clazz:" + clazz.getName() + " methodName:" + methodName + " parameterTypes:" + parameterTypes;
                log.error("getMethod " + s, e);
                throw new IllegalArgumentException(s);
            }
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTargetObject(Object candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        try {
            if (AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
                Object target = ((Advised) candidate).getTargetSource().getTarget();
                if (target != null) {
                    return (T) getTargetObject(target);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to unwrap proxied object", ex);
        }
        return (T) candidate;
    }
}
