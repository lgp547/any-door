package io.github.lgp547.anydoor.util;

import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class BeanUtil {
    public static final SimpleTypeConverter SIMPLE_TYPE_CONVERTER = new SimpleTypeConverter();

    public static <T> T toBean(Class<T> clazz, String value) {
        T obj = AnyDoorBeanUtil.instantiate(clazz);
        if (value.startsWith("{") && value.endsWith("}")) {
            Map<String, Object> map = JsonUtil.toMap(value);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                setValue(clazz, obj, entry.getKey(), entry.getValue());
            }
        }
        return obj;
    }

    private static <T> void setValue(Class<T> clazz, Object obj, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(obj, getArgs(field, JsonUtil.toStrNotExc(value)));
        } catch (Exception ignored) {
        }
    }


    private static Object getArgs(Field field, String value) {
        Object obj = null;
        if (BeanUtil.isSimpleProperty(field.getType())) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.simpleTypeConvertIfNecessary(value, field.getType()));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> JsonUtil.toJavaBean(value, ResolvableType.forField(field).getType()));
        }
        if (obj == null && field.getType().isInterface() && (value.contains("->") || value.contains("::"))) {
            obj = LambdaUtil.runNotExc(() -> LambdaUtil.compileExpression(value, field.getGenericType()));
        }
        if (obj == null) {
            obj = LambdaUtil.runNotExc(() -> BeanUtil.toBean(field.getType(), value));
        }
        return obj;
    }

    public static boolean isSimpleProperty(Class<?> type) {
        Assert.notNull(type, "'type' must not be null");
        return isSimpleValueType(type);
    }

    public static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type &&
                (ClassUtils.isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Number.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type));
    }

    public static Object simpleTypeConvertIfNecessary(MethodParameter parameter, String value) {
        return SIMPLE_TYPE_CONVERTER.convertIfNecessary(value, parameter.getParameterType(), parameter);
    }

    public static <T> T simpleTypeConvertIfNecessary(Object value, Class<T> clazz) {
        return SIMPLE_TYPE_CONVERTER.convertIfNecessary(value, clazz);
    }
}
