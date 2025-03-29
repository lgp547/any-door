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

import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.framework.Advised;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

public class AnyDoorAopUtil {
    
    public static Class<?> getTargetClass(Object candidate) {
        Assert.notNull(candidate, "Candidate object must not be null");
        Class<?> result = null;
        if (candidate instanceof TargetClassAware) {
            result = ((TargetClassAware) candidate).getTargetClass();
        }
        if (result == null) {
            result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
        }
        return result;
    }
    
    public static boolean isCglibProxy(@Nullable Object object) {
        return (object instanceof SpringProxy &&
                object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
    }
    
    public static boolean isAopProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) ||
                object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getTargetObject(Object candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        try {
            if (isAopProxy(candidate) && candidate instanceof Advised) {
                Object target = ((Advised) candidate).getTargetSource().getTarget();
                if (target != null) {
                    return (T) getTargetObject(target);
                }
            }
        } catch (Throwable ex) {
            System.err.println("Failed to unwrap proxied object");
            ex.printStackTrace();
        }
        return (T) candidate;
    }
}
