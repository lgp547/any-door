package io.github.lgp547.anydoor.attach.vmtool;

import arthas.VmTool;
import com.taobao.arthas.common.OSUtils;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorFileUtil;

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

}