package io.github.lgp547.anydoor.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpringUtil {

    private static List<ApplicationContext> applicationContextList;

    private static Supplier<ApplicationContext[]> applicationContextsSupplier;

    private static volatile boolean isInit = false;

    public static void initApplicationContexts(Supplier<ApplicationContext[]> applicationContextsSupplier) {
        if (!isInit) {
            synchronized (SpringUtil.class) {
                if (!isInit) {
                    doInit(applicationContextsSupplier);
                    isInit = true;
                }
            }
        }
    }

    private static void doInit(Supplier<ApplicationContext[]> applicationContextsSupplier) {
        SpringUtil.applicationContextsSupplier = applicationContextsSupplier;
        List<ApplicationContext> curApplicationContextList = updateApplicationContextList();

        if (curApplicationContextList.size() != 1) {

            String contextName = System.getProperty("anydoor.spring.core.context.name");
            if (StringUtils.hasLength(contextName)) {
                Optional<ApplicationContext> any = curApplicationContextList.stream().filter(item -> item.getDisplayName().contains(contextName)).findAny();
                if (any.isPresent()) {
                    SpringUtil.applicationContextList = new ArrayList<>();
                    SpringUtil.applicationContextList.add(any.get());
                }
            } else {
                ArrayList<String> contextNames = new ArrayList<>();
                contextNames.add("WebServerApplicationContext");
                contextNames.add("ReactiveWebApplicationContext");
                contextNames.add("WebApplicationContext");
                contextNames.add("ConfigurableApplicationContext");
                SpringUtil.applicationContextList.sort(Comparator.comparing(ApplicationContext::getDisplayName, (name1, name2) -> comparingSort(contextNames, name1, name2)));
            }
        }
        BuildProperties instance = BuildProperties.getInstance();
        System.out.println("mmmmmmmmmmmmmmmmmmm any-door " + instance.getVersion() + " init end mmmmmmmmmmmmmmmmmmm");
    }

    private static int comparingSort(List<String> sort, String name1, String name2) {
        for (String s : sort) {
            if (name1.contains(s)) {
                return -1;
            }
            if (name2.contains(s)) {
                return 1;
            }
        }
        return 0;
    }

    private static List<ApplicationContext> updateApplicationContextList() {
        SpringUtil.applicationContextList = Arrays.stream(applicationContextsSupplier.get()).collect(Collectors.toList());
        return SpringUtil.applicationContextList;
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        Objects.requireNonNull(applicationContextList);

        try {
            return doGetBean(requiredType, applicationContextList);
        } catch (IllegalStateException illegalStateException) {
            return doGetBean(requiredType, updateApplicationContextList());
        }
    }

    private static <T> T doGetBean(Class<T> requiredType, List<ApplicationContext> applicationContextList1) {
        for (ApplicationContext applicationContext : applicationContextList1) {
            try {
                return applicationContext.getBean(requiredType);
            } catch (BeansException ignored) {
            }
        }
        throw new NoSuchBeanDefinitionException("No bean of type " + requiredType + " is defined");
    }

    public static boolean containsBean(Class<?> requiredType) {
        try {
            getBean(requiredType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
