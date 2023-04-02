package io.github.lgp547.anydoorplugin.util;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class ImportNewUtil {

    public static final String anyDoorAllDependenceName = "any-door-all-dependence.jar";

    public static final String anyDoorLibraryName = "any-door";

    public final static UnaryOperator<String> ANY_DOOR_JAR_PATH = version -> "/io/github/lgp547/any-door/" + version + "/any-door-" + version + ".jar";

    public static String getPluginBasePath() {
        return PathManager.getPluginsPath() + "/any-door-plugin/lib";
    }

    public static String getPluginLibPath(String libraryName, String libraryVersion) {
        return getPluginBasePath() + "/" + libraryName + "-" + libraryVersion + ".jar";
    }

    public static void fillAnyDoorJar(Project project, String libName, String libVersion) {
        try {
            removeLibraryIfExist(libName);
            String filePath = fillLibrary(libName, libVersion);
            NotifierUtil.notifyInfo(project, "fill " + libName + " library success: " + filePath);
        } catch (Exception e) {
            NotifierUtil.notifyError(project, "fill " + libName + "library fail: " + e.getMessage());
        }
    }

    /**
     * todo: async remote download. lib pata from settings
     */
    private static String fillLibrary(String libName, String libVersion) throws IOException {
        File file = new File(getPluginLibPath(libName, libVersion));
        if (!file.exists()) {
            String httpPath = "https://s01.oss.sonatype.org/content/repositories/releases" + ANY_DOOR_JAR_PATH.apply(libVersion);
            FileUtils.copyURLToFile(new URL(httpPath), file);
            if (!file.isFile()) {
                throw new RuntimeException("down jar file fail");
            }
        }
        return file.getPath();
    }

    private static void removeLibraryIfExist(String libName) {
        File file = new File(getPluginBasePath());
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f : files) {
                    if (f.getName().startsWith(libName)) {
                        f.delete();
                    }
                }
            }
        }
    }

    public static void checkAndGenAnyDoorJarAllDependence(ExecutionEnvironment env) {
        try {
            String dependenceJarFilePath = getPluginBasePath() + "/" + anyDoorAllDependenceName;
            if (new File(dependenceJarFilePath).exists()) {
                return;
            }

            File[] dependenceJars = new File(getPluginBasePath()).listFiles(curFile -> !curFile.getName().startsWith(anyDoorLibraryName) && curFile.getName().endsWith(".jar"));
            if (dependenceJars != null && dependenceJars.length != 0) {
                genAllDependenceFile(dependenceJars, dependenceJarFilePath);
            }
        } catch (Exception e) {
            NotifierUtil.notifyError(env.getProject(), anyDoorAllDependenceName + " does not exist. errMsg:" + e.getMessage());
        }
    }

    private static void genAllDependenceFile(File[] dependenceJars, String dependenceJarFilePath) throws IOException {
        FileOutputStream out = new FileOutputStream(dependenceJarFilePath);
        JarOutputStream jos = new JarOutputStream(out);

        Path tempDirectory = Files.createTempDirectory(anyDoorLibraryName);
        for (File jarFile : dependenceJars) {
            FileInputStream fis = new FileInputStream(jarFile);
            JarInputStream jis = new JarInputStream(fis);

            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            JarEntry entry = null;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File tempFile = new File(tempDirectory + "/" + entry.getName());
                if (!tempFile.exists()) {
                    tempFile.getParentFile().mkdirs();
                }

                FileOutputStream tempFileOut = new FileOutputStream(tempFile.getPath());
                while ((bytesRead = jis.read(buffer)) != -1) {
                    tempFileOut.write(buffer, 0, bytesRead);
                }
                tempFileOut.close();

                jis.closeEntry();
            }

            jis.close();
            fis.close();
        }


        copyJarFile(tempDirectory.toString(), jos, tempDirectory.toFile().listFiles());
        jos.close();
    }

    private static void copyJarFile(String basePath, JarOutputStream jos, File[] files) throws IOException {
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                copyJarFile(basePath, jos, file.listFiles());
                continue;
            }
            jos.putNextEntry(new JarEntry(file.getAbsolutePath().substring(basePath.length() + 1)));
            Files.copy(file.toPath(), jos);
        }
    }
}
