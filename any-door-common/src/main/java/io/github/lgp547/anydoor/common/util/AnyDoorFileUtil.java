package io.github.lgp547.anydoor.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnyDoorFileUtil {

    /**
     * @return tmp file absolute path
     */
    public static String copyChildFile(File file, String child) throws IOException {
        if (AnyDoorStringUtils.endsWithIgnoreCase(file.getPath(), ".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                // 获取Jar包中所有的文件
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // 如果该文件是需要提取的资源文件
                    if (AnyDoorStringUtils.pathEquals(entryName, child)) {
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
        File tmpLibFile = File.createTempFile("AnyDoorJniLibrary", null);
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

    public static String getTextFileAsString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            return sb.toString();
        }
    }

}
