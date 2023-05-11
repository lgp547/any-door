package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.support.HandlerMethod;
import io.github.lgp547.anydoor.util.BeanUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import io.github.lgp547.anydoor.util.LambdaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AnyDoorHandlerMethod extends HandlerMethod {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorHandlerMethod.class);

    public AnyDoorHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public CompletableFuture<Object> invokeAsync(Map<String, Object> contentMap) {
        return CompletableFuture.supplyAsync(() -> invokeSync(contentMap));
    }

    /**
     * 阻塞调度线程，简化堆栈使用
     */
    public Object invokeSync(Map<String, Object> contentMap) {
        return doInvoke(getArgs(contentMap));
    }

    /**
     * 并行，阻塞调度进程
     */
    public void parallelInvokeSync(Map<String, Object> contentMap, int num, BiConsumer<Integer, Object> resultLogConsumer) {
        Object[] args = getArgs(contentMap);
        for (int i = 0; i < num; i++) {
            resultLogConsumer.accept(i, doInvoke(args));
        }
    }

    /**
     * 并行，不阻塞调度进程
     */
    public void parallelInvokeAsync(Map<String, Object> contentMap, int num, BiConsumer<Integer, Object> resultLogConsumer) {
        Object[] args = getArgs(contentMap);
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < num; i++) {
                resultLogConsumer.accept(i, doInvoke(args));
            }
        });
    }

    /**
     * 并发，不阻塞调度进程
     */
    public void concurrentInvokeAsync(Map<String, Object> contentMap, int num, BiConsumer<Integer, Object> resultLogConsumer, BiConsumer<Integer, Throwable> excLogConsumer) {
        Object[] args = getArgs(contentMap);
        for (int i = 0; i < num; i++) {
            final int i1 = i;
            CompletableFuture.supplyAsync(() -> doInvoke(args)).whenComplete(((o, throwable) -> {
                if (throwable != null) {
                    excLogConsumer.accept(i1, throwable);
                } else {
                    resultLogConsumer.accept(i1, o);
                }
            }));
        }
    }

    private Object doInvoke(Object[] args) {
        Object bean = getBean();
        if (bean instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
            if (invocationHandler instanceof AopProxy) {
                try {
                    return invocationHandler.invoke(bean, getBridgedMethod(), args);
                } catch (Throwable e) {
                    log.error("invoke exception ", e);
                }
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
        if (ObjectUtils.isEmpty(parameters)) {
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
     * 函数式检查
     * json序列化
     * 反射构造
     * null
     */
    private Object getArgs(MethodParameter parameter, String value) {
        Object obj = null;
        if (BeanUtil.isSimpleProperty(parameter.getParameterType())) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.simpleTypeConvertIfNecessary(parameter, value));
        }
        if (obj == null && parameter.getParameterType().isInterface() && (value.contains("->") || value.contains("::"))) {
            obj = LambdaUtil.runNotExc(() -> LambdaUtil.compileExpression(value, parameter.getNestedGenericParameterType()));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> JsonUtil.toJavaBean(value, ResolvableType.forMethodParameter(parameter).getType()));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.instantiate(parameter.getParameterType()));
        }
        return obj;
    }

}
