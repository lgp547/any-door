package io.github.lgp547.anydoor.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AnyDoorHandlerMethod extends HandlerMethod {


    public AnyDoorHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public CompletableFuture<Object> invokeAsync(JsonNode jsonNode) {
        return doInvokeAsync(getArgs(jsonNode));
    }

    protected Object[] getArgs(JsonNode jsonNode) {
        MethodParameter[] parameters = getMethodParameters();
        if (ObjectUtils.isEmpty(parameters)) {
            return new Object[0];
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
            String value = Optional.ofNullable(jsonNode.get(parameter.getParameterName())).map(getJsonNodeValueFun()).orElse(null);
            if (null == value) {
                args[i] = null;
                break;
            }
            if (BeanUtils.isSimpleProperty(parameter.getParameterType())) {
                SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
                args[i] = simpleTypeConverter.convertIfNecessary(value, parameter.getParameterType(), parameter);
            } else {
                // 用spring的mvc的转换
                parameter = parameter.nestedIfOptional();
                Type targetType = parameter.getNestedGenericParameterType();
                Class<?> contextClass = parameter.getContainingClass();
                Object paramObj = SpringUtil.readObject(targetType, contextClass, value);
                if (null == paramObj) {
                    paramObj = BeanUtils.instantiateClass(parameter.getParameterType());

                }
                args[i] = paramObj;
            }

        }
        return args;
    }

    private Function<JsonNode, String> getJsonNodeValueFun() {
        return curJson -> {
            if (curJson instanceof TextNode) {
                return curJson.asText();
            } else if (curJson instanceof NullNode) {
                return null;
            } else {
                return curJson.toString();
            }
        };
    }

    protected CompletableFuture<Object> doInvokeAsync(Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBridgedMethod().invoke(getBean(), args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
