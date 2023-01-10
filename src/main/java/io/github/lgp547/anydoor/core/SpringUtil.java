package io.github.lgp547.anydoor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Type;
import java.util.Objects;

public class SpringUtil implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringUtil.class);

    private static ApplicationContext applicationContext;

    private static boolean isWebmvcProject = false;

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return Objects.requireNonNull(applicationContext).getBean(requiredType);
    }

    public static boolean containsBean(Class<?> requiredType) {
        try {
            getBean(requiredType);
            return true;
        } catch (BeansException e) {
            return false;
        }
    }

    public static Object readObject(Type targetType, Class<?> contextClass, String value) {
        if (isWebmvcProject) {
            return SpringWebmvcUtil.readObject(targetType, contextClass, value);
        } else {
            return JsonUtil.toJavaBean(value, contextClass);
        }
    }

    public static <T> T instantiateClass(Class<T> clazz) {
        try {
            return BeanUtils.instantiateClass(clazz);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
        isWebmvcProject = SpringWebmvcUtil.init(applicationContext);
        log.info("mmmmmmmmmmmmmmmmmmm any-door springUtil init end mmmmmmmmmmmmmmmmmmm");
    }

}
