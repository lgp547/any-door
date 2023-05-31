package io.github.lgp547.anydoor.util;

import arthas.VmTool;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtil {

    /**
     * @return tmp file absolute path
     */
    public static String copyChildFile(File file, String child) throws IOException {
        if (StringUtils.endsWithIgnoreCase(file.getPath(), ".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                // 获取Jar包中所有的文件
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // 如果该文件是需要提取的资源文件
                    if (pathEquals(entryName, child)) {
                        return getTmpLibFile(jarFile.getInputStream(entry));
                    }
                }
            }
            return "";
        }
        if (file.isDirectory()) {
            file = new File(file.getAbsolutePath(), child);
        }
        return getTmpLibFile(Files.newInputStream(file.toPath()));
    }

    public static String getTmpLibFile(InputStream inputStream) throws IOException {
        File tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
        try (FileOutputStream tmpLibOutputStream = new FileOutputStream(tmpLibFile);
             InputStream inputStreamNew = inputStream) {
            copy(inputStreamNew, tmpLibOutputStream);
        }
        return tmpLibFile.getAbsolutePath();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static boolean pathEquals(String path1, String path2) {
        return StringUtils.pathEquals(path1, path2);
    }

}
