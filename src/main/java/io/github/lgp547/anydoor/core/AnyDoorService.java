package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.dto.AnyDoorDto;
import io.github.lgp547.anydoor.util.AopUtil;
import io.github.lgp547.anydoor.util.BeanUtil;
import io.github.lgp547.anydoor.util.ClassUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import io.github.lgp547.anydoor.util.SpringUtil;
import io.github.lgp547.anydoor.vmtool.VmToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class AnyDoorService {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorService.class);

    private static final String ANY_DOOR_RUN_MARK = "any-door run ";

    private static volatile boolean isInit = false;

    public AnyDoorService(boolean isMvc) {
        if (isMvc) {
            return;
        }
        if (!isInit) {
            synchronized (AnyDoorService.class) {
                if (!isInit) {
                    VmToolUtils.init();
                    SpringUtil.initApplicationContexts(() -> VmToolUtils.getInstances(ApplicationContext.class));
                    isInit = true;
                }
            }
        }
    }

    /**
     * 只允许 illegalArgumentException 抛出
     */
    public Object run(AnyDoorDto anyDoorDto) {
        try {
            return doRun(anyDoorDto);
        } catch (Exception e) {
            log.error("run exception ", e);
            return null;
        }
    }

    public Object doRun(AnyDoorDto anyDoorDto) {
        anyDoorDto.verify();

        Class<?> clazz = anyDoorDto.getClazz();
        String methodName = anyDoorDto.getMethodName();
        Map<String, Object> contentMap = anyDoorDto.getContentMap();
        List<String> parameterTypes = anyDoorDto.getParameterTypes();

        boolean containsBean = SpringUtil.containsBean(clazz);
        Object bean;
        Method method;
        if (!containsBean) {
            bean = BeanUtil.instantiate(clazz);
            method = ClassUtil.getMethod(clazz, methodName, parameterTypes);
        } else {
            bean = SpringUtil.getBean(clazz);
            method = ClassUtil.getMethod(AopUtil.getTargetClass(bean), methodName, parameterTypes);
            if (!Modifier.isPublic(method.getModifiers())) {
                bean = AopUtil.getTargetObject(bean);
            }
        }


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
            log.info(ANY_DOOR_RUN_MARK + methodName + " return: {}", JsonUtil.toStrNotExc(result));
        }
        return result;
    }

    private BiConsumer<Integer, Object> resultLogConsumer(String methodName) {
        return (num, result) -> log.info(ANY_DOOR_RUN_MARK + methodName + " " + num + " return: {}", JsonUtil.toStrNotExc(result));
    }

    private BiConsumer<Integer, Throwable> excLogConsumer(String methodName) {
        return (num, throwable) -> log.info(ANY_DOOR_RUN_MARK + methodName + " " + num + " exception: ", throwable);
    }

    private BiConsumer<Object, Throwable> futureResultLogConsumer(String methodName) {
        return (result, throwable) -> {
            if (throwable != null) {
                log.info(ANY_DOOR_RUN_MARK + methodName + " exception: ", throwable);
            } else {
                log.info(ANY_DOOR_RUN_MARK + methodName + " return: {}", JsonUtil.toStrNotExc(result));
            }
        };
    }
}
