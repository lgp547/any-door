/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.github.lgp547.anydoor.common.util;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class AnyDoorBeanUtil {
    
    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz) {
        if (clazz == null || clazz.isInterface()) {
            throw new IllegalArgumentException("Specified class is null or interface. " + clazz);
        }
        Object kotlinSingleton = getKotlinSingleton(clazz);
        if (kotlinSingleton != null) {
            return (T) kotlinSingleton;
        }
        Optional<Constructor<?>> noArgConstructorOpt = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        Object obj;
        try {
            if (noArgConstructorOpt.isPresent()) {
                Constructor<?> constructor = noArgConstructorOpt.get();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                obj = constructor.newInstance();
            } else {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                Object[] objects = IntStream.range(0, constructor.getParameterCount()).mapToObj(i -> null).toArray();
                obj = constructor.newInstance(objects);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("instantiate Exception" + clazz, e);
        }
        return (T) obj;
    }

    /**
     * 返回 Kotlin object 或 companion 声明的 JVM 单例实例；普通类返回 {@code null}。
     *
     * @param clazz Kotlin object 或 companion 的 JVM 类
     * @return 已初始化的单例实例，或 {@code null}
     */
    public static Object getKotlinSingleton(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (!isKotlinClass(clazz)) {
            return null;
        }
        Object instance = getStaticFieldValue(clazz, "INSTANCE", clazz);
        if (instance != null) {
            return instance;
        }
        Class<?> enclosingClass = clazz.getEnclosingClass();
        if (enclosingClass == null) {
            return null;
        }
        String nestedName = clazz.getName().substring(clazz.getName().lastIndexOf('$') + 1);
        instance = getStaticFieldValue(enclosingClass, nestedName, clazz);
        if (instance != null) {
            return instance;
        }
        return getStaticFieldValue(enclosingClass, "Companion", clazz);
    }

    private static boolean isKotlinClass(Class<?> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if ("kotlin.Metadata".equals(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    private static Object getStaticFieldValue(Class<?> owner, String fieldName, Class<?> expectedType) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getType() != expectedType) {
                return null;
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(null);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }
    
}
