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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnyDoorClassUtil {
    
    private static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<>();
    
    static {
        PRIMITIVE_TYPE_MAP.put("byte", byte.class);
        PRIMITIVE_TYPE_MAP.put("short", short.class);
        PRIMITIVE_TYPE_MAP.put("int", int.class);
        PRIMITIVE_TYPE_MAP.put("long", long.class);
        PRIMITIVE_TYPE_MAP.put("float", float.class);
        PRIMITIVE_TYPE_MAP.put("double", double.class);
        PRIMITIVE_TYPE_MAP.put("char", char.class);
        PRIMITIVE_TYPE_MAP.put("boolean", boolean.class);
    }
    
    public static Class<?> forName(String className) {
        Class<?> clazz = PRIMITIVE_TYPE_MAP.get(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            return Class.forName(className);
        } catch (Exception e) {
            System.err.println("not found class. className:" + className);
            throw new IllegalArgumentException(e);
        }
    }
    
    public static Method getMethod(Class<?> clazz, String methodName, List<String> parameterTypes) {
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
            Optional<Class<?>[]> classes = Optional.ofNullable(parameterTypes).map(item -> item.stream().map(AnyDoorClassUtil::forName).toArray(Class[]::new));
            try {
                method = clazz.getDeclaredMethod(methodName, classes.orElse(null));
            } catch (NoSuchMethodException e) {
                String s = "not found method. clazz:" + clazz.getName() + " methodName:" + methodName + " parameterTypes:" + parameterTypes;
                System.err.println(s);
                throw new IllegalArgumentException(s);
            }
        }
        return method;
    }
    
}
