package io.github.lgp.anydoor.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            String value = jsonNode.get(parameter.getParameterName()).toString();
            if (BeanUtils.isSimpleProperty(parameter.getParameterType())) { // todo: 这个判断可能得修改一下
                SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
                args[i] = simpleTypeConverter.convertIfNecessary(value, parameter.getParameterType(), parameter);
            } else {
                // 用spring的mvc的转换
                Class<?> targetType = parameter.getParameterType();
                Class<?> contextClass = parameter.getContainingClass();
                args[i] = SpringUtil.readObject(targetType, contextClass, value, parameter);
            }

        }
        return args;
    }

    protected Object doInvoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        // todo:这里会走aop的吗
        return getBridgedMethod().invoke(getBean(), args);
    }


}
