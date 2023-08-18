package io.github.lgp547.anydoorplugin.util;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import io.github.lgp547.anydoorplugin.AnyDoorInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class ImportNewUtil {

    public static String getPluginBasePath() {
        return PathManager.getPluginsPath() + File.separator + "any-door-plugin" + File.separator + "lib";
    }

//    public static String getPluginLibPath(String libraryName, String libraryVersion) {
//        return getPluginBasePath() + File.separator + libraryName + "-" + libraryVersion + ".jar";
//    }

    public static String getPluginLibPath(String libraryName) {
        return getPluginBasePath() + File.separator + libraryName;
    }

//    public static void fillAnyDoorJar(Project project, String libName, String libVersion) {
//        try {
//            // First check that the base path exists, then check from the maven path, and then download from the remote end
//            removeLibraryIfExist(libName);
//            String filePath = fillLibrary(libName, libVersion);
//            NotifierUtil.notifyInfo(project, "fill " + libName + " library success: " + filePath);
//        } catch (Exception e) {
//            NotifierUtil.notifyError(project, "fill " + libName + "library fail: " + e.getMessage());
//        }
//    }

//    /**
//     * todo: async remote download. lib pata from settings
//     */
//    private static String fillLibrary(String libName, String libVersion) throws IOException {
//        File file = new File(getPluginLibPath(libName, libVersion));
//        if (!file.exists()) {
//            String httpPath = "https://s01.oss.sonatype.org/content/repositories/releases" + AnyDoorInfo.ANY_DOOR_JAR_PATH.apply(libVersion);
//            FileUtils.copyURLToFile(new URL(httpPath), file);
//            if (!file.isFile()) {
//                throw new RuntimeException("down jar file fail");
//            }
//        }
//        return file.getPath();
//    }

//    private static void removeLibraryIfExist(String libName) {
//        File file = new File(getPluginBasePath());
//        if (file.exists()) {
//            File[] files = file.listFiles();
//            if (null != files) {
//                for (File f : files) {
//                    if (f.getName().startsWith(libName)) {
//                        f.delete();
//                    }
//                }
//            }
//        }
//    }

    public static void checkAndGenJar(@NotNull Project project, String anyDoorAllDependenceJar) {
        try {
            String dependenceJarFilePath = getPluginLibPath(anyDoorAllDependenceJar);
            if (new File(dependenceJarFilePath).exists()) {
                return;
            }
            File[] dependenceJars = new File(getPluginBasePath()).listFiles(curFile ->
                    !StringUtils.startsWithAny(curFile.getName(), AnyDoorInfo.ANY_DOOR_NAME, "arthas") && StringUtils.endsWith(curFile.getName(), ".jar"));
            if (dependenceJars != null && dependenceJars.length != 0) {
                genAllDependenceFile(dependenceJars, dependenceJarFilePath);
                NotifierUtil.notifyInfo(project, String.format("%s dependence gen %s", dependenceJars.length, dependenceJarFilePath));
            } else {
                NotifierUtil.notifyError(project, "dependence jar is empty.Please check the path:" + dependenceJarFilePath);
            }
        } catch (Exception e) {
            NotifierUtil.notifyError(project, anyDoorAllDependenceJar + " does not exist. errMsg:" + e.getMessage());
        }
    }

    private static void genAllDependenceFile(File[] dependenceJars, String dependenceJarFilePath) throws IOException {
        FileOutputStream out = new FileOutputStream(dependenceJarFilePath);
        JarOutputStream jos = new JarOutputStream(out);

        Path tempDirectory = Files.createTempDirectory(AnyDoorInfo.ANY_DOOR_NAME);
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
        out.close();
        FileUtils.deleteDirectory(tempDirectory.toFile());
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
            String jarEntryName = file.getAbsolutePath().substring(basePath.length() + 1);
            jarEntryName = StringUtils.replace(jarEntryName, "\\", "/");
            jos.putNextEntry(new JarEntry(jarEntryName));
            Files.copy(file.toPath(), jos);
        }
    }

    public static void fillAnyDoorJar(Project project, String moduleName) {
        try {
            Module module = getMainModule(project, Optional.ofNullable(moduleName));
            removeModuleLibraryIfExist(module, AnyDoorInfo.ANY_DOOR_NAME);
            File file = fillModuleLibrary(module, AnyDoorInfo.ANY_DOOR_JAR);
            NotifierUtil.notifyInfo(project, module.getName() + " fill ModuleLibrary success " + file.getPath());
        } catch (Exception e) {
            NotifierUtil.notifyError(project, "fill ModuleLibrary fail: " + e.getMessage());
        }
    }

    public static Module getMainModule(Project project, Optional<String> runModuleNameOp) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        if (modules.length == 1) {
            return modules[0];
        }
        if (runModuleNameOp.isPresent()) {
            for (Module module : modules) {
                if (module.getName().equals(runModuleNameOp.get())) {
                    return module;
                }
            }
        }
        throw new RuntimeException("main module could not find. size " + modules.length);
    }

    public static void removeModuleLibraryIfExist(@NotNull Module module, String jarName) {
        ModuleRootModificationUtil.updateModel(module, modifiableRootModel -> {
            LibraryTable moduleLibraryTable = modifiableRootModel.getModuleLibraryTable();
            Iterator<Library> libraryIterator = moduleLibraryTable.getLibraryIterator();
            while (libraryIterator.hasNext()) {
                Library next = libraryIterator.next();
                if (StringUtils.contains(next.getName(), jarName)) {
                    moduleLibraryTable.removeLibrary(next);
                }
            }
        });
    }

    public static void removeAllModuleLibrary(Project project, String jarName) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            removeModuleLibraryIfExist(module, jarName);
        }
        NotifierUtil.notifyInfo(project, "Remove module success");
    }

    public static File fillModuleLibrary(Module module, String libraryName) throws IOException {
        File file = new File(ImportNewUtil.getPluginLibPath(libraryName));
        if (!file.isFile()) {
            throw new FileNotFoundException("File not found [" + libraryName + "]");
        }
        ModuleRootModificationUtil.addModuleLibrary(module, libraryName, List.of("file://" + file.getPath()), List.of());
        return file;
    }

}
