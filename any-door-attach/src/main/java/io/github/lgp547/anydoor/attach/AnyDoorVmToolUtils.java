package io.github.lgp547.anydoor.attach;

import arthas.VmTool;
import com.taobao.arthas.common.OSUtils;
import io.github.lgp547.anydoor.common.util.AnyDoorAopUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorFileUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.security.CodeSource;

public class AnyDoorVmToolUtils {

    private static VmTool instance = null;

    public static void init() {
        if (instance != null) {
            return;
        }

        String libName;
        if (OSUtils.isMac()) {
            libName = "libArthasJniLibrary.dylib";
        } else if (OSUtils.isLinux()) {
            libName = "libArthasJniLibrary-x64.so";
        } else if (OSUtils.isWindows()) {
            libName = "libArthasJniLibrary-x64.dll";
        } else {
            throw new IllegalStateException("unsupported os");
        }

        CodeSource codeSource = AnyDoorVmToolUtils.class.getProtectionDomain().getCodeSource();
        String libPath = null;
        if (codeSource != null) {
            try {
                File bootJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());

                libPath = AnyDoorFileUtil.copyChildFile(bootJarPath, "vmlib/" + libName);
                instance = VmTool.getInstance(libPath);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        if (instance == null) {
            throw new IllegalStateException("VmToolUtils init fail. codeSource: " + codeSource + " libPath: " + libPath);
        }
    }

    public static <T> T getOrGenInstance(Class<T> klass) {
        T[] instances = instance.getInstances(klass);
        if (instances.length == 0) {
            return AnyDoorBeanUtil.instantiate(klass);
        } else {
            return instances[0];
        }
    }

    public static <T> T[] getInstances(Class<T> klass) {
        return instance.getInstances(klass);
    }

    public static Object getInstance(Class<?> clazz, boolean isGetTargetObject) {
        Object instance = getInstance(clazz);
        if (isGetTargetObject) {
            return AnyDoorAopUtil.getTargetObject(instance);
        } else {
            return instance;
        }
    }

    /**
     * 优先通过spring 上下文获取
     */
    public static Object getInstance(Class<?> clazz) {
        try {
            // 这里用的是被调用项目的ApplicationContext
            AnyDoorSpringUtil.initApplicationContexts(() -> AnyDoorVmToolUtils.getInstances(ApplicationContext.class), () -> AnyDoorVmToolUtils.getInstances(BeanFactory.class));
            if (AnyDoorSpringUtil.containsBean(clazz)) {
                return AnyDoorSpringUtil.getBean(clazz);
            }
        } catch (Throwable ignored) {
        }
        Object[] instances = AnyDoorVmToolUtils.getInstances(clazz);
        if (instances.length == 0) {
            return AnyDoorBeanUtil.instantiate(clazz);
        } else {
            return instances[0];
        }
    }
}