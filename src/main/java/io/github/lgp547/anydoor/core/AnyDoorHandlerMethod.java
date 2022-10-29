package io.github.lgp547.anydoor.core;

import com.fasterxml.jackson.databind.JsonNode;
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

public class AnyDoorHandlerMethod extends HandlerMethod {


    public AnyDoorHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public Object invoke(JsonNode jsonNode) {
        try {
            Object[] args = getArgs(jsonNode);
            return doInvoke(args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
            String value = Optional.ofNullable(jsonNode.get(parameter.getParameterName()))
                    .map(curJson -> curJson instanceof TextNode ? curJson.asText() : curJson.toString())
                    .orElse(null);
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

    protected Object doInvoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        // todo:这里会走aop的吗
        return getBridgedMethod().invoke(getBean(), args);
    }


}
