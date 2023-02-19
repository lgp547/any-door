package io.github.lgp547.anydoor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

public class SpringUtil implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringUtil.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
        SpringWebmvcUtil.init(applicationContext);
        log.info("mmmmmmmmmmmmmmmmmmm any-door 0.0.8 springUtil init end mmmmmmmmmmmmmmmmmmm");
    }

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

}
