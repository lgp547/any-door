package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorClassUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import io.github.lgp547.anydoor.util.AopUtil;
import io.github.lgp547.anydoor.util.JsonUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AnyDoorService {

    private static final String ANY_DOOR_RUN_MARK = "any-door run ";

    public AnyDoorService() {
    }

    public Object run(AnyDoorRunDto anyDoorDto) {
        try {
            anyDoorDto.verify();
            Class<?> clazz = Class.forName(anyDoorDto.getClassName());
            Method method = AnyDoorClassUtil.getMethod(clazz, anyDoorDto.getMethodName(), anyDoorDto.getParameterTypes());

            boolean containsBean = AnyDoorSpringUtil.containsBean(clazz);
            Object bean;
            if (!containsBean) {
                bean = AnyDoorBeanUtil.instantiate(clazz);
            } else {
                bean = AnyDoorSpringUtil.getBean(clazz);
                if (!Modifier.isPublic(method.getModifiers())) {
                    bean = AopUtil.getTargetObject(bean);
                }
            }
            return doRun(anyDoorDto, method, bean);
        } catch (Exception e) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDto + "]");
            throw new RuntimeException(e);
        }
    }


    /**
     * {@code  io.github.lgp547.anydoor.attach.AnyDoorAttach#AnyDoorRun(String)}
     */
    public Object run(String anyDoorDtoStr, Method method, Object bean) {
        if (null == anyDoorDtoStr || anyDoorDtoStr.isEmpty()) {
            System.err.println("anyDoorService run param exception. anyDoorDtoStr is empty");
            return null;
        }
        if (null == method || null == bean) {
            System.err.println("anyDoorService run param exception. method or bean is null");
            return null;
        }

        try {
            return doRun(JsonUtil.toJavaBean(anyDoorDtoStr, AnyDoorRunDto.class), method, bean);
        } catch (Throwable throwable) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDtoStr + "]");
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).orElse(throwable).printStackTrace();
            return null;
        }
    }

    public Object doRun(AnyDoorRunDto anyDoorDto, Method method, Object bean) {
        String methodName = method.getName();
        Map<String, Object> contentMap = JsonUtil.toMap(JsonUtil.toStrNotExc(anyDoorDto.getContent()));

        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        Object result = null;
        if (Objects.equals(anyDoorDto.getSync(), true)) {
            if (anyDoorDto.getNum() == 1) {
                result = handlerMethod.invokeSync(contentMap);
            } else {
                if (anyDoorDto.getConcurrent()) {
                    throw new IllegalArgumentException("Concurrent calls do not support sync");
                }
                handlerMethod.parallelInvokeSync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
            }
        } else {
            if (anyDoorDto.getNum() == 1) {
                handlerMethod.invokeAsync(contentMap).whenComplete(futureResultLogConsumer(methodName));
            } else {
                if (anyDoorDto.getConcurrent()) {
                    handlerMethod.concurrentInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName), excLogConsumer(methodName));
                } else {
                    handlerMethod.parallelInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
                }
            }
        }
        if (result != null) {
            System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
        }
        return result;
    }

    private BiConsumer<Integer, Object> resultLogConsumer(String methodName) {
        return (num, result) -> System.out.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " return: " + JsonUtil.toStrNotExc(result));
    }

    private BiConsumer<Integer, Throwable> excLogConsumer(String methodName) {
        return (num, throwable) -> {
            System.err.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " exception: " + throwable.getMessage());
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).orElse(throwable).printStackTrace();
        };
    }

    private BiConsumer<Object, Throwable> futureResultLogConsumer(String methodName) {
        return (result, throwable) -> {
            if (throwable != null) {
                System.err.println(ANY_DOOR_RUN_MARK + methodName + " exception: " + throwable.getMessage());
                Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).orElse(throwable).printStackTrace();
            } else {
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
            }
        };
    }
}
