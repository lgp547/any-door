package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.common.util.AnyDoorAopUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorClassUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
                    bean = AnyDoorAopUtil.getTargetObject(bean);
                }
            }
            return doRun(anyDoorDto, method, bean, () -> {
            });
        } catch (Exception e) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDto + "]");
            throw new RuntimeException(e);
        }
    }

    /**
     * {@code  io.github.lgp547.anydoor.attach.AnyDoorAttach#AnyDoorRun(String)}
     */
    public Object run(String anyDoorDtoStr, Method method, Object bean, Runnable endRun) {
        if (null == anyDoorDtoStr || anyDoorDtoStr.isEmpty()) {
            System.err.println("anyDoorService run param exception. anyDoorDtoStr is empty");
            return null;
        }
        if (null == method || null == bean) {
            System.err.println("anyDoorService run param exception. method or bean is null");
            return null;
        }

        try {
            Thread.currentThread().setContextClassLoader(AnyDoorService.class.getClassLoader());
            return doRun(JsonUtil.toJavaBean(anyDoorDtoStr, AnyDoorRunDto.class), method, bean, endRun);
        } catch (Throwable throwable) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDtoStr + "]");
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
            return null;
        }
    }

    public Object doRun(AnyDoorRunDto anyDoorDto, Method method, Object bean, Runnable endRun) {
        String methodName = method.getName();
        String content = JsonUtil.toStrNotExc(anyDoorDto.getContent());
        if (JsonUtil.isJsonArray(content)) {
            List<Map<String, Object>> contentMaps = JsonUtil.toMaps(content);
            return contentMaps.stream()
                .map(contentMap -> handleAndRun(anyDoorDto, method, bean, endRun, contentMap, methodName))
                .collect(Collectors.toList());
        } else {
            Map<String, Object> contentMap = JsonUtil.toMap(content);
            return handleAndRun(anyDoorDto, method, bean, endRun, contentMap, methodName);
        }
    }

    private static Object handleAndRun(AnyDoorRunDto anyDoorDto, Method method, Object bean, Runnable endRun,
        Map<String, Object> contentMap, String methodName) {
        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        if (anyDoorDto.getNum() == 1) {
            if (Objects.equals(anyDoorDto.getSync(), true)) {
                Object result = handlerMethod.invokeSync(contentMap);
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
                endRun.run();
                return result;
            } else {
                handlerMethod.invokeAsync(contentMap).whenComplete(futureResultLogConsumer(methodName)).whenComplete((result, throwable) -> endRun.run());
                return null;
            }
        } else {
            if (anyDoorDto.getConcurrent()) {
                List<CompletableFuture<Object>> completableFutures =
                    handlerMethod.concurrentInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName), excLogConsumer(methodName));
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).whenComplete((result, throwable) -> endRun.run());
            } else {
                if (Objects.equals(anyDoorDto.getSync(), true)) {
                    handlerMethod.parallelInvokeSync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
                    endRun.run();
                } else {
                    CompletableFuture<Void> voidCompletableFuture = handlerMethod.parallelInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
                    voidCompletableFuture.whenComplete((result, throwable) -> endRun.run());
                }
            }
            return null;
        }
    }

    private static BiConsumer<Integer, Object> resultLogConsumer(String methodName) {
        return (num, result) -> System.out.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " return: " + JsonUtil.toStrNotExc(result));
    }

    private static BiConsumer<Integer, Throwable> excLogConsumer(String methodName) {
        return (num, throwable) -> {
            System.err.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " exception: " + throwable.getMessage());
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
        };
    }

    private static BiConsumer<Object, Throwable> futureResultLogConsumer(String methodName) {
        return (result, throwable) -> {
            if (throwable != null) {
                System.err.println(ANY_DOOR_RUN_MARK + methodName + " exception: " + throwable.getMessage());
                Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
            } else {
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
            }
        };
    }
}
