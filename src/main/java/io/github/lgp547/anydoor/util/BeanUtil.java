package io.github.lgp547.anydoor.util;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class BeanUtil {
    public static final SimpleTypeConverter SIMPLE_TYPE_CONVERTER = new SimpleTypeConverter();

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Specified class is an interface. " + clazz);
        }
        Optional<Constructor<?>> noArgConstructorOpt = Arrays.stream(clazz.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        Object obj;
        try {
            if (noArgConstructorOpt.isPresent()) {
                obj = noArgConstructorOpt.get().newInstance();
            } else {
                Constructor<?> constructor = clazz.getConstructors()[0];
                Object[] objects = IntStream.range(0, constructor.getParameterCount()).mapToObj(i -> null).toArray();
                obj = constructor.newInstance(objects);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("instantiate Exception" + clazz);
        }
        return (T) obj;
    }

    public static <T> T toBean(Class<T> clazz, String value) {
        T obj = instantiate(clazz);
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
            field.set(obj, BeanUtil.simpleTypeConvertIfNecessary(value, field.getType()));
        } catch (Exception ignored) {
        }
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
