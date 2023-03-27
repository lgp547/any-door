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
import java.util.concurrent.CompletableFuture;

public class AnyDoorService {

    private static final Logger log = LoggerFactory.getLogger(AnyDoorService.class);

    static {
        SpringUtil.initApplicationContexts(VmToolUtils.getInstances(ApplicationContext.class));
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
        if (Objects.equals(anyDoorDto.getSync(), true)) {
            return handlerMethod.invokeSync(contentMap);
        } else {
            CompletableFuture<Object> future = handlerMethod.invokeAsync(contentMap);
            future.whenComplete((result, e) -> {
                String callMethodStr = "/any_door/run " + method.getName();
                if (e != null) {
                    log.info(callMethodStr + " exception: ", e);
                } else {
                    log.info(callMethodStr + " return: {}", JsonUtil.toStrNotExc(result));
                }
            });
            return null;
        }
    }
}
