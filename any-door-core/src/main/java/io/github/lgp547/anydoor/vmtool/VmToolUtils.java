package io.github.lgp547.anydoor.vmtool;

import arthas.VmTool;
import com.taobao.arthas.common.OSUtils;
import io.github.lgp547.anydoor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.security.CodeSource;

public class VmToolUtils {
    private final static Logger log = LoggerFactory.getLogger(VmToolUtils.class);

    private static VmTool instance = null;

    public static void init() {
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

        CodeSource codeSource = VmToolUtils.class.getProtectionDomain().getCodeSource();
        String libPath = null;
        if (codeSource != null) {
            try {
                File bootJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                log.info("load vmtool from {}", bootJarPath.getAbsolutePath());

                libPath = FileUtil.copyChildFile(bootJarPath, "vmlib/" + libName);
                instance = VmTool.getInstance(libPath);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        if (instance == null) {
            throw new IllegalStateException("VmToolUtils init fail. codeSource: " + codeSource + " libPath: " + libPath);
        }
    }

    public static <T> T getInstance(Class<T> klass) {
        T[] instances = instance.getInstances(klass);
        Assert.isTrue(instances.length == 1, "klass" + klass + " instances != 1");
        return instances[0];
    }

    public static <T> T[] getInstances(Class<T> klass) {
        return instance.getInstances(klass);
    }

}