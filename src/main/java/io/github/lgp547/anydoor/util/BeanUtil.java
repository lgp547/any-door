package io.github.lgp547.anydoor.util;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;

public class BeanUtil {

    public static <T> T instantiate(Class<T> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Specified class is an interface. " + clazz);
        }
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("instantiate Exception" + clazz);
        }
    }

    public static boolean isSimpleProperty(Class<?> type) {
        Assert.notNull(type, "'type' must not be null");
        return isSimpleValueType(type) || (type.isArray() && isSimpleValueType(type.getComponentType()));
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
        SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
        return simpleTypeConverter.convertIfNecessary(value, parameter.getParameterType(), parameter);
    }
}
