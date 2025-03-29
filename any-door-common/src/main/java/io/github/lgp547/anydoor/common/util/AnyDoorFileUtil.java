/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
        try (
                FileOutputStream tmpLibOutputStream = new FileOutputStream(tmpLibFile);
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
