package io.github.lgp547.anydoor.util;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;

public class ClassUtil {

    private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            log.debug("forName exception ", e);
            throw new IllegalArgumentException(e);
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
            throw new IllegalArgumentException("not found method. clazz " + clazz.getName() + " methodName:" + methodName);
        } else if (methods.size() == 1) {
            method = methods.get(0);
        } else {
            Optional<Class<?>[]> classes = Optional.ofNullable(parameterTypes).map(item -> item.stream().map(ClassUtil::forName).toArray(Class[]::new));
            try {
                method = clazz.getDeclaredMethod(methodName, classes.orElse(null));
            } catch (NoSuchMethodException e) {
                String s = "not found method. clazz:" + clazz.getName() + " methodName:" + methodName + " parameterTypes:" + parameterTypes;
                log.error(s, e);
                throw new IllegalArgumentException(s);
            }
        }
        return method;
    }


}
