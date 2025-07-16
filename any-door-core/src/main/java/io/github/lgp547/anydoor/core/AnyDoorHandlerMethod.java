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
 
package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.support.HandlerMethod;
import io.github.lgp547.anydoor.util.BeanUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import io.github.lgp547.anydoor.util.LambdaUtil;
import org.springframework.aop.framework.AopProxy;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AnyDoorHandlerMethod extends HandlerMethod {
    
    public AnyDoorHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }
    
    public CompletableFuture<Object> invokeAsync(Runnable startRun, Map<String, Object> contentMap) {
        Object[] args = getArgs(contentMap);
        return CompletableFuture.supplyAsync(() -> doInvoke(startRun, args));
    }
    
    /**
     * 阻塞调度线程，简化堆栈使用
     */
    public Object invokeSync(Runnable startRun, Map<String, Object> contentMap) {
        return doInvoke(startRun, getArgs(contentMap));
    }
    
    /**
     * 并行，阻塞调度进程
     */
    public void parallelInvokeSync(Runnable startRun, List<Map<String, Object>> contentMaps, int num, BiConsumer<Integer, Object> resultLogConsumer) {
        Object[] curArgs = null;
        for (int i = 0; i < num; i++) {
            if (contentMaps.size() > i) {
                curArgs = getArgs(contentMaps.get(i));
            }
            final Object[] args = curArgs;
            resultLogConsumer.accept(i, doInvoke(startRun, args));
        }
    }
    
    /**
     * 并行，不阻塞调度进程
     */
    public CompletableFuture<Void> parallelInvokeAsync(Runnable startRun, List<Map<String, Object>> contentMaps, int num, BiConsumer<Integer, Object> resultLogConsumer) {
        return CompletableFuture.runAsync(() -> {
            Object[] curArgs = null;
            for (int i = 0; i < num; i++) {
                if (contentMaps.size() > i) {
                    curArgs = getArgs(contentMaps.get(i));
                }
                final Object[] args = curArgs;
                resultLogConsumer.accept(i, doInvoke(startRun, args));
            }
        });
    }
    
    /**
     * 并发，不阻塞调度进程
     */
    public List<CompletableFuture<Object>> concurrentInvokeAsync(Runnable startRun, List<Map<String, Object>> contentMaps, int num, BiConsumer<Integer, Object> resultLogConsumer,
                                                                 BiConsumer<Integer, Throwable> excLogConsumer) {
        Object[] curArgs = null;
        List<CompletableFuture<Object>> completableFutures = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            if (contentMaps.size() > i) {
                curArgs = getArgs(contentMaps.get(i));
            }
            final Object[] args = curArgs;
            final int i1 = i;
            CompletableFuture<Object> objectCompletableFuture = CompletableFuture.supplyAsync(() -> doInvoke(startRun, args)).whenComplete(((o, throwable) -> {
                if (throwable != null) {
                    excLogConsumer.accept(i1, throwable);
                } else {
                    resultLogConsumer.accept(i1, o);
                }
            }));
            completableFutures.add(objectCompletableFuture);
        }
        return completableFutures;
    }
    
    public Object doInvoke(Runnable startRun, Object[] args) {
        try {
            startRun.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object bean = getBean();
        if (bean instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
            try {
                if (invocationHandler instanceof AopProxy) {
                    return invocationHandler.invoke(bean, getBridgedMethod(), args);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        try {
            return getBridgedMethod().invoke(bean, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected Object[] getArgs(Map<String, Object> contentMap) {
        MethodParameter[] parameters = getMethodParameters();
        if (parameters == null || parameters.length == 0) {
            return new Object[0];
        }
        
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
            String value;
            if (contentMap.containsKey(parameter.getParameterName())) {
                value = Optional.ofNullable(contentMap.get(parameter.getParameterName())).map(JsonUtil::toStrNotExc).orElse(null);
            } else {
                // 对于是接口的话，通过顺序来填充参数，不再通过name来映射
                value = Optional.ofNullable(contentMap.get("args" + i)).map(JsonUtil::toStrNotExc).orElse(null);
            }
            if (null == value) {
                args[i] = null;
                continue;
            }
            
            args[i] = getArgs(parameter, value);
        }
        return args;
    }
    
    /**
     * simpleTypeConvert
     * json序列化
     * 反射构造并尝试set进去 （转Map然后塞进去）
     * null
     */
    private Object getArgs(MethodParameter parameter, String value) {
        Object obj = null;
        if (BeanUtil.isSimpleProperty(parameter.getParameterType())) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.simpleTypeConvertIfNecessary(parameter, value));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> JsonUtil.toJavaBean(value, ResolvableType.forMethodParameter(parameter).getType()));
        }
        if (obj == null && parameter.getParameterType().isInterface() && (value.contains("->") || value.contains("::"))) {
            obj = LambdaUtil.runNotExc(() -> LambdaUtil.compileExpression(value, parameter.getNestedGenericParameterType()));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.toBean(parameter.getParameterType(), value));
        }
        if (obj == null) {
            System.out.println("any-door run param [" + parameter.getParameterType().getName() + "] is null");
        }
        return obj;
    }
    
}
