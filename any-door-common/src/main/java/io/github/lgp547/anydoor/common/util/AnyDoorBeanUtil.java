package io.github.lgp547.anydoor.common.util;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
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

public class AnyDoorBeanUtil {

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz) {
        if (clazz == null || clazz.isInterface()) {
            throw new IllegalArgumentException("Specified class is null or interface. " + clazz);
        }
        Optional<Constructor<?>> noArgConstructorOpt = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        Object obj;
        try {
            if (noArgConstructorOpt.isPresent()) {
                Constructor<?> constructor = noArgConstructorOpt.get();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                obj = constructor.newInstance();
            } else {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                Object[] objects = IntStream.range(0, constructor.getParameterCount()).mapToObj(i -> null).toArray();
                obj = constructor.newInstance(objects);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("instantiate Exception" + clazz, e);
        }
        return (T) obj;
    }


}
