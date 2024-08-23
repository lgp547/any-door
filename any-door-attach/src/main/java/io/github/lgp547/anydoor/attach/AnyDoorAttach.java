package io.github.lgp547.anydoor.attach;

import com.taobao.arthas.common.FileUtils;
import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.common.util.AnyDoorClassUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorClassloader;
import io.github.lgp547.anydoor.common.util.AnyDoorFileUtil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AnyDoorAttach {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (null == agentArgs || agentArgs.isEmpty()) {
            System.err.println("any_door agentmain param is empty");
            return;
        }

        if (agentArgs.startsWith("file://")) {
            try {
                agentArgs = AnyDoorFileUtil.getTextFileAsString(new File(URLDecoder.decode(agentArgs.substring(7), "UTF-8")));
            } catch (IOException e) {
                System.err.println("any_door agentmain error. agentArgs[" + agentArgs + "]");
                e.printStackTrace();
                return;
            }
        }

        AnyDoorRun(agentArgs, inst);
    }


    /**
     * {@code  io.github.lgp547.anydoor.core.AnyDoorService#run(String, Method, Object, Runnable)}
     */
    public static void AnyDoorRun(String anyDoorDtoStr, Instrumentation inst) {
        AnyDoorRunDto anyDoorRunDto = AnyDoorRunDto.parseObj(anyDoorDtoStr);
        if (!anyDoorRunDto.verifyPassByAttach()) {
            System.err.println("any_door agentmain error. anyDoorDtoStr[" + anyDoorDtoStr + "]");
            return;
        }

        Runnable startRun = getStartRunnable(inst, anyDoorRunDto);

        AnyDoorVmToolUtils.init();


        try (AnyDoorClassloader anyDoorClassloader = new AnyDoorClassloader(anyDoorRunDto.getJarPaths())) {
            Class<?> clazz = AnyDoorClassUtil.forName(anyDoorRunDto.getClassName());
            Method method = AnyDoorClassUtil.getMethod(clazz, anyDoorRunDto.getMethodName(), anyDoorRunDto.getParameterTypes());
            Object instance = AnyDoorVmToolUtils.getInstance(clazz, !Modifier.isPublic(method.getModifiers()));

            Class<?> anyDoorServiceClass = anyDoorClassloader.loadClass("io.github.lgp547.anydoor.core.AnyDoorService");
            Object anyDoorService = anyDoorServiceClass.getConstructor().newInstance();
            Method run = anyDoorServiceClass.getMethod("run", String.class, Method.class, Object.class, Runnable.class, Runnable.class);
            Runnable endRun = anyDoorClassloader::forceClose;
            run.invoke(anyDoorService, anyDoorDtoStr, method, instance, startRun, endRun);
        } catch (Exception e) {
            System.err.println("any_door agentmain error. anyDoorDtoStr[" + anyDoorDtoStr + "]");
            e.printStackTrace();
        }
    }


    private static Runnable getStartRunnable(Instrumentation inst, AnyDoorRunDto anyDoorRunDto) {
        Runnable runnable = () -> {};
        try {
            String className = AnyDoorRunDto.AnyDoorInjectedClassName;
            String baseJavaPath = AnyDoorRunDto.dataBaseJavaPath(anyDoorRunDto.getProjectBasePath());
            String javaFilePath = baseJavaPath + className + ".java";
            String classFilePath = baseJavaPath + className + ".class";

            File javaFile = new File(javaFilePath);
            byte[] javaFileBytes = Files.readAllBytes(javaFile.toPath());
            String fileContent = new String(javaFileBytes, StandardCharsets.UTF_8);
            if (!fileContent.contains("AnyDoorIsUpdatePreRun:true")) {
                return runnable;
            }

            Class<?> cls = searchClass(inst, className);
            boolean isNullCls = cls == null;

            // 编译java文件
            compilerJavaFile(javaFilePath);

            // 替换成false
            String newContent = fileContent.replace("AnyDoorIsUpdatePreRun:true", "AnyDoorIsUpdatePreRun:false");
            FileUtils.writeByteArrayToFile(javaFile, newContent.getBytes());

            if (isNullCls) {
                // 将编译后的class文件加载到内存中
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new File(baseJavaPath).toURI().toURL()});
                cls = Class.forName(className, true, urlClassLoader);
            }

            byte[] bytes = Files.readAllBytes(new File(classFilePath).toPath());
            inst.redefineClasses(new ClassDefinition(cls, bytes));

            // 使用反射调用类的方法
            Object instance = cls.getDeclaredConstructor().newInstance();
            Method method = cls.getMethod("preRun");
            runnable = () -> {
                try {
                    method.invoke(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runnable;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void compilerJavaFile(String javaPath) {
        new File(javaPath.replace(".java", ".class")).delete();

        // 获取Java编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // 编译文件
        int run = compiler.run(null, null, null, javaPath);
        if (run != 0) {
            throw new RuntimeException("编译失败");
        }
    }

    public static Class<?> searchClass(Instrumentation inst, String className) {
        Class<?> targetClass = null;
        for (Class<?> loadedClass : inst.getAllLoadedClasses()) {
            if (loadedClass.getName().equals(className)) {
                targetClass = loadedClass;
                break;
            }
        }

        return targetClass;
    }

}
